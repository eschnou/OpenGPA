package org.opengpa.server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AwsXRayFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String traceId = request.getHeader("X-Amzn-Trace-Id");
            if (traceId != null) {
                MDC.put("awsTraceId", traceId);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}