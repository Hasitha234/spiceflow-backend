package com.spiceflow.backend.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Attaches a unique requestId to every log line via MDC. Must run first — Order(1). */
@Component
@Order(1)
public class MdcLoggingFilter implements Filter {

  private static final String REQUEST_ID_KEY = "requestId";
  private static final String REQUEST_ID_HEADER = "X-Request-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String requestId = UUID.randomUUID().toString();
    try {
      MDC.put(REQUEST_ID_KEY, requestId);
      if (response instanceof HttpServletResponse httpResponse) {
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
      }
      chain.doFilter(request, response);
    } finally {
      MDC.clear(); // ALWAYS clear — thread is returned to the pool and reused
    }
  }
}
