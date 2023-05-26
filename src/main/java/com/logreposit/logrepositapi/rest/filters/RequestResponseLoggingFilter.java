package com.logreposit.logrepositapi.rest.filters;

import com.logreposit.logrepositapi.rest.filters.clientinfo.ClientInfoFactory;
import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class RequestResponseLoggingFilter implements Filter {
  private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    final var httpServletRequest = (HttpServletRequest) servletRequest;
    final var httpServletResponse = (HttpServletResponse) servletResponse;
    final var clientInfo = ClientInfoFactory.extract(httpServletRequest);

    logger.info(
        "Request: {} {} [{}, {}]",
        httpServletRequest.getMethod(),
        httpServletRequest.getRequestURI(),
        clientInfo.getIpAddress(),
        clientInfo.getUserAgent());
    logger.info("Full client-info: {}", clientInfo);

    filterChain.doFilter(servletRequest, servletResponse);

    final var responseStatus = httpServletResponse.getStatus();

    final var httpStatus =
        Optional.ofNullable(HttpStatus.resolve(responseStatus))
            .map(HttpStatus::getReasonPhrase)
            .orElse("");

    if (responseStatus < 200 || responseStatus > 299) {
      logger.error("Response: {} {}", responseStatus, httpStatus);
    } else {
      logger.info("Response: {} {}", responseStatus, httpStatus);
    }
  }
}
