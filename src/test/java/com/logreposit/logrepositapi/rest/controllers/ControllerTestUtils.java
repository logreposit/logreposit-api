package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class ControllerTestUtils
{
    public static final String ADMIN_USER_API_KEY    = "ef05b0b1-89d0-446f-bfb2-81974143dc8a";
    public static final String REGULAR_USER_API_KEY  = "74c3e9df-8346-4c34-a96e-64708bfbe163";
    public static final String ROLELESS_USER_API_KEY = "5c9e7f20-92c9-4c08-81ee-c892b491bb89";

    public static void prepareDefaultUsers(UserService userService) throws UserNotFoundException, ApiKeyNotFoundException
    {
        User adminUser    = getAdminUser();
        User regularUser  = getRegularUser();
        User roleLessUser = getRoleLessUser();

        Mockito.when(userService.getByApiKey(Mockito.eq(ADMIN_USER_API_KEY))).thenReturn(adminUser);
        Mockito.when(userService.getByApiKey(Mockito.eq(REGULAR_USER_API_KEY))).thenReturn(regularUser);
        Mockito.when(userService.getByApiKey(Mockito.eq(ROLELESS_USER_API_KEY))).thenReturn(roleLessUser);
    }

    public static User getAdminUser()
    {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setEmail("admin@localhost");
        adminUser.setRoles(Collections.singletonList(UserRoles.ADMIN));

        return adminUser;
    }

    public static User getRegularUser()
    {
        User regularUser = new User();
        regularUser.setId("b4571e30-70f9-4766-99ba-eb60f55e6896");
        regularUser.setEmail("regular.user@localhost");
        regularUser.setRoles(Collections.singletonList(UserRoles.USER));

        return regularUser;
    }

    public static User getRoleLessUser()
    {
        User roleLessUser = new User();
        roleLessUser.setId("538767c2-bec2-4530-93d8-be5ed45165e4");
        roleLessUser.setEmail("roleless.user@localhost");
        roleLessUser.setRoles(new ArrayList<>());

        return roleLessUser;
    }
}
