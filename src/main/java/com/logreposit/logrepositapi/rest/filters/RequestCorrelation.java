package com.logreposit.logrepositapi.rest.filters;

import org.slf4j.MDC;

public class RequestCorrelation
{
    private static final ThreadLocal<String> id = new ThreadLocal<>();

    public static void setCorrelationId(String correlationId)
    {
        id.set(correlationId);

        MDC.put("correlationId", correlationId);
    }

    public static String getCorrelationId()
    {
        return id.get();
    }
}
