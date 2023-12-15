package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.exception.dto.ErrorType
import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.service.TransporterService
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

class TransporterControllerTest : ControllerTestBase() {

    @MockBean
    private lateinit var transporterService: TransporterService

    @AfterEach
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(transporterService)
    }

    private val transporterJson = """
        {
            "id": "$TRANSPORTER_ID",
            "userId": "$USER_ID",
            "name": "$TRANSPORTER_NAME",
            "capacity": $TRANSPORTER_CAPACITY
        }
    """.trimIndent()

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN creating transporter, THEN error response is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users/$USER_ID/transporters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "  ",
                        "capacity": 0
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.INVALID_REQUEST.name)))
            .andExpect(jsonPath("fieldErrors", hasSize<JSONArray>(2)))
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN creating transporter, THEN created transporter is returned`() {
        whenever(transporterService.createTransporter(USER_ID, createTransporterDtoFixture)).thenReturn(
            transporterDtoFixture
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users/$USER_ID/transporters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "$TRANSPORTER_NAME",
                        "capacity": $TRANSPORTER_CAPACITY
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(content().json(transporterJson))

        verify(transporterService).createTransporter(USER_ID, createTransporterDtoFixture)
    }

    @Test
    @WithMockUser
    fun `GIVEN invalid request, WHEN getting transporters page, THEN error response is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/transporters")
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
    fun `GIVEN valid request, WHEN getting transporters page, THEN transporters page is returned`() {
        val paginationFilterDto = PaginationFilterDto(page = 0, size = 1)
        whenever(transporterService.getTransporters(USER_ID, paginationFilterDto))
            .thenReturn(PageDto(totalElements = 1, totalPages = 1, elements = listOf(transporterDtoFixture)))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/transporters")
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
                            $transporterJson
                        ]
                    }
                    """.trimIndent()
                )
            )

        verify(transporterService).getTransporters(USER_ID, paginationFilterDto)
    }

    @Test
    @WithMockUser
    fun `GIVEN request for a transporter that doesn't exist, WHEN getting the transporter, THEN not found is returned`() {
        whenever(
            transporterService.getTransporter(
                USER_ID,
                TRANSPORTER_ID
            )
        ).thenThrow(NotFoundException(TRANSPORTER_ID))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/transporters/$TRANSPORTER_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("errorType", Matchers.`is`(ErrorType.NOT_FOUND.name)))

        verify(transporterService).getTransporter(USER_ID, TRANSPORTER_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN request for a transporter that exists, WHEN getting the transporter, THEN transporter is returned`() {
        whenever(transporterService.getTransporter(USER_ID, TRANSPORTER_ID)).thenReturn(transporterDtoFixture)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/v1/users/$USER_ID/transporters/$TRANSPORTER_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(transporterJson)
            )

        verify(transporterService).getTransporter(USER_ID, TRANSPORTER_ID)
    }

    @Test
    @WithMockUser
    fun `GIVEN valid request, WHEN deleting transporter, THEN no content is returned`() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/v1/users/$USER_ID/transporters/$TRANSPORTER_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)

        verify(transporterService).deleteTransporter(USER_ID, TRANSPORTER_ID)
    }
}
