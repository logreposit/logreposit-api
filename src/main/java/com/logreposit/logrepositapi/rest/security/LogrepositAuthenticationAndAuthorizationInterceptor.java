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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class LogrepositAuthenticationAndAuthorizationInterceptor implements HandlerInterceptor {
  private static final Logger logger =
      LoggerFactory.getLogger(LogrepositAuthenticationAndAuthorizationInterceptor.class);

  private final String apiKeyHeaderName;
  private final String deviceTokenHeaderName;
  private final ObjectMapper objectMapper;
  private final UserService userService;
  private final DeviceService deviceService;

  public LogrepositAuthenticationAndAuthorizationInterceptor(
      String apiKeyHeaderName,
      String deviceTokenHeaderName,
      ObjectMapper objectMapper,
      UserService userService,
      DeviceService deviceService) {
    this.apiKeyHeaderName = apiKeyHeaderName;
    this.deviceTokenHeaderName = deviceTokenHeaderName;
    this.objectMapper = objectMapper;
    this.userService = userService;
    this.deviceService = deviceService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    final var route = request.getRequestURI().toLowerCase();

    if (route.startsWith("/reference/")) {
      return true;
    }

    if (route.startsWith("/ingress")
        || route.startsWith("/v1/ingress")
        || route.startsWith("/v2/ingress/")) {
      return this.handleIngressRequests(request, response);
    }

    return this.handleOtherRequests(request, response, handler);
  }

  private boolean handleIngressRequests(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    final var deviceToken = request.getHeader(this.deviceTokenHeaderName);
    final var route = request.getRequestURI();

    logger.info("Trying to authenticate request => deviceToken: {}, route: {}", deviceToken, route);

    try {
      final var device = this.authenticateDevice(deviceToken);

      logger.info(
          "Successfully authenticated and authorized => deviceToken: {}, route: {}, userId: {}, device: {} ({})",
          deviceToken,
          route,
          device.getUserId(),
          device.getId(),
          device.getName());
    } catch (UnauthenticatedException e) {
      logger.error("Request unauthenticated => deviceToken: {}, route: {}", deviceToken, route);

      return this.sendAuthFailedResponse(
          response,
          "Unauthenticated",
          ErrorCodes.UNAUTHORIZED_INGRESS_REQUEST,
          HttpStatus.UNAUTHORIZED.value());
    }

    return true;
  }

  private boolean handleOtherRequests(
      HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    final var apiKey = request.getHeader(this.apiKeyHeaderName);
    final var route = request.getRequestURI();

    logger.info("Trying to authenticate request => apiKey: {}, route: {}", apiKey, route);

    try {
      final var user = this.authenticateUser(apiKey, route);

      logger.info(
          "Successfully authenticated and authorized => apiKey: {}, route: {}, user: {}",
          apiKey,
          route,
          user);
    } catch (UnauthenticatedException e) {
      logger.error("Request unauthenticated => apiKey: {}, route: {}", apiKey, route);

      return this.sendAuthFailedResponse(
          response,
          "Unauthenticated",
          ErrorCodes.UNAUTHENTICATED_API_REQUEST,
          HttpStatus.UNAUTHORIZED.value());
    } catch (UnauthorizedException e) {
      logger.error("Request unauthorized => apiKey: {}, route: {}", apiKey, route);

      return this.sendAuthFailedResponse(
          response,
          "Unauthorized",
          ErrorCodes.UNAUTHORIZED_API_REQUEST,
          HttpStatus.FORBIDDEN.value());
    }

    return true;
  }

  private User authenticateUser(String apiKey, String route)
      throws UnauthorizedException, UnauthenticatedException {
    if (StringUtils.isBlank(apiKey)) {
      throw new UnauthenticatedException();
    }

    final var user = this.resolveUser(apiKey);

    this.checkRoute(route, user.getRoles());

    return user;
  }

  private Device authenticateDevice(String deviceToken) throws UnauthenticatedException {
    if (StringUtils.isBlank(deviceToken)) {
      throw new UnauthenticatedException();
    }

    return this.resolveDevice(deviceToken);
  }

  private User resolveUser(String apiKey) throws UnauthenticatedException {
    try {
      return this.userService.getByApiKey(apiKey);
    } catch (UserServiceException | ApiKeyNotFoundException e) {
      logger.error("Unable to resolve user using apiKey {}", apiKey);

      throw new UnauthenticatedException("Unable to resolve user using apiKey", e);
    }
  }

  private Device resolveDevice(String deviceToken) throws UnauthenticatedException {
    try {
      return this.deviceService.getByDeviceToken(deviceToken);
    } catch (DeviceNotFoundException | DeviceTokenNotFoundException e) {
      logger.error("Unable to resolve device using deviceToken {}", deviceToken);

      throw new UnauthenticatedException("Unable to resolve device using deviceToken", e);
    }
  }

  private void checkRoute(String route, List<String> roles) throws UnauthorizedException {
    if (route.toLowerCase().startsWith("/v1/admin") && !roles.contains(UserRoles.ADMIN)) {
      logger.error("User does not have sufficient permissions to access admin resources.");

      throw new UnauthorizedException();
    }
  }

  private boolean sendAuthFailedResponse(
      HttpServletResponse response, String errorMessage, int errorCode, int httpCode)
      throws IOException {
    final var errorResponse = ErrorResponse.builder().code(errorCode).message(errorMessage).build();

    final var errorJson = this.objectMapper.writeValueAsString(errorResponse);

    response.setStatus(httpCode);
    response.setContentType(MediaType.APPLICATION_JSON.toString());
    response.getWriter().write(errorJson);
    response.flushBuffer();

    return false;
  }
}
