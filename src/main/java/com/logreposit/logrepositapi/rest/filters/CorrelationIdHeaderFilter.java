package com.logreposit.logrepositapi.rest.filters;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
