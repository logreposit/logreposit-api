package com.logreposit.logrepositapi.rest.resolvers;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.services.user.UserService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class UserResolver implements HandlerMethodArgumentResolver {
  private final String apiKeyHeaderName;
  private final UserService userService;

  public UserResolver(String apiKeyHeaderName, UserService userService) {
    this.apiKeyHeaderName = apiKeyHeaderName;
    this.userService = userService;
  }

  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return methodParameter.getParameterType().equals(User.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest,
      WebDataBinderFactory webDataBinderFactory)
      throws Exception {
    final var headers = ResolverHelper.getHeaders(nativeWebRequest);
    final var apiKey = this.getApiKeyFromHeaders(headers);

    return this.userService.getByApiKey(apiKey);
  }

  private String getApiKeyFromHeaders(CaseInsensitiveMap<String, String> headers)
      throws ServletRequestBindingException {
    final var apiKey = headers.get(this.apiKeyHeaderName);

    if (StringUtils.isBlank(apiKey)) {
      throw new ServletRequestBindingException(
          String.format("Missing request header '%s' of type String", this.apiKeyHeaderName));
    }

    return apiKey;
  }
}
