package com.logreposit.logrepositapi.rest.filters.clientinfo;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class ClientInfoFactory
{
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

    //http://stackoverflow.com/a/18030465/1845894
    private static String getClientIpAddress(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private static String getUserAgent(HttpServletRequest request)
    {
        return request.getHeader("User-Agent");
    }
}
