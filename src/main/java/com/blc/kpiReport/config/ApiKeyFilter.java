package com.blc.kpiReport.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.blc.kpiReport.config.SecurityConfiguration.API_KEY_HEADER;

/**
 * Filter class to handle API key authentication for requests.
 * This filter checks for the presence of an API key in the request header and validates it against
 * the configured API key token. Requests to specific endpoints are filtered for authentication.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${spring.application.api-key}")
    private String apiKeyToken;
    private final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);
    private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/pub/**");

    /**
     * Handles the filtering of incoming HTTP requests.
     *
     * @param request     the HTTP servlet request
     * @param response    the HTTP servlet response
     * @param filterChain the filter chain for processing the request
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (requestMatcher.matches(request)) {
            var apiKey = request.getHeader(API_KEY_HEADER);
            if (apiKey != null && apiKey.equals(apiKeyToken)) {
                log.info("Processing request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

