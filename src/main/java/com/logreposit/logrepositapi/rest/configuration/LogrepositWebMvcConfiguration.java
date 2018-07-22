package com.logreposit.logrepositapi.rest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.rest.resolvers.DeviceResolver;
import com.logreposit.logrepositapi.rest.resolvers.UserResolver;
import com.logreposit.logrepositapi.rest.security.LogrepositAuthenticationAndAuthorizationInterceptor;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class LogrepositWebMvcConfiguration implements WebMvcConfigurer
{
    public static final String API_KEY_HEADER_NAME      = "x-api-key";
    public static final String DEVICE_TOKEN_HEADER_NAME = "x-device-token";

    private final ObjectMapper  objectMapper;
    private final UserService   userService;
    private final DeviceService deviceService;

    public LogrepositWebMvcConfiguration(ObjectMapper objectMapper, UserService userService, DeviceService deviceService)
    {
        this.objectMapper  = objectMapper;
        this.userService   = userService;
        this.deviceService = deviceService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        LogrepositAuthenticationAndAuthorizationInterceptor apiKeyCheckInterceptor = new LogrepositAuthenticationAndAuthorizationInterceptor(
                API_KEY_HEADER_NAME,
                DEVICE_TOKEN_HEADER_NAME,
                this.objectMapper,
                this.userService,
                this.deviceService
        );

        registry.addInterceptor(apiKeyCheckInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
    {
        UserResolver   userResolver   = new UserResolver(API_KEY_HEADER_NAME, this.userService);
        DeviceResolver deviceResolver = new DeviceResolver(DEVICE_TOKEN_HEADER_NAME, this.deviceService);

        resolvers.add(userResolver);
        resolvers.add(deviceResolver);
    }
}
