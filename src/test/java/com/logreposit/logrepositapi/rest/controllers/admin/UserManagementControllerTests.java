package com.logreposit.logrepositapi.rest.controllers.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.rest.controllers.ControllerTestUtils;
import com.logreposit.logrepositapi.rest.dtos.request.UserCreationRequestDto;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.CreatedUser;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {UserManagementController.class})
public class UserManagementControllerTests
{
    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private MockMvc controller;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws UserNotFoundException, ApiKeyNotFoundException
    {
        ControllerTestUtils.prepareDefaultUsers(this.userService);
    }

    @Test
    public void testCreate() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            CreatedUser createdUser = new CreatedUser(firstArgument, new ApiKey());

            return createdUser;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isCreated())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").isString())
                       .andExpect(jsonPath("$.data.email").value(userCreationRequestDto.getEmail()));
    }

    @Test
    public void testCreate_unauthenticated() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70001))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testCreate_unauthenticated_invalidKey() throws Exception
    {
        String invalidApiKey = UUID.randomUUID().toString();

        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, invalidApiKey)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        Mockito.when(this.userService.getByApiKey(Mockito.eq(invalidApiKey))).thenThrow(new ApiKeyNotFoundException(""));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70001))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testCreate_unauthenticated_noUserForKey() throws Exception
    {
        String invalidApiKey = UUID.randomUUID().toString();

        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, invalidApiKey)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        Mockito.when(this.userService.getByApiKey(Mockito.eq(invalidApiKey))).thenThrow(new UserNotFoundException(""));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isUnauthorized())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70001))
                       .andExpect(jsonPath("$.message").value("Unauthenticated"));
    }

    @Test
    public void testCreate_unauthorized_regularUser() throws Exception
    {
        this.testCreate_unauthorized(ControllerTestUtils.REGULAR_USER_API_KEY);
    }

    @Test
    public void testCreate_unauthorized_roleLessUser() throws Exception
    {
        this.testCreate_unauthorized(ControllerTestUtils.ROLELESS_USER_API_KEY);
    }

    private void testCreate_unauthorized(String apiKey) throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, apiKey)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isForbidden())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(70002))
                       .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    public void testCreate_alreadyExistent() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String userCreationRequestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenThrow(new UserAlreadyExistentException("", userCreationRequestDto.getEmail()));

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isConflict())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(10002))
                       .andExpect(jsonPath("$.message").value("User resource with given email address is already existent. Please choose another email."));
    }

    @Test
    public void testList() throws Exception
    {
        int defaultPageNumber = 0;
        int defaultPageSize   = 10;

        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail(UUID.randomUUID().toString() + "@local");
        user1.setRoles(Collections.singletonList(UserRoles.ADMIN));

        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail(UUID.randomUUID().toString() + "@local");
        user2.setRoles(Collections.singletonList(UserRoles.USER));

        List<User> users    = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users);

        Mockito.when(this.userService.list(Mockito.anyInt(), Mockito.anyInt())).thenReturn(userPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.totalElements").value(2))
                       .andExpect(jsonPath("$.data.totalPages").value(1))
                       .andExpect(jsonPath("$.data.items").isArray())
                       .andExpect(jsonPath("$.data.items.length()").value(2))
                       .andExpect(jsonPath("$.data.items[0].id").value(user1.getId()))
                       .andExpect(jsonPath("$.data.items[0].email").value(user1.getEmail()))
                       .andExpect(jsonPath("$.data.items[0].roles[0]").value(user1.getRoles().get(0)))
                       .andExpect(jsonPath("$.data.items[1].id").value(user2.getId()))
                       .andExpect(jsonPath("$.data.items[1].email").value(user2.getEmail()))
                       .andExpect(jsonPath("$.data.items[1].roles[0]").value(user2.getRoles().get(0)));

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.userService, Mockito.times(1)).list(pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer pageNumber = pageNumberArgumentCaptor.getValue();
        Integer pageSize   = pageSizeArgumentCaptor.getValue();

        assertThat(pageNumber).isNotNull();
        assertThat(pageSize).isNotNull();
        assertThat(pageNumber.intValue()).isEqualTo(defaultPageNumber);
        assertThat(pageSize.intValue()).isEqualTo(defaultPageSize);
    }

    @Test
    public void testList_customPaginationSettings() throws Exception
    {
        int pageNumber = 1;
        int pageSize   = 8;

        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setEmail(UUID.randomUUID().toString() + "@local");
        user1.setRoles(Collections.singletonList(UserRoles.ADMIN));

        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setEmail(UUID.randomUUID().toString() + "@local");
        user2.setRoles(Collections.singletonList(UserRoles.USER));

        List<User> users    = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users);

        Mockito.when(this.userService.list(Mockito.anyInt(), Mockito.anyInt())).thenReturn(userPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.totalElements").value(2))
                       .andExpect(jsonPath("$.data.totalPages").value(1))
                       .andExpect(jsonPath("$.data.items").isArray())
                       .andExpect(jsonPath("$.data.items.length()").value(2))
                       .andExpect(jsonPath("$.data.items[0].id").value(user1.getId()))
                       .andExpect(jsonPath("$.data.items[0].email").value(user1.getEmail()))
                       .andExpect(jsonPath("$.data.items[0].roles[0]").value(user1.getRoles().get(0)))
                       .andExpect(jsonPath("$.data.items[1].id").value(user2.getId()))
                       .andExpect(jsonPath("$.data.items[1].email").value(user2.getEmail()))
                       .andExpect(jsonPath("$.data.items[1].roles[0]").value(user2.getRoles().get(0)));

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.userService, Mockito.times(1)).list(pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer capturedPageNumber = pageNumberArgumentCaptor.getValue();
        Integer capturedPageSize   = pageSizeArgumentCaptor.getValue();

        assertThat(pageNumber).isNotNull();
        assertThat(pageSize).isNotNull();
        assertThat(capturedPageNumber.intValue()).isEqualTo(pageNumber);
        assertThat(capturedPageSize.intValue()).isEqualTo(pageSize);
    }

    @Test
    public void testList_customPaginationSettings_exceedsLimits() throws Exception
    {
        int pageNumber = -1;
        int pageSize   = 40;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/admin/users?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80016))
                       .andExpect(jsonPath("$.message").value(containsString("list.size: size must be less or equal than 25")))
                       .andExpect(jsonPath("$.message").value(containsString("list.page: page must be greater than or equal to 0")));
    }

    @Test
    public void testCreate_unCompletePayload() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(UUID.randomUUID().toString() + "@localhost");

        String requestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        requestDtoSerialized = requestDtoSerialized.replace(requestDtoSerialized.substring(requestDtoSerialized.length() - 1), "");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(requestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));
    }

    @Test
    public void testCreate_missingValuesInPayload() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail(null);
        userCreationRequestDto.setPassword("highLySecUr3_somePassword");

        String requestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(requestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: email -> must not be null (actual value: null) => Please check your input."));
    }

    @Test
    public void testCreate_wrongValueTypesInPayload() throws Exception
    {
        String userCreationRequestDtoJson = "{\"email\": 170.25, \"password\": \"superPassword\"}";

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(userCreationRequestDtoJson);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80005))
                       .andExpect(jsonPath("$.message").value("Invalid input data. Field Errors: email -> must be a well-formed email address (actual value: 170.25) => Please check your input."));
    }

    @Test
    public void testCreate_noPayload() throws Exception
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80004))
                       .andExpect(jsonPath("$.message").value("Request could not be processed. Please check if the JSON syntax is valid."));
    }

    @Test
    public void testCreate_wrongContentType() throws Exception
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .contentType(MediaType.APPLICATION_XML)
                                                                      .content("");

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isBadRequest())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80002))
                       .andExpect(jsonPath("$.message").value("Given MediaType 'application/xml' is not supported. Supported MediaTypes are: application/json, application/octet-stream, application/xml, application/*+json, text/plain, text/xml, application/x-www-form-urlencoded, application/*+xml, multipart/form-data, multipart/mixed, */*"));
    }

    @Test
    public void testCreate_invalidAcceptHeader() throws Exception
    {
        UserCreationRequestDto userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setEmail("superemail@localhost");

        String requestDtoSerialized = this.objectMapper.writeValueAsString(userCreationRequestDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/admin/users")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY)
                                                                      .accept(MediaType.APPLICATION_XML)
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .content(requestDtoSerialized);

        Mockito.when(this.userService.create(Mockito.any(User.class))).thenAnswer(i -> {
            User firstArgument = (User) i.getArguments()[0];

            firstArgument.setId(UUID.randomUUID().toString());

            return firstArgument;
        });

        // Somehow the response from the exception handler is not printed out because the client assumes to get some XML and our response format is JSON.
        // Going with default now: 406 from spring

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotAcceptable());
    }

    @Test
    public void testNotExistentRoute() throws Exception
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.ADMIN_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotFound())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(80001))
                       .andExpect(jsonPath("$.message").value("Given route is not existent."));
    }
}
