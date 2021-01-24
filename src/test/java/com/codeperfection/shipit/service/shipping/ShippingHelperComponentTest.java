package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.Shipping;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.exception.clienterror.ShippingImpossibleException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.service.shipping.placer.Item;
import com.codeperfection.shipit.service.shipping.placer.Knapsack;
import com.codeperfection.shipit.service.shipping.placer.KnapsackPlacer;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.ProductFixtureFactory;
import com.codeperfection.shipit.util.ShippingFixtureFactory;
import com.codeperfection.shipit.util.TransporterFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShippingHelperComponentTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private KnapsackPlacer knapsackPlacer;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ShippingHelperComponent shippingHelperComponent;

    @Test
    public void convertToItems_ReturnsItemsArray() {
        final var product = ProductFixtureFactory.createProduct();
        final var expectedItem = new Item(product, product.getVolume(), product.getPrice());
        final var items = shippingHelperComponent.convertToItems(List.of(product));
        assertThat(items).containsExactlyElementsOf(Collections.nCopies(product.getCountInStock(), expectedItem));
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void runPlacer_IfPlacementImpossible_ThrowsException() {
        final var transporterCapacity = 10;
        final Product product = ProductFixtureFactory.createProduct();
        final Item[] items = new Item[product.getCountInStock()];
        Arrays.fill(items, Item.valueOf(product));
        final Knapsack knapsack = new Knapsack(10, 10, Collections.emptyList());
        doReturn(knapsack).when(knapsackPlacer).place(items, transporterCapacity);

        assertThatExceptionOfType(ShippingImpossibleException.class).isThrownBy(() ->
                shippingHelperComponent.runPlacer(Transporter.builder().capacity(transporterCapacity).build(),
                        Collections.singletonList(product)));

        verify(knapsackPlacer).place(items, transporterCapacity);
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void runPlacer_IfPlacementPossible_ReturnWantedResult() {
        final var transporterCapacity = 10;
        final Product product = ProductFixtureFactory.createProduct();
        final Item[] items = new Item[product.getCountInStock()];
        Item item = Item.valueOf(product);
        Arrays.fill(items, item);
        final Knapsack knapsack = new Knapsack(10, 10, List.of(item, item, item));
        doReturn(knapsack).when(knapsackPlacer).place(items, transporterCapacity);

        assertThat(shippingHelperComponent.runPlacer(Transporter.builder().capacity(transporterCapacity).build(),
                Collections.singletonList(product))).contains(Map.entry(product, 3L));

        verify(knapsackPlacer).place(items, transporterCapacity);
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void deductPlacedProductsFromStock_DeductsCounts() {
        final var product = ProductFixtureFactory.createProduct();
        final var placedProduct = ProductFixtureFactory.createProduct();
        int count = 2;
        placedProduct.setCountInStock(placedProduct.getCountInStock() - count);
        doReturn(placedProduct).when(productRepository).save(placedProduct);
        shippingHelperComponent.deductPlacedProductsFromStock(Map.of(product, (long) count));

        assertThat(product).isEqualTo(placedProduct);
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void saveShipping_PersistsInDb() {
        final var shipping = ShippingFixtureFactory.createShipping();
        doReturn(shipping).when(shippingRepository).save(any());

        final var shippedItems = ShippingFixtureFactory.createShippedItems();
        final var returnedShipping = shippingHelperComponent.saveShipping(
                ShippingFixtureFactory.createCreateShippingDto(), TransporterFixtureFactory.createTransporter(),
                AuthenticationFixtureFactory.createUser(), shippedItems);
        assertThat(returnedShipping).isEqualTo(shipping);

        final var shippingArgumentCaptor = ArgumentCaptor.forClass(Shipping.class);
        verify(shippingRepository).save(shippingArgumentCaptor.capture());
        final var savedShipping = shippingArgumentCaptor.getValue();
        assertThat(savedShipping).usingRecursiveComparison().ignoringFields("uuid", "createdAt", "shippedItems")
                .isEqualTo(shipping);
        final var epsilon = within(10, ChronoUnit.SECONDS);
        assertThat(savedShipping.getCreatedAt()).isCloseToUtcNow(epsilon);
        shippedItems.forEach(shippedItem -> assertThat(shippedItem.getShipping()).isEqualTo(savedShipping));
        assertThat(savedShipping.getShippedItems()).usingElementComparatorIgnoringFields("shipping")
                .containsExactlyElementsOf(shippedItems);
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void createShippedItems_ReturnsShippedItems() {
        final var product = ProductFixtureFactory.createProduct();
        int count = 2;
        final var shippedItems = shippingHelperComponent.createShippedItems(Map.of(product, (long) count));

        assertThat(shippedItems).usingElementComparatorIgnoringFields("uuid")
                .containsExactlyElementsOf(ShippingFixtureFactory.createShippedItems());
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }

    @Test
    public void mapToDto_ReturnsDto() {
        assertThat(shippingHelperComponent.mapToDto(ShippingFixtureFactory.createShipping()))
                .isEqualTo(ShippingFixtureFactory.createShippingDto());
        verifyNoMoreInteractions(productRepository, shippingRepository, knapsackPlacer);
    }
}
