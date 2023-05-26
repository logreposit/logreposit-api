package com.logreposit.logrepositapi.rest.filters;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CorrelationIdHeaderFilter implements Filter {
  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    if (!currentRequestIsAsyncDispatcher((HttpServletRequest) servletRequest)) {
      RequestCorrelation.setCorrelationId(UUID.randomUUID().toString());
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  private static boolean currentRequestIsAsyncDispatcher(HttpServletRequest httpServletRequest) {
    return httpServletRequest.getDispatcherType().equals(DispatcherType.ASYNC);
  }
}
