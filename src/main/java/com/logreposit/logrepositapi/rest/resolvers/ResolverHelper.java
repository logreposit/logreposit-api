package com.logreposit.logrepositapi.rest.resolvers;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;

public class ResolverHelper
{
    private ResolverHelper()
    {
    }

    public static CaseInsensitiveMap<String, String> getHeaders(NativeWebRequest nativeWebRequest)
    {
        CaseInsensitiveMap<String, String> headers = new CaseInsensitiveMap<>();

        nativeWebRequest.getHeaderNames().forEachRemaining(headerName -> extractHeaders(nativeWebRequest, headerName, headers));

        return headers;
    }

    private static void extractHeaders(NativeWebRequest nativeWebRequest, String headerName, CaseInsensitiveMap<String, String> headers)
    {
        if (StringUtils.isBlank(headerName) || StringUtils.isBlank(nativeWebRequest.getHeader(headerName)))
        {
            return;
        }

        String headerValue = nativeWebRequest.getHeader(headerName);

        headers.put(headerName, headerValue);
    }
}
