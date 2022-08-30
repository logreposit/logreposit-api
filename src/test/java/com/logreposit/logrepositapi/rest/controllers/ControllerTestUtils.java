package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.ArrayList;
import java.util.Collections;
import org.mockito.Mockito;

public class ControllerTestUtils {
  public static final String ADMIN_USER_API_KEY = "ef05b0b1-89d0-446f-bfb2-81974143dc8a";
  public static final String REGULAR_USER_API_KEY = "74c3e9df-8346-4c34-a96e-64708bfbe163";
  public static final String ROLELESS_USER_API_KEY = "5c9e7f20-92c9-4c08-81ee-c892b491bb89";
  public static final String VALID_DEVICE_TOKEN = "a96532dd-8c8d-47ab-9e16-ebb10f8c2277";

  public static void prepareDefaultUsers(UserService userService)
      throws UserNotFoundException, ApiKeyNotFoundException {
    User adminUser = getAdminUser();
    User regularUser = getRegularUser();
    User roleLessUser = getRoleLessUser();

    Mockito.when(userService.getByApiKey(Mockito.eq(ADMIN_USER_API_KEY))).thenReturn(adminUser);
    Mockito.when(userService.getByApiKey(Mockito.eq(REGULAR_USER_API_KEY))).thenReturn(regularUser);
    Mockito.when(userService.getByApiKey(Mockito.eq(ROLELESS_USER_API_KEY)))
        .thenReturn(roleLessUser);
  }

  public static void prepareDefaultDevice(DeviceService deviceService)
      throws DeviceTokenNotFoundException, DeviceNotFoundException {
    Device device = sampleDevice();

    Mockito.when(deviceService.getByDeviceToken(Mockito.eq(VALID_DEVICE_TOKEN))).thenReturn(device);
  }

  public static Device sampleDevice() {
    Device device = new Device();

    device.setId("27b5eaa7-41c4-400f-8273-576a03546427");
    device.setUserId("104fb8f2-dc9f-4656-baf8-0d88f332a221");
    device.setName("Test Device");

    return device;
  }

  public static User getAdminUser() {
    User adminUser = new User();
    adminUser.setId("7e5ceddd-65bc-4ae2-8f39-2d8c20cc6696");
    adminUser.setEmail("admin@localhost");
    adminUser.setRoles(Collections.singletonList(UserRoles.ADMIN));

    return adminUser;
  }

  public static User getRegularUser() {
    User regularUser = new User();
    regularUser.setId("b4571e30-70f9-4766-99ba-eb60f55e6896");
    regularUser.setEmail("regular.user@localhost");
    regularUser.setRoles(Collections.singletonList(UserRoles.USER));

    return regularUser;
  }

  public static User getRoleLessUser() {
    User roleLessUser = new User();
    roleLessUser.setId("538767c2-bec2-4530-93d8-be5ed45165e4");
    roleLessUser.setEmail("roleless.user@localhost");
    roleLessUser.setRoles(new ArrayList<>());

    return roleLessUser;
  }
}
