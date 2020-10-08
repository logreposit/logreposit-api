package com.logreposit.logrepositapi.rest.controllers;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.configuration.LogrepositWebMvcConfiguration;
import com.logreposit.logrepositapi.services.apikey.ApiKeyService;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {ApiKeyController.class})
public class ApiKeyControllerTests
{
    @MockBean
    private ApiKeyService apiKeyService;

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private MockMvc controller;

    @Before
    public void setUp() throws UserNotFoundException, ApiKeyNotFoundException
    {
        ControllerTestUtils.prepareDefaultUsers(this.userService);
    }

    @Test
    public void testCreate() throws Exception
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/account/api-keys")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        User   regularUser = ControllerTestUtils.getRegularUser();
        ApiKey apiKey      = sampleApiKey(regularUser.getId());

        Mockito.when(this.apiKeyService.create(Mockito.eq(regularUser.getId()))).thenReturn(apiKey);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isCreated())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(apiKey.getId()))
                       .andExpect(jsonPath("$.data.key").value(apiKey.getKey()))
                       .andExpect(jsonPath("$.data.createdAt").exists());

        Mockito.verify(this.apiKeyService, Mockito.times(1)).create(Mockito.eq(regularUser.getId()));
    }

    @Test
    public void testList() throws Exception
    {
        int defaultPageNumber = 0;
        int defaultPageSize   = 10;

        User regularUser = ControllerTestUtils.getRegularUser();

        ApiKey apiKey1 = sampleApiKey(regularUser.getId());
        ApiKey apiKey2 = sampleApiKey(regularUser.getId());

        List<ApiKey> apiKeys    = Arrays.asList(apiKey1, apiKey2);
        Page<ApiKey> apiKeyPage = new PageImpl<>(apiKeys);

        Mockito.when(this.apiKeyService.list(Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(apiKeyPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account/api-keys")
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

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
                       .andExpect(jsonPath("$.data.items[0].id").value(apiKey1.getId()))
                       .andExpect(jsonPath("$.data.items[0].key").value(apiKey1.getKey()))
                       .andExpect(jsonPath("$.data.items[0].createdAt").exists())
                       .andExpect(jsonPath("$.data.items[1].id").value(apiKey2.getId()))
                       .andExpect(jsonPath("$.data.items[1].key").value(apiKey2.getKey()))
                       .andExpect(jsonPath("$.data.items[1].createdAt").exists());

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.apiKeyService, Mockito.times(1)).list(Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer pageNumber = pageNumberArgumentCaptor.getValue();
        Integer pageSize   = pageSizeArgumentCaptor.getValue();

        Assert.assertNotNull(pageNumber);
        Assert.assertNotNull(pageSize);
        Assert.assertEquals(defaultPageNumber, pageNumber.intValue());
        Assert.assertEquals(defaultPageSize, pageSize.intValue());
    }

    @Test
    public void testList_customPaginationSettings() throws Exception
    {
        int pageNumber = 1;
        int pageSize   = 8;

        User regularUser = ControllerTestUtils.getRegularUser();

        ApiKey apiKey1 = sampleApiKey(regularUser.getId());
        ApiKey apiKey2 = sampleApiKey(regularUser.getId());

        List<ApiKey> apiKeys    = Arrays.asList(apiKey1, apiKey2);
        Page<ApiKey> apiKeyPage = new PageImpl<>(apiKeys);

        Mockito.when(this.apiKeyService.list(Mockito.eq(regularUser.getId()), Mockito.anyInt(), Mockito.anyInt())).thenReturn(apiKeyPage);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account/api-keys?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

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
                       .andExpect(jsonPath("$.data.items[0].id").value(apiKey1.getId()))
                       .andExpect(jsonPath("$.data.items[0].key").value(apiKey1.getKey()))
                       .andExpect(jsonPath("$.data.items[0].createdAt").exists())
                       .andExpect(jsonPath("$.data.items[1].id").value(apiKey2.getId()))
                       .andExpect(jsonPath("$.data.items[1].key").value(apiKey2.getKey()))
                       .andExpect(jsonPath("$.data.items[1].createdAt").exists());

        ArgumentCaptor<Integer> pageNumberArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeArgumentCaptor   = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(this.apiKeyService, Mockito.times(1)).list(Mockito.eq(regularUser.getId()), pageNumberArgumentCaptor.capture(), pageSizeArgumentCaptor.capture());

        Integer capturedPageNumber = pageNumberArgumentCaptor.getValue();
        Integer capturedPageSize   = pageSizeArgumentCaptor.getValue();

        Assert.assertNotNull(capturedPageNumber);
        Assert.assertNotNull(capturedPageSize);
        Assert.assertEquals(pageNumber, capturedPageNumber.intValue());
        Assert.assertEquals(pageSize, capturedPageSize.intValue());
    }

    @Test
    public void testList_customPaginationSettings_exceedsLimits() throws Exception
    {
        int pageNumber = -1;
        int pageSize   = 40;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account/api-keys?page=" + pageNumber + "&size=" + pageSize)
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

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
    public void testGet() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        ApiKey apiKey      = sampleApiKey(regularUser.getId());

        Mockito.when(this.apiKeyService.get(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()))).thenReturn(apiKey);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account/api-keys/" + apiKey.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(apiKey.getId()))
                       .andExpect(jsonPath("$.data.key").value(apiKey.getKey()))
                       .andExpect(jsonPath("$.data.createdAt").exists());

        Mockito.verify(this.apiKeyService, Mockito.times(1)).get(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()));
    }

    @Test
    public void testGet_noSuchKey() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        ApiKey apiKey      = sampleApiKey(regularUser.getId());

        Mockito.when(this.apiKeyService.get(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()))).thenThrow(new ApiKeyNotFoundException(""));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/account/api-keys/" + apiKey.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotFound())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(20001))
                       .andExpect(jsonPath("$.message").value("Given api-key resource not found."));
    }

    @Test
    public void testDelete() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        ApiKey apiKey      = sampleApiKey(regularUser.getId());

        Mockito.when(this.apiKeyService.delete(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()))).thenReturn(apiKey);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/account/api-keys/" + apiKey.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isOk())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("SUCCESS"))
                       .andExpect(jsonPath("$.data").exists())
                       .andExpect(jsonPath("$.data.id").value(apiKey.getId()))
                       .andExpect(jsonPath("$.data.key").value(apiKey.getKey()))
                       .andExpect(jsonPath("$.data.createdAt").exists());

        Mockito.verify(this.apiKeyService, Mockito.times(1)).delete(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()));
    }

    @Test
    public void testDelete_noSuchKey() throws Exception
    {
        User   regularUser = ControllerTestUtils.getRegularUser();
        ApiKey apiKey      = sampleApiKey(regularUser.getId());

        Mockito.when(this.apiKeyService.delete(Mockito.eq(apiKey.getId()), Mockito.eq(regularUser.getId()))).thenThrow(new ApiKeyNotFoundException(""));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/account/api-keys/" + apiKey.getId())
                                                                      .header(LogrepositWebMvcConfiguration.API_KEY_HEADER_NAME, ControllerTestUtils.REGULAR_USER_API_KEY);

        this.controller.perform(request)
                       .andDo(MockMvcResultHandlers.print())
                       .andExpect(status().isNotFound())
                       .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(jsonPath("$.correlationId").isString())
                       .andExpect(jsonPath("$.status").value("ERROR"))
                       .andExpect(jsonPath("$.code").value(20001))
                       .andExpect(jsonPath("$.message").value("Given api-key resource not found."));
    }

    private static ApiKey sampleApiKey(String userId)
    {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID().toString());
        apiKey.setKey(UUID.randomUUID().toString());
        apiKey.setUserId(userId);
        apiKey.setCreatedAt(new Date());

        return apiKey;
    }
}
