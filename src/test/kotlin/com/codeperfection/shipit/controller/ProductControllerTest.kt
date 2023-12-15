package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.exception.dto.ErrorType
import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.service.ProductService
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasSize
import org.json.JSONArray
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ProductControllerTest : ControllerTestBase() {

    @MockBean
    private lateinit var productService: ProductService

    @AfterEach
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(productService)
    }

    private val productJson = """
        {
            "id": "$PRODUCT_ID",
            "userId": "$USER_ID",
            "name": "$PRODUCT_NAME",
            "volume": $PRODUCT_VOLUME,
            "price": $PRODUCT_PRICE,
            "countInStock": $PRODUCT_COUNT_IN_STOCK
        }
    """.trimIndent()

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN creating product, THEN error response is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users/$USER_ID/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "  ",
                        "volume": 0,
                        "price": 0,
                        "countInStock": -1
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.INVALID_REQUEST.name)))
            .andExpect(jsonPath("fieldErrors", hasSize<JSONArray>(4)))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN creating product, THEN created product is returned`() {
        whenever(productService.createProduct(USER_ID, createProductDtoFixture)).thenReturn(productDtoFixture)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users/$USER_ID/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "$PRODUCT_NAME",
                        "volume": $PRODUCT_VOLUME,
                        "price": $PRODUCT_PRICE,
                        "countInStock": $PRODUCT_COUNT_IN_STOCK
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(content().json(productJson))

        verify(productService).createProduct(USER_ID, createProductDtoFixture)
    }

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN getting products page, THEN error response is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/products")
                .queryParam("page", "-1")
                .queryParam("size", "0")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.INVALID_REQUEST.name)))
            .andExpect(jsonPath("fieldErrors", hasSize<JSONArray>(2)))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN getting products page, THEN products page is returned`() {
        val paginationFilterDto = PaginationFilterDto(page = 0, size = 1)
        whenever(productService.getProducts(USER_ID, paginationFilterDto))
            .thenReturn(PageDto(totalElements = 1, totalPages = 1, elements = listOf(productDtoFixture)))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/products")
                .queryParam("page", "0")
                .queryParam("size", "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                        "totalElements": 1,
                        "totalPages": 1,
                        "elements": [
                            $productJson
                        ]
                    }
                    """.trimIndent()
                )
            )

        verify(productService).getProducts(USER_ID, paginationFilterDto)
    }

    @Test
    @WithMockUser
    fun `GIVEN request for a product that doesn't exist, WHEN getting the product, THEN not found is returned`() {
        whenever(productService.getProduct(USER_ID, PRODUCT_ID)).thenThrow(NotFoundException(PRODUCT_ID))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/products/$PRODUCT_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.NOT_FOUND.name)))

        verify(productService).getProduct(USER_ID, PRODUCT_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN request for a product that exists, WHEN getting the product, THEN product is returned`() {
        whenever(productService.getProduct(USER_ID, PRODUCT_ID)).thenReturn(productDtoFixture)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/products/$PRODUCT_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(productJson)
            )

        verify(productService).getProduct(USER_ID, PRODUCT_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN updating product, THEN error response is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/users/$USER_ID/products/$PRODUCT_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "countInStock": -1
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.INVALID_REQUEST.name)))
            .andExpect(jsonPath("fieldErrors", hasSize<JSONArray>(1)))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN updating product, THEN updated product is returned`() {
        val newCountInStock = 123
        whenever(productService.updateProduct(USER_ID, PRODUCT_ID, UpdateProductDto(countInStock = newCountInStock)))
            .thenReturn(productDtoFixture.copy(countInStock = newCountInStock))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/v1/users/$USER_ID/products/$PRODUCT_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "countInStock": $newCountInStock
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                        "id": "$PRODUCT_ID",
                        "userId": "$USER_ID",
                        "name": "$PRODUCT_NAME",
                        "volume": $PRODUCT_VOLUME,
                        "price": $PRODUCT_PRICE,
                        "countInStock": $newCountInStock
                    }
                    """.trimIndent()
                )
            )

        verify(productService).updateProduct(USER_ID, PRODUCT_ID, UpdateProductDto(countInStock = newCountInStock))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN deleting product, THEN no content is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/users/$USER_ID/products/$PRODUCT_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        verify(productService).deleteProduct(USER_ID, PRODUCT_ID)
    }
}
