package com.logreposit.logrepositapi.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import com.logreposit.logrepositapi.rest.error.ErrorCodes;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceService;
import com.logreposit.logrepositapi.services.user.UserService;
import com.logreposit.logrepositapi.services.user.UserServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public class LogrepositAuthenticationAndAuthorizationInterceptor extends HandlerInterceptorAdapter
{
    private static final Logger logger = LoggerFactory.getLogger(LogrepositAuthenticationAndAuthorizationInterceptor.class);

    private final String        apiKeyHeaderName;
    private final String        deviceTokenHeaderName;
    private final ObjectMapper  objectMapper;
    private final UserService   userService;
    private final DeviceService deviceService;

    public LogrepositAuthenticationAndAuthorizationInterceptor(String apiKeyHeaderName,
                                                               String deviceTokenHeaderName,
                                                               ObjectMapper objectMapper,
                                                               UserService userService,
                                                               DeviceService deviceService)
    {
        this.apiKeyHeaderName      = apiKeyHeaderName;
        this.deviceTokenHeaderName = deviceTokenHeaderName;
        this.objectMapper          = objectMapper;
        this.userService           = userService;
        this.deviceService         = deviceService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String route = request.getRequestURI().toLowerCase();

        if (route.startsWith("/reference/"))
        {
            return super.preHandle(request, response, handler);
        }

        if (route.startsWith("/ingress") || route.startsWith("/v1/ingress") || route.startsWith("/v2/ingress/"))
        {
            return this.handleIngressRequests(request, response, handler);
        }

        return this.handleOtherRequests(request, response, handler);
    }

    private boolean handleIngressRequests(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String deviceToken = request.getHeader(this.deviceTokenHeaderName);
        String route       = request.getRequestURI();

        logger.info("Trying to authenticate request => deviceToken: {}, route: {}", deviceToken, route);

        try
        {
            Device device = this.authenticateDevice(deviceToken);
            logger.info("Successfully authenticated and authorized => deviceToken: {}, route: {}, device: {}", deviceToken, route, device);
        }
        catch (UnauthenticatedException e)
        {
            logger.error("Request unauthenticated => deviceToken: {}, route: {}", deviceToken, route);
            return this.sendAuthFailedResponse(response, "Unauthenticated", ErrorCodes.UNAUTHORIZED_INGRESS_REQUEST, HttpStatus.UNAUTHORIZED.value());
        }

        return super.preHandle(request, response, handler);
    }

    private boolean handleOtherRequests(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String apiKey = request.getHeader(this.apiKeyHeaderName);
        String route  = request.getRequestURI();

        logger.info("Trying to authenticate request => apiKey: {}, route: {}", apiKey, route);

        try
        {
            User user = this.authenticateUser(apiKey, route);
            logger.info("Successfully authenticated and authorized => apiKey: {}, route: {}, user: {}", apiKey, route, user);
        }
        catch (UnauthenticatedException e)
        {
            logger.error("Request unauthenticated => apiKey: {}, route: {}", apiKey, route);
            return this.sendAuthFailedResponse(response, "Unauthenticated", ErrorCodes.UNAUTHENTICATED_API_REQUEST, HttpStatus.UNAUTHORIZED.value());
        }
        catch (UnauthorizedException e)
        {
            logger.error("Request unauthorized => apiKey: {}, route: {}", apiKey, route);
            return this.sendAuthFailedResponse(response, "Unauthorized", ErrorCodes.UNAUTHORIZED_API_REQUEST, HttpStatus.FORBIDDEN.value());
        }

        return super.preHandle(request, response, handler);
    }

    private User authenticateUser(String apiKey, String route) throws UnauthorizedException, UnauthenticatedException
    {
        if (StringUtils.isBlank(apiKey))
        {
            throw new UnauthenticatedException();
        }

        User user = this.resolveUser(apiKey);

        this.checkRoute(route, user.getRoles());

        return user;
    }

    private Device authenticateDevice(String deviceToken) throws UnauthenticatedException
    {
        if (StringUtils.isBlank(deviceToken))
        {
            throw new UnauthenticatedException();
        }

        Device device = this.resolveDevice(deviceToken);

        return device;
    }

    private User resolveUser(String apiKey) throws UnauthenticatedException
    {
        try
        {
            User user = this.userService.getByApiKey(apiKey);

            return user;
        }
        catch (UserServiceException | ApiKeyNotFoundException e)
        {
            logger.error("Unable to resolve user using apiKey {}", apiKey);
            throw new UnauthenticatedException("Unable to resolve user using apiKey", e);
        }
    }

    private Device resolveDevice(String deviceToken) throws UnauthenticatedException
    {
        try
        {
            Device device = this.deviceService.getByDeviceToken(deviceToken);

            return device;
        }
        catch (DeviceNotFoundException | DeviceTokenNotFoundException e)
        {
            logger.error("Unable to resolve device using deviceToken {}", deviceToken);
            throw new UnauthenticatedException("Unable to resolve device using deviceToken", e);
        }
    }

    private void checkRoute(String route, List<String> roles) throws UnauthorizedException
    {
        if (route.toLowerCase().startsWith("/v1/admin") && !roles.contains(UserRoles.ADMIN))
        {
            logger.error("User does not have sufficient permissions to access admin resources.");
            throw new UnauthorizedException();
        }
    }

    private boolean sendAuthFailedResponse(HttpServletResponse response, String errorMessage, int errorCode, int httpCode) throws IOException
    {
        ErrorResponse errorResponse = ErrorResponse.builder().code(errorCode).message(errorMessage).build();
        String        errorJson     = this.objectMapper.writeValueAsString(errorResponse);

        response.setStatus(httpCode);
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(errorJson);
        response.flushBuffer();

        return false;
    }
}
