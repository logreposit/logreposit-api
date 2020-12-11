package com.logreposit.logrepositapi.rest.filters.clientinfo;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

import static java.util.function.Predicate.not;

public class ClientInfoFactory
{
    private static final List<String> CLIENT_IP_ADDRESS_HEADERS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    private ClientInfoFactory()
    {
    }

    public static ClientInfo extract(HttpServletRequest request)
    {
        final String referer   = getReferer(request);
        final String fullUrl   = getFullURL(request);
        final String ipAddress = getClientIpAddress(request);
        final String userAgent = getUserAgent(request);

        ClientInfo clientInfo = ClientInfo.builder()
                                          .referer(referer)
                                          .fullUrl(fullUrl)
                                          .ipAddress(ipAddress)
                                          .userAgent(userAgent)
                                          .build();

        return clientInfo;
    }

    private static String getReferer(HttpServletRequest request)
    {
        final String referer = request.getHeader("referer");

        return referer;
    }

    private static String getFullURL(HttpServletRequest request)
    {
        final StringBuffer requestUrl  = request.getRequestURL();
        final String       queryString = request.getQueryString();

        if (StringUtils.isEmpty(queryString))
        {
            return requestUrl.toString();
        }

        String url = requestUrl.append('?').append(queryString).toString();

        return url;
    }

    private static String getClientIpAddress(HttpServletRequest request)
    {
        return CLIENT_IP_ADDRESS_HEADERS.stream()
                                        .map(request::getHeader)
                                        .filter(Objects::nonNull)
                                        .filter(not("unknown"::equalsIgnoreCase))
                                        .findFirst()
                                        .orElse(request.getRemoteAddr());
    }

    private static String getUserAgent(HttpServletRequest request)
    {
        return request.getHeader("User-Agent");
    }
}
