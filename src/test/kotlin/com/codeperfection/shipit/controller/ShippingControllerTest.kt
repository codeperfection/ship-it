package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.exception.ShippingImpossibleException
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.exception.dto.ErrorType
import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.service.shipping.ShippingManagementService
import com.codeperfection.shipit.service.shipping.placer.ShippingPlacementService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ShippingControllerTest : ControllerTestBase() {

    @MockBean
    private lateinit var shippingManagementService: ShippingManagementService

    @MockBean
    private lateinit var shippingPlacementService: ShippingPlacementService

    @AfterEach
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(shippingManagementService, shippingPlacementService)
    }

    private val shippingJson = """
        {
            "id": "$SHIPPING_ID",
            "name": "$SHIPPING_NAME",
            "userId": "$USER_ID",
            "createdAt": "2024-01-01T11:00:00Z",
            "transporter": {
                "id": "$TRANSPORTER_ID",
                "userId": "$USER_ID",
                "name": "$TRANSPORTER_NAME",
                "capacity": $TRANSPORTER_CAPACITY
            },
            "shippedItems": [
                {
                    "id": "$SHIPPED_ITEM_ID_1",
                    "product": {
                        "id": "$PRODUCT_ID_1",
                        "userId": "$USER_ID",
                        "name": "$PRODUCT_NAME_1",
                        "volume": $PRODUCT_VOLUME_1,
                        "price": $PRODUCT_PRICE_1,
                        "countInStock": $PRODUCT_COUNT_IN_STOCK_1
                    },
                    "quantity": $SHIPPED_ITEM_QUANTITY_1
                },
                {
                    "id": "$SHIPPED_ITEM_ID_2",
                    "product": {
                        "id": "$PRODUCT_ID_2",
                        "userId": "$USER_ID",
                        "name": "$PRODUCT_NAME_2",
                        "volume": $PRODUCT_VOLUME_2,
                        "price": $PRODUCT_PRICE_2,
                        "countInStock": $PRODUCT_COUNT_IN_STOCK_2
                    },
                    "quantity": $SHIPPED_ITEM_QUANTITY_2                    
                }
            ]
        }
    """.trimIndent()

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN creating shipping, THEN error response is returned`() {
        mockMvc.perform(
            post("/api/v1/users/$USER_ID/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "",
                        "transporterId": "cf6b1121-b808-44cd-9100-de7bad00c8cd"
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
    fun `GIVEN transporter without enough space for placement, WHEN creating shipping, THEN conflict is returned`() {
        whenever(shippingPlacementService.createShipping(USER_ID, createShippingDtoFixture))
            .thenThrow(ShippingImpossibleException(userId = USER_ID, transporterId = TRANSPORTER_ID))

        mockMvc.perform(
            post("/api/v1/users/$USER_ID/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "$SHIPPING_NAME",
                        "transporterId": "$TRANSPORTER_ID"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.FAILED_SHIPPING_PLACEMENT.name)))

        verify(shippingPlacementService).createShipping(USER_ID, createShippingDtoFixture)
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN creating shipping, THEN created shipping is returned`() {
        whenever(shippingPlacementService.createShipping(USER_ID, createShippingDtoFixture)).thenReturn(
            shippingDtoFixture
        )

        mockMvc.perform(
            post("/api/v1/users/$USER_ID/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "$SHIPPING_NAME",
                        "transporterId": "$TRANSPORTER_ID"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(content().json(shippingJson))
            .andExpect(
                header().string(
                    "location", "http://localhost/api/v1/users/$USER_ID/shippings/$SHIPPING_ID"
                )
            )

        verify(shippingPlacementService).createShipping(USER_ID, createShippingDtoFixture)
    }

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN getting shippings, THEN error response is returned`() {
        mockMvc.perform(
            get("/api/v1/users/$USER_ID/shippings")
                .queryParam("page", "-1")
                .queryParam("size", "0")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.INVALID_REQUEST.name)))
            .andExpect(jsonPath("fieldErrors", hasSize<JSONArray>(2)))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN getting all shippings, THEN expected list result is returned`() {
        val paginationFilterDto = PaginationFilterDto(page = 0, size = 1)
        whenever(shippingManagementService.getShippings(USER_ID, paginationFilterDto))
            .thenReturn(PageDto(totalElements = 1, totalPages = 1, elements = listOf(shippingDtoFixture)))

        mockMvc.perform(
            get("/api/v1/users/$USER_ID/shippings")
                .queryParam("page", "0")
                .queryParam("size", "1")
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                        "totalElements": 1,
                        "totalPages": 1,
                        "elements": [
                            $shippingJson
                        ]
                    }
                    """.trimIndent()
                )
            )

        verify(shippingManagementService).getShippings(USER_ID, paginationFilterDto)
    }

    @Test
    @WithMockUser
    fun `GIVEN non existing shipping id, WHEN getting shipping, THEN not found is returned`() {
        whenever(shippingManagementService.getShipping(USER_ID, SHIPPING_ID)).thenThrow(
            NotFoundException(
                SHIPPING_ID,
                USER_ID
            )
        )

        mockMvc.perform(get("/api/v1/users/$USER_ID/shippings/$SHIPPING_ID"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.NOT_FOUND.name)))

        verify(shippingManagementService).getShipping(USER_ID, SHIPPING_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN getting shipping, THEN expected shipping is returned`() {
        whenever(shippingManagementService.getShipping(USER_ID, SHIPPING_ID)).thenReturn(shippingDtoFixture)

        mockMvc.perform(get("/api/v1/users/$USER_ID/shippings/$SHIPPING_ID"))
            .andExpect(status().isOk)
            .andExpect(content().json(shippingJson))

        verify(shippingManagementService).getShipping(USER_ID, SHIPPING_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN deleting shipping, THEN no content is returned`() {
        mockMvc.perform(delete("/api/v1/users/$USER_ID/shippings/$SHIPPING_ID"))
            .andExpect(status().isNoContent)

        verify(shippingManagementService).deleteShipping(USER_ID, SHIPPING_ID)
    }
}
