package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.transporter.TransporterDto;
import com.codeperfection.shipit.dto.transporter.UpdateTransporterDto;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import com.codeperfection.shipit.service.TransporterService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.CommonFixtureFactory;
import com.codeperfection.shipit.util.TestUtil;
import com.codeperfection.shipit.util.TransporterFixtureFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.codeperfection.shipit.controller.CommonPathValues.API_V1;
import static com.codeperfection.shipit.controller.TransporterController.TRANSPORTERS_PATH;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TransporterControllerTest extends ControllerTestBase {

    @MockBean
    private TransporterService transporterService;

    @Test
    public void createTransporterIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(post(API_V1 + TRANSPORTERS_PATH));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void createTransporterIfInvalidPayloadReturnsError() throws Exception {
        mockAuthentication();
        final var transporterDto = new TransporterDto(null, "", -1);
        mockMvc.perform(post(API_V1 + TRANSPORTERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transporterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "capacity")));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void createTransporterIfValidPayloadReturnsDto() throws Exception {
        mockAuthentication();
        final var createTransporterDto = TransporterFixtureFactory.createCreateTransporterDto();
        final var transporterDto = TransporterFixtureFactory.createTransporterDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(transporterDto).when(transporterService).createTransporter(createTransporterDto, authenticatedUser);
        mockMvc.perform(post(API_V1 + TRANSPORTERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTransporterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(createTransporterDto)))
                .andExpect(redirectedUrlPattern("http://*" + API_V1 + TRANSPORTERS_PATH + "/" +
                        TransporterFixtureFactory.createTransporter().getUuid()));
        verify(transporterService).createTransporter(createTransporterDto, authenticatedUser);
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void getTransportersIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + TRANSPORTERS_PATH));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void getTransportersIfInvalidPaginationReturnsError() throws Exception {
        mockAuthentication();
        final var invalidPaginationFilterDto = new PaginationFilterDto(-1, 0);
        mockMvc.perform(get(API_V1 + TRANSPORTERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(invalidPaginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("page", "size")));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void getTransportersIfValidPaginationReturnsPageDto() throws Exception {
        mockAuthentication();
        final var paginationFilterDto = CommonFixtureFactory.createPaginationFilterDto();
        final var transporterDto = TransporterFixtureFactory.createTransporterDto();
        final var pageDto = CommonFixtureFactory.createPageDto(transporterDto);
        doReturn(pageDto).when(transporterService).getTransporters(paginationFilterDto, authenticatedUser);
        mockMvc.perform(get(API_V1 + TRANSPORTERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(paginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(pageDto)));
        verify(transporterService).getTransporters(paginationFilterDto, authenticatedUser);
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void getTransporterIfNotAuthenticatedReturnsError() throws Exception {
        final var transporterUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(get(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void getTransporterIfInvalidPathVariableReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(get(API_V1 + TRANSPORTERS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void getTransporterIfValidUuidReturnsDto() throws Exception {
        mockAuthentication();
        final var transporterDto = TransporterFixtureFactory.createTransporterDto();
        doReturn(transporterDto).when(transporterService).getTransporter(transporterDto.getUuid(), authenticatedUser);
        mockMvc.perform(get(API_V1 + TRANSPORTERS_PATH + "/" + transporterDto.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .param("uuid", transporterDto.getUuid().toString())
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(transporterDto)));
        verify(transporterService).getTransporter(transporterDto.getUuid(), authenticatedUser);
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void updateTransporterIfNotAuthenticatedReturnsError() throws Exception {
        final var transporterUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(put(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void updateTransporterIfInvalidPathVariableReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(put(API_V1 + TRANSPORTERS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void updateTransporterIfInvalidPayloadReturnsError() throws Exception {
        mockAuthentication();
        final var transporterUuid = UUID.randomUUID();
        final var updateTransporterDto = new UpdateTransporterDto("", -1);

        mockMvc.perform(put(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransporterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_REQUEST.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "capacity")));

        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void updateTransporterIfValidPayloadReturnsDto() throws Exception {
        mockAuthentication();
        final var transporterUuid = UUID.randomUUID();
        final var updateTransporterDto = new UpdateTransporterDto("a", 1);
        final var transporterDto = new TransporterDto(UUID.randomUUID(), updateTransporterDto.getName(),
                updateTransporterDto.getCapacity());
        doReturn(transporterDto).when(transporterService)
                .updateTransporter(transporterUuid, updateTransporterDto, authenticatedUser);

        mockMvc.perform(put(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTransporterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(transporterDto)))
                .andExpect(redirectedUrlPattern(
                        "http://*" + API_V1 + TRANSPORTERS_PATH + "/" + transporterDto.getUuid()));

        verify(transporterService).updateTransporter(transporterUuid, updateTransporterDto, authenticatedUser);
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void deleteTransporterIfNotAuthenticatedReturnsError() throws Exception {
        final var transporterUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(delete(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid));
        verifyNoInteractions(transporterService);
    }

    @Test
    public void deleteTransporterIfInvalidPathVariableReturnsError() throws Exception {
        checkBadRequestResponseOnInvalidPathVariable(delete(API_V1 + TRANSPORTERS_PATH + "/" +
                "InvalidUuid86bc3ac7-7ba5-446c-a751-9a525f7b2378"));
        verifyNoMoreInteractions(transporterService);
    }

    @Test
    public void deleteTransporterReturnsSuccessfulStatus() throws Exception {
        mockAuthentication();
        final var transporterUuid = UUID.randomUUID();
        mockMvc.perform(delete(API_V1 + TRANSPORTERS_PATH + "/" + transporterUuid.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful());

        verify(transporterService).deleteTransporter(transporterUuid, authenticatedUser);
        verifyNoMoreInteractions(transporterService);
    }
}
