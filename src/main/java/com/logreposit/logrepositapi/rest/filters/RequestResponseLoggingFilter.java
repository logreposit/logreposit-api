package com.logreposit.logrepositapi.rest.filters;

import com.logreposit.logrepositapi.rest.filters.clientinfo.ClientInfo;
import com.logreposit.logrepositapi.rest.filters.clientinfo.ClientInfoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2)
public class RequestResponseLoggingFilter implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig)
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest  httpServletRequest  = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        ClientInfo          clientInfo          = ClientInfoFactory.extract(httpServletRequest);

        logger.info("Request: {} {} [{}, {}]", httpServletRequest.getMethod(), httpServletRequest.getRequestURI(), clientInfo.getIpAddress(), clientInfo.getOperatingSystem());
        logger.info("Full client-info: {}", clientInfo);

        filterChain.doFilter(servletRequest, servletResponse);

        int    responseStatus = httpServletResponse.getStatus();
        String httpStatus     = getHttpStatusAsString(responseStatus);

        if (responseStatus < 200 || responseStatus > 299)
        {
            logger.info("Response: {} {}", responseStatus, httpStatus);
        }
        else
        {
            logger.error("Response: {} {}", responseStatus, httpStatus);
        }
    }

    @Override
    public void destroy()
    {
    }

    private static String getHttpStatusAsString(int statusCode)
    {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);

        if (httpStatus == null)
        {
            return "";
        }

        return httpStatus.toString();
    }
}
