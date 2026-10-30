package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

class RequestSizeFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;
    private final long maxBytes;

    RequestSizeFilter(HandlerExceptionResolver resolver, long maxBytes) {
        this.resolver = resolver;
        this.maxBytes = maxBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > maxBytes) {
            resolver.resolveException(request, response, null, new ApiException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "PAYLOAD_TOO_LARGE", "Request too large"));
            return;
        }
        chain.doFilter(request, response);
    }
}
