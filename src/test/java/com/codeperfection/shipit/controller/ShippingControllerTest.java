package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.CreateShippingDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.exception.ErrorType;
import com.codeperfection.shipit.service.ShippingService;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.CommonFixtureFactory;
import com.codeperfection.shipit.util.ShippingFixtureFactory;
import com.codeperfection.shipit.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.codeperfection.shipit.controller.RequestValues.API_V1;
import static com.codeperfection.shipit.controller.RequestValues.SHIPPINGS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ShippingControllerTest extends ControllerTestBase {

    @MockBean
    private ShippingService shippingService;

    @Test
    public void createShippingIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(post(API_V1 + SHIPPINGS));
    }

    @Test
    public void createShippingIfInvalidPayloadReturnsError() throws Exception {
        mockAuthentication();
        final var createShippingDto = new CreateShippingDto("", null, null);
        mockMvc.perform(post(API_V1 + SHIPPINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createShippingDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("name", "transporterUuid", "timeZoneName")));
        verifyNoInteractions(shippingService);
    }

    @Test
    public void createShippingIfValidPayloadReturnsDto() throws Exception {
        mockAuthentication();
        final var createShippingDto = ShippingFixtureFactory.createCreateShippingDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(shippingDto).when(shippingService).createShipping(createShippingDto, authenticatedUser);
        mockMvc.perform(post(API_V1 + SHIPPINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createShippingDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(shippingDto)))
                .andExpect(redirectedUrlPattern("http://*" + API_V1 + SHIPPINGS + "/" + shippingDto.getUuid()));
        verify(shippingService).createShipping(createShippingDto, authenticatedUser);
        verifyNoMoreInteractions(shippingService);
    }

    @Test
    public void getShippingsIfNotAuthenticatedReturnsError() throws Exception {
        checkUnauthorizedResponse(get(API_V1 + SHIPPINGS));
    }

    @Test
    public void getShippingsIfInvalidPaginationReturnsError() throws Exception {
        mockAuthentication();
        final var invalidPaginationFilterDto = new PaginationFilterDto(-1, 0);
        mockMvc.perform(get(API_V1 + SHIPPINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(invalidPaginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errorType", is(ErrorType.INVALID_PAYLOAD.getDisplayName())))
                .andExpect(jsonPath("$.fieldErrors[*].fieldName",
                        containsInAnyOrder("page", "size")));
        verifyNoInteractions(shippingService);
    }

    @Test
    public void getShippingsIfValidPaginationReturnsPageDto() throws Exception {
        mockAuthentication();
        final var paginationFilterDto = CommonFixtureFactory.createPaginationFilterDto();
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        final var pageDto = CommonFixtureFactory.createPageDto(shippingDto);
        doReturn(pageDto).when(shippingService).getShippings(paginationFilterDto, authenticatedUser);
        mockMvc.perform(get(API_V1 + SHIPPINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .params(TestUtil.toMultiValueMap(paginationFilterDto))
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(pageDto)));
        verify(shippingService).getShippings(paginationFilterDto, authenticatedUser);
        verifyNoMoreInteractions(shippingService);
    }

    @Test
    public void getShippingIfNotAuthenticatedReturnsError() throws Exception {
        final var invalidUuid = UUID.fromString("86bc3ac7-7ba5-446c-a751-9a525f7b2378");
        checkUnauthorizedResponse(get(API_V1 + SHIPPINGS + "/" + invalidUuid));
    }

    @Test
    public void getShippingsIfValidUuidReturnsDto() throws Exception {
        mockAuthentication();
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(shippingDto).when(shippingService).getShipping(shippingDto.getUuid(), authenticatedUser);
        mockMvc.perform(get(API_V1 + SHIPPINGS + "/" + shippingDto.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .param("uuid", shippingDto.getUuid().toString())
                .header(HttpHeaders.AUTHORIZATION, getJwtMockAuthorization()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(shippingDto)));
        verify(shippingService).getShipping(shippingDto.getUuid(), authenticatedUser);
        verifyNoMoreInteractions(shippingService);
    }
}
