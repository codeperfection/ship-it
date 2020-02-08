package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.ProductDto;
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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerTest extends ControllerTestBase {

    @MockBean
    private ProductService productService;

    @Test
    public void createProductIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(post(API_V1 + ProductController.PRODUCTS_PATH));
    }

    @Test
    public void createProductIfInvalidPayloadReturnsError() throws Exception {
        mockAuthentication();
        final var productDto = new ProductDto(null, "", 0, 0, -1);
        mockMvc.perform(post(API_V1 + ProductController.PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "volume", "price", "countInStock")));
        verifyNoInteractions(productService);
    }

    @Test
    public void createProductIfValidPayloadReturnsDto() throws Exception {
        mockAuthentication();
        final var createProductDto = ProductFixtureFactory.createCreateProductDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(productDto).when(productService).save(createProductDto, authenticatedUser);

        mockMvc.perform(post(API_V1 + ProductController.PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)))
                .andExpect(redirectedUrlPattern("http://*" + API_V1 + ProductController.PRODUCTS_PATH + "/" + productDto.getUuid()));

        verify(productService).save(createProductDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProductsIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + ProductController.PRODUCTS_PATH));
    }

    @Test
    public void getProductsIfInvalidPaginationReturnsError() throws Exception {
        mockAuthentication();
        final var invalidPaginationFilterDto = new PaginationFilterDto(-1, 0);
        mockMvc.perform(get(API_V1 + ProductController.PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(invalidPaginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("page", "size")));
        verifyNoInteractions(productService);
    }

    @Test
    public void getProductsIfValidPaginationReturnsPageDto() throws Exception {
        mockAuthentication();
        final var paginationFilterDto = CommonFixtureFactory.createPaginationFilterDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var pageDto = CommonFixtureFactory.createPageDto(productDto);
        doReturn(pageDto).when(productService).getProducts(paginationFilterDto, authenticatedUser);
        mockMvc.perform(get(API_V1 + ProductController.PRODUCTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(paginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(pageDto)));
        verify(productService).getProducts(paginationFilterDto, authenticatedUser);
        verifyNoMoreInteractions(productService);
    }

    @Test
    public void getProductIfNotAuthenticatedReturnsError() throws Exception {
        final var invalidUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(get(API_V1 + ProductController.PRODUCTS_PATH + "/" + invalidUuid));
    }

    @Test
    public void getProductsIfValidUuidReturnsDto() throws Exception {
        mockAuthentication();
        final var productDto = ProductFixtureFactory.createProductDto();
        doReturn(productDto).when(productService).getProduct(productDto.getUuid(), authenticatedUser);
        mockMvc.perform(get(API_V1 + ProductController.PRODUCTS_PATH + "/" + productDto.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .param("uuid", productDto.getUuid().toString())
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(productDto)));
        verify(productService).getProduct(productDto.getUuid(), authenticatedUser);
        verifyNoMoreInteractions(productService);
    }
}
