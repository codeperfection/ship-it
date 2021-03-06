package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.CreateProductDto;
import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.dto.product.UpdateCountInStockDto;
import com.codeperfection.shipit.dto.product.UpdateProductDto;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import com.codeperfection.shipit.service.ProductService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.CommonFixtureFactory;
import com.codeperfection.shipit.util.ProductFixtureFactory;
import com.codeperfection.shipit.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.codeperfection.shipit.controller.CommonPathValues.API_V1;
import static com.codeperfection.shipit.controller.ProductController.COUNT_IN_STOCK_PATH;
import static com.codeperfection.shipit.controller.ProductController.PRODUCTS_PATH;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerTest extends ControllerTestBase {

    @MockBean
    private ProductService productService;

    @Test
    public void createProduct_IfNotAuthenticated_ReturnsError() throws Exception {
        checkUnauthorizedResponse(post(API_V1 + PRODUCTS_PATH));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void createProduct_IfInvalidPayload_ReturnsError() throws Exception {
        mockAuthentication();
        final var createProductDto = new CreateProductDto("", 0, 0, -1);
        mockMvc.perform(post(API_V1 + PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "volume", "price", "countInStock")));
        verifyNoInteractions(productService);
    }

    @Test
    public void createProduct_IfValidPayload_ReturnsDto() throws Exception {
        mockAuthentication();
        final var createProductDto = ProductFixtureFactory.createCreateProductDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(productDto).when(productService).createProduct(createProductDto, authenticatedUser);

        mockMvc.perform(post(API_V1 + PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)))
                .andExpect(redirectedUrlPattern("http://*" + API_V1 + PRODUCTS_PATH + "/" + productDto.getUuid()));

        verify(productService).createProduct(createProductDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProducts_IfNotAuthenticated_ReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + PRODUCTS_PATH));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProducts_IfInvalidPagination_ReturnsError() throws Exception {
        mockAuthentication();
        final var invalidPaginationFilterDto = new PaginationFilterDto(-1, 0);
        mockMvc.perform(get(API_V1 + PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(invalidPaginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("page", "size")));
        verifyNoInteractions(productService);
    }

    @Test
    public void getProducts_IfValidPagination_ReturnsPageDto() throws Exception {
        mockAuthentication();
        final var paginationFilterDto = CommonFixtureFactory.createPaginationFilterDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var pageDto = CommonFixtureFactory.createPageDto(productDto);
        doReturn(pageDto).when(productService).getProducts(paginationFilterDto, authenticatedUser);
        mockMvc.perform(get(API_V1 + PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(paginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(pageDto)));
        verify(productService).getProducts(paginationFilterDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProduct_IfNotAuthenticated_ReturnsError() throws Exception {
        final var productUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(get(API_V1 + PRODUCTS_PATH + "/" + productUuid));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProduct_IfInvalidPathVariable_ReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(get(API_V1 + PRODUCTS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProduct_IfValidUuid_ReturnsDto() throws Exception {
        mockAuthentication();
        final var productDto = ProductFixtureFactory.createProductDto();
        doReturn(productDto).when(productService).getProduct(productDto.getUuid(), authenticatedUser);
        mockMvc.perform(get(API_V1 + PRODUCTS_PATH + "/" + productDto.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .param("uuid", productDto.getUuid().toString())
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)));
        verify(productService).getProduct(productDto.getUuid(), authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateProduct_IfNotAuthenticated_ReturnsError() throws Exception {
        final var productUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(put(API_V1 + PRODUCTS_PATH + "/" + productUuid));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateProduct_IfInvalidPathVariable_ReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(put(API_V1 + PRODUCTS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateProduct_IfInvalidPayload_ReturnsError() throws Exception {
        mockAuthentication();
        final var productUuid = UUID.randomUUID();
        final var updateProductDto = new UpdateProductDto("", -1, -1);

        mockMvc.perform(put(API_V1 + PRODUCTS_PATH + "/" + productUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "volume", "price")));

        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateProduct_IfValidPayload_ReturnsDto() throws Exception {
        mockAuthentication();
        final var productUuid = UUID.randomUUID();
        final var updateProductDto = new UpdateProductDto("a", 1, 1);
        final var productDto = new ProductDto(UUID.randomUUID(), updateProductDto.getName(),
                updateProductDto.getVolume(), updateProductDto.getPrice(), 5);
        doReturn(productDto).when(productService).updateProduct(productUuid, updateProductDto, authenticatedUser);

        mockMvc.perform(put(API_V1 + PRODUCTS_PATH + "/" + productUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)))
                .andExpect(redirectedUrlPattern("http://*" + API_V1 + PRODUCTS_PATH + "/" + productDto.getUuid()));

        verify(productService).updateProduct(productUuid, updateProductDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateCountInStock_IfNotAuthenticated_ReturnsError() throws Exception {
        final var productUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(put(API_V1 + PRODUCTS_PATH + "/" + productUuid + COUNT_IN_STOCK_PATH));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateCountInStock_IfInvalidPathVariable_ReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(put(API_V1 + PRODUCTS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378" + COUNT_IN_STOCK_PATH));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateCountInStock_IfInvalidPayload_ReturnsError() throws Exception {
        mockAuthentication();
        final var productUuid = UUID.randomUUID();
        final var updateCountInStockDto = new UpdateCountInStockDto(-1);

        mockMvc.perform(put(API_V1 + PRODUCTS_PATH + "/" + productUuid.toString() + COUNT_IN_STOCK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCountInStockDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("countInStock")));

        verifyNoMoreInteractions(productService);
    }

    @Test
    public void updateCountInStock_IfValidPayload_ReturnsDto() throws Exception {
        mockAuthentication();
        final var productUuid = UUID.randomUUID();
        final var updateCountInStockDto = new UpdateCountInStockDto(3);
        final var productDto = new ProductDto(productUuid, "name", 1, 1,
                updateCountInStockDto.getCountInStock());
        doReturn(productDto).when(productService).updateCountInStock(
                productUuid, updateCountInStockDto, authenticatedUser);

        mockMvc.perform(put(API_V1 + PRODUCTS_PATH + "/" + productUuid.toString() + COUNT_IN_STOCK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCountInStockDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)));

        verify(productService).updateCountInStock(productUuid, updateCountInStockDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void deleteProduct_IfNotAuthenticated_ReturnsError() throws Exception {
        final var productUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(delete(API_V1 + PRODUCTS_PATH + "/" + productUuid + COUNT_IN_STOCK_PATH));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void deleteProduct_IfInvalidPathVariable_ReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(delete(API_V1 + PRODUCTS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void deleteProduct_ReturnsSuccessfulStatus() throws Exception {
        mockAuthentication();
        final var productUuid = UUID.randomUUID();
        mockMvc.perform(delete(API_V1 + PRODUCTS_PATH + "/" + productUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful());

        verify(productService).deleteProduct(productUuid, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }
}
