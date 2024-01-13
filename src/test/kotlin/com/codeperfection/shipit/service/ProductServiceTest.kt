package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.entity.Product
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Clock
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var clock: Clock

    @InjectMocks
    private lateinit var underTest: ProductService

    private val productFixture = createProductFixture1()

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(productRepository)
    }

    @Test
    fun `GIVEN create product request, WHEN creating product, THEN it is saved in db and returned`() {
        whenever(clock.instant()).thenReturn(PRODUCT_CREATION_DATE.toInstant())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))
        whenever(productRepository.save(any<Product>())).thenReturn(productFixture)

        val productDto = underTest.createProduct(USER_ID, createProductDtoFixture1)

        val productArgumentCaptor = argumentCaptor<Product>()
        verify(productRepository).save(productArgumentCaptor.capture())
        val savedProduct = productArgumentCaptor.firstValue
        assertThat(savedProduct).usingRecursiveComparison().ignoringFields("id").isEqualTo(productFixture)
        assertThat(productDto).isEqualTo(productDtoFixture1)
    }

    @Test
    fun `GIVEN pagination filter, WHEN getting empty products, THEN an empty products page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(productRepository.findByUserIdAndIsActiveTrue(USER_ID, pageRequest)).thenReturn(PageImpl(emptyList()))

        val productsPage = underTest.getProducts(USER_ID, PaginationFilterDto())

        assertThat(productsPage).isEqualTo(
            PageDto<ProductDto>(
                totalElements = 0,
                totalPages = 1,
                elements = emptyList()
            )
        )
        verify(productRepository).findByUserIdAndIsActiveTrue(USER_ID, pageRequest)
    }

    @Test
    fun `GIVEN pagination filter, WHEN getting products, THEN products page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(productRepository.findByUserIdAndIsActiveTrue(USER_ID, pageRequest))
            .thenReturn(PageImpl(listOf(productFixture)))

        val productsPage = underTest.getProducts(USER_ID, PaginationFilterDto())

        assertThat(productsPage).isEqualTo(
            PageDto(totalElements = 1, totalPages = 1, elements = listOf(productDtoFixture1))
        )
        verify(productRepository).findByUserIdAndIsActiveTrue(USER_ID, pageRequest)
    }

    @Test
    fun `GIVEN product with id and user id doesn't exist, WHEN getting product, THEN exception is thrown`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.getProduct(USER_ID, PRODUCT_ID_1)
        }
        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
    }

    @Test
    fun `GIVEN product with id and user id exists, WHEN getting product, THEN it is returned`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(productFixture)

        val productDto = underTest.getProduct(USER_ID, PRODUCT_ID_1)

        assertThat(productDto).isEqualTo(productDtoFixture1)
        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
    }

    @Test
    fun `GIVEN product with id and user id doesn't exist, WHEN updating product, THEN exception is thrown`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.updateProduct(USER_ID, PRODUCT_ID_1, UpdateProductDto(countInStock = 4))
        }
        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
    }

    @Test
    fun `GIVEN product with id and user id exists, WHEN updating product, THEN it is updated in db and returned`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(productFixture)
        val newCountInStock = 123
        val updatedProduct = productFixture.copy(countInStock = newCountInStock)
        whenever(productRepository.save(updatedProduct)).thenReturn(updatedProduct)

        val productDto = underTest.updateProduct(USER_ID, PRODUCT_ID_1, UpdateProductDto(countInStock = newCountInStock))

        assertThat(productDto).isEqualTo(productDtoFixture1.copy(countInStock = newCountInStock))
        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
        verify(productRepository).save(updatedProduct)
    }

    @Test
    fun `GIVEN product with id and user id doesn't exist, WHEN deleting product, THEN exception is thrown`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.deleteProduct(USER_ID, PRODUCT_ID_1)
        }
        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
    }

    @Test
    fun `GIVEN product with id and user id exists, WHEN deleting product, THEN it is updated in db`() {
        whenever(productRepository.findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)).thenReturn(productFixture)
        val deactivatedProduct = productFixture.copy(isActive = false)
        whenever(productRepository.save(deactivatedProduct)).thenReturn(deactivatedProduct)

        underTest.deleteProduct(USER_ID, PRODUCT_ID_1)

        verify(productRepository).findByIdAndUserIdAndIsActiveTrue(PRODUCT_ID_1, USER_ID)
        verify(productRepository).save(deactivatedProduct)
    }
}
