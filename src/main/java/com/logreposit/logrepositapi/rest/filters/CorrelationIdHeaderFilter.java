package com.logreposit.logrepositapi.rest.filters;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdHeaderFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig)
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        if (!currentRequestIsAsyncDispatcher((HttpServletRequest) servletRequest))
        {
            RequestCorrelation.setCorrelationId(UUID.randomUUID().toString());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy()
    {

    }

    private static boolean currentRequestIsAsyncDispatcher(HttpServletRequest httpServletRequest)
    {
        return httpServletRequest.getDispatcherType().equals(DispatcherType.ASYNC);
    }
}
