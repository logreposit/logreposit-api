package com.logreposit.logrepositapi.rest.resolvers;

import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.services.device.DeviceService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class DeviceResolver implements HandlerMethodArgumentResolver {
  private final String deviceTokenHeaderName;
  private final DeviceService deviceService;

  public DeviceResolver(String deviceTokenHeaderName, DeviceService deviceService) {
    this.deviceTokenHeaderName = deviceTokenHeaderName;
    this.deviceService = deviceService;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(Device.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest,
      WebDataBinderFactory webDataBinderFactory)
      throws Exception {
    final var headers = ResolverHelper.getHeaders(nativeWebRequest);

    final var deviceToken = this.getDeviceTokenFromHeaders(headers);

    return this.deviceService.getByDeviceToken(deviceToken);
  }

  private String getDeviceTokenFromHeaders(CaseInsensitiveMap<String, String> headers)
      throws ServletRequestBindingException {
    final var apiKey = headers.get(this.deviceTokenHeaderName);

    if (StringUtils.isBlank(apiKey)) {
      throw new ServletRequestBindingException(
          String.format("Missing request header '%s' of type String", this.deviceTokenHeaderName));
    }

    return apiKey;
  }
}
