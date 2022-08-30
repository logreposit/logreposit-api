package com.logreposit.logrepositapi.rest.filters.clientinfo;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public class ClientInfoFactory {
  private static final List<String> CLIENT_IP_ADDRESS_HEADERS =
      List.of(
          "X-Forwarded-For",
          "Proxy-Client-IP",
          "WL-Proxy-Client-IP",
          "HTTP_CLIENT_IP",
          "HTTP_X_FORWARDED_FOR");

  private ClientInfoFactory() {}

  public static ClientInfo extract(HttpServletRequest request) {
    final String referer = getReferer(request);
    final String fullUrl = getFullURL(request);
    final String ipAddress = getClientIpAddress(request);
    final String userAgent = getUserAgent(request);

    return ClientInfo.builder()
        .referer(referer)
        .fullUrl(fullUrl)
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .build();
  }

  private static String getReferer(HttpServletRequest request) {
    return request.getHeader("referer");
  }

  private static String getFullURL(HttpServletRequest request) {
    final var requestUrl = request.getRequestURL();
    final var queryString = request.getQueryString();

    if (StringUtils.isEmpty(queryString)) {
      return requestUrl.toString();
    }

    return requestUrl.append('?').append(queryString).toString();
  }

  private static String getClientIpAddress(HttpServletRequest request) {
    return CLIENT_IP_ADDRESS_HEADERS.stream()
        .map(request::getHeader)
        .filter(Objects::nonNull)
        .filter(not("unknown"::equalsIgnoreCase))
        .findFirst()
        .orElse(request.getRemoteAddr());
  }

  private static String getUserAgent(HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }
}
