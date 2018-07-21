package com.logreposit.logrepositapi.rest.filters;

public class RequestCorrelation
{
    public static final String CORRELATION_ID_HEADER_NAME = "x-correlation-id"; // TODO: USEFUL for other services! this one is the edge service

    private static final ThreadLocal<String> id = new ThreadLocal<>();

    public static void setCorrelationId(String correlationId)
    {
        id.set(correlationId);
    }

    public static String getCorrelationId()
    {
        return id.get();
    }
}
