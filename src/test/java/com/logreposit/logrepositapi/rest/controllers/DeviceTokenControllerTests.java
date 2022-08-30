package com.logreposit.logrepositapi.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.devicetoken.DeviceTokenService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {DeviceTokenController.class})
public class DeviceTokenControllerTests {
  private static final MediaType EXPECTED_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  @MockBean private UserService userService;

  @MockBean private DeviceService deviceService;

  @MockBean private DeviceTokenService deviceTokenService;

  @Autowired private MockMvc controller;

  @Captor private ArgumentCaptor<DeviceToken> deviceTokenArgumentCaptor;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() throws UserNotFoundException, ApiKeyNotFoundException {
    ControllerTestUtils.prepareDefaultUsers(this.userService);
  }

  @Test
  public void testCreate() throws Exception {
    String deviceId = UUID.randomUUID().toString();

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.post("/v1/devices/" + deviceId + "/tokens")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.create(Mockito.eq(deviceId), Mockito.eq(regularUser.getId())))
        .thenReturn(deviceToken);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").value(deviceToken.getId()))
        .andExpect(jsonPath("$.data.token").value(deviceToken.getToken()))
        .andExpect(jsonPath("$.data.createdAt").exists());

    Mockito.verify(this.deviceTokenService, Mockito.times(1))
        .create(Mockito.eq(deviceId), Mockito.eq(regularUser.getId()));
  }

  @Test
  public void testList() throws Exception {
    String deviceId = UUID.randomUUID().toString();

    int defaultPageNumber = 0;
    int defaultPageSize = 10;

    User regularUser = ControllerTestUtils.getRegularUser();

    DeviceToken deviceToken1 = sampleDeviceToken(deviceId);
    DeviceToken deviceToken2 = sampleDeviceToken(deviceId);

    List<DeviceToken> deviceTokens = Arrays.asList(deviceToken1, deviceToken2);
    Page<DeviceToken> deviceTokenPage = new PageImpl<>(deviceTokens);

    Mockito.when(
            this.deviceTokenService.list(
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId()),
                Mockito.anyInt(),
                Mockito.anyInt()))
        .thenReturn(deviceTokenPage);

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/devices/" + deviceId + "/tokens")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].id").value(deviceToken1.getId()))
        .andExpect(jsonPath("$.data.items[0].token").value(deviceToken1.getToken()))
        .andExpect(jsonPath("$.data.items[0].createdAt").exists())
        .andExpect(jsonPath("$.data.items[1].id").value(deviceToken2.getId()))
        .andExpect(jsonPath("$.data.items[1].token").value(deviceToken2.getToken()))
        .andExpect(jsonPath("$.data.items[1].createdAt").exists());

    ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> pageSizeArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

    Mockito.verify(this.deviceTokenService, Mockito.times(1))
        .list(
            Mockito.eq(deviceId),
            Mockito.eq(regularUser.getId()),
            pageNumberArgumentCaptor.capture(),
            pageSizeArgumentCaptor.capture());

    Integer pageNumber = pageNumberArgumentCaptor.getValue();
    Integer pageSize = pageSizeArgumentCaptor.getValue();

    assertThat(pageNumber).isNotNull();
    assertThat(pageSize).isNotNull();
    assertThat(pageNumber.intValue()).isEqualTo(defaultPageNumber);
    assertThat(pageSize.intValue()).isEqualTo(defaultPageSize);
  }

  @Test
  public void testList_customPaginationSettings() throws Exception {
    String deviceId = UUID.randomUUID().toString();

    int pageNumber = 1;
    int pageSize = 8;

    User regularUser = ControllerTestUtils.getRegularUser();

    DeviceToken deviceToken1 = sampleDeviceToken(deviceId);
    DeviceToken deviceToken2 = sampleDeviceToken(deviceId);

    List<DeviceToken> deviceTokens = Arrays.asList(deviceToken1, deviceToken2);
    Page<DeviceToken> deviceTokenPage = new PageImpl<>(deviceTokens);

    Mockito.when(
            this.deviceTokenService.list(
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId()),
                Mockito.anyInt(),
                Mockito.anyInt()))
        .thenReturn(deviceTokenPage);

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get(
                "/v1/devices/" + deviceId + "/tokens?page=" + pageNumber + "&size=" + pageSize)
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].id").value(deviceToken1.getId()))
        .andExpect(jsonPath("$.data.items[0].token").value(deviceToken1.getToken()))
        .andExpect(jsonPath("$.data.items[0].createdAt").exists())
        .andExpect(jsonPath("$.data.items[1].id").value(deviceToken2.getId()))
        .andExpect(jsonPath("$.data.items[1].token").value(deviceToken2.getToken()))
        .andExpect(jsonPath("$.data.items[1].createdAt").exists());

    ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> pageSizeArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

    Mockito.verify(this.deviceTokenService, Mockito.times(1))
        .list(
            Mockito.eq(deviceId),
            Mockito.eq(regularUser.getId()),
            pageNumberArgumentCaptor.capture(),
            pageSizeArgumentCaptor.capture());

    Integer capturedPageNumber = pageNumberArgumentCaptor.getValue();
    Integer capturedPageSize = pageSizeArgumentCaptor.getValue();

    assertThat(pageNumber).isNotNull();
    assertThat(pageSize).isNotNull();
    assertThat(capturedPageNumber.intValue()).isEqualTo(pageNumber);
    assertThat(capturedPageSize.intValue()).isEqualTo(pageSize);
  }

  @Test
  public void testList_customPaginationSettings_exceedsLimits() throws Exception {
    String deviceId = UUID.randomUUID().toString();

    int pageNumber = -1;
    int pageSize = 40;

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get(
                "/v1/devices/" + deviceId + "/tokens?page=" + pageNumber + "&size=" + pageSize)
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(80016))
        .andExpect(
            jsonPath("$.message")
                .value(containsString("list.size: size must be less or equal than 25")))
        .andExpect(
            jsonPath("$.message")
                .value(containsString("list.page: page must be greater than or equal to 0")));
  }

  @Test
  public void testList_noSuchDevice() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();

    Mockito.when(
            this.deviceTokenService.list(
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId()),
                Mockito.anyInt(),
                Mockito.anyInt()))
        .thenThrow(new DeviceNotFoundException(""));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/devices/" + deviceId + "/tokens")
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(30001))
        .andExpect(jsonPath("$.message").value("Given device resource not found."));
  }

  @Test
  public void testGet() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.get(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenReturn(deviceToken);

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").value(deviceToken.getId()))
        .andExpect(jsonPath("$.data.token").value(deviceToken.getToken()))
        .andExpect(jsonPath("$.data.createdAt").exists());

    Mockito.verify(this.deviceTokenService, Mockito.times(1))
        .get(
            Mockito.eq(deviceToken.getId()), Mockito.eq(deviceId), Mockito.eq(regularUser.getId()));
  }

  @Test
  public void testGet_noSuchDevice() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.get(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenThrow(new DeviceNotFoundException(""));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(30001))
        .andExpect(jsonPath("$.message").value("Given device resource not found."));
  }

  @Test
  public void testGet_noSuchDeviceToken() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.get(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenThrow(new DeviceTokenNotFoundException("", deviceToken.getToken()));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(40001))
        .andExpect(jsonPath("$.message").value("Given device-token resource not found."));
  }

  @Test
  public void testDelete() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.delete(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenReturn(deviceToken);

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.delete("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.id").value(deviceToken.getId()))
        .andExpect(jsonPath("$.data.token").value(deviceToken.getToken()))
        .andExpect(jsonPath("$.data.createdAt").exists());

    Mockito.verify(this.deviceTokenService, Mockito.times(1))
        .delete(
            Mockito.eq(deviceToken.getId()), Mockito.eq(deviceId), Mockito.eq(regularUser.getId()));
  }

  @Test
  public void testDelete_noSuchDevice() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.delete(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenThrow(new DeviceNotFoundException(""));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.delete("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(30001))
        .andExpect(jsonPath("$.message").value("Given device resource not found."));
  }

  @Test
  public void testDelete_noSuchDeviceToken() throws Exception {
    String deviceId = UUID.randomUUID().toString();
    User regularUser = ControllerTestUtils.getRegularUser();
    DeviceToken deviceToken = sampleDeviceToken(deviceId);

    Mockito.when(
            this.deviceTokenService.delete(
                Mockito.eq(deviceToken.getId()),
                Mockito.eq(deviceId),
                Mockito.eq(regularUser.getId())))
        .thenThrow(new DeviceTokenNotFoundException("", deviceToken.getToken()));

    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.delete("/v1/devices/" + deviceId + "/tokens/" + deviceToken.getId())
            .header(
                LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME,
                ControllerTestUtils.REGULAR_USER_API_KEY);

    this.controller
        .perform(request)
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(EXPECTED_CONTENT_TYPE))
        .andExpect(jsonPath("$.correlationId").isString())
        .andExpect(jsonPath("$.status").value("ERROR"))
        .andExpect(jsonPath("$.code").value(40001))
        .andExpect(jsonPath("$.message").value("Given device-token resource not found."));
  }

  private static DeviceToken sampleDeviceToken(String deviceId) {
    DeviceToken deviceToken = new DeviceToken();

    deviceToken.setId(UUID.randomUUID().toString());
    deviceToken.setToken(UUID.randomUUID().toString());
    deviceToken.setDeviceId(deviceId);
    deviceToken.setCreatedAt(new Date());

    return deviceToken;
  }
}
