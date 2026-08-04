package com.suraj.embeddingservice.filter;

import com.suraj.embeddingservice.common.HeaderNames;
import com.suraj.embeddingservice.common.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveHeader(request, HeaderNames.CORRELATION_ID);
        String requestId = resolveHeader(request, HeaderNames.REQUEST_ID);
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.REQUEST_ID, requestId);
        response.setHeader(HeaderNames.CORRELATION_ID, correlationId);
        response.setHeader(HeaderNames.REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.REQUEST_ID);
        }
    }

    private String resolveHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value;
    }
}
