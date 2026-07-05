package com.placement.placement_intelligence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter that enforces per-IP rate limits using Bucket4j token-bucket algorithm.
 * <ul>
 *   <li>Auth endpoints ({@code /api/auth/**}): 20 requests / minute</li>
 *   <li>All other API endpoints: 200 requests / minute</li>
 * </ul>
 * When a limit is exceeded the filter responds with HTTP 429 and a JSON body.
 */
@Component
public class RateLimitFilter implements Filter {

    private static final int AUTH_LIMIT = 20;
    private static final int GENERAL_LIMIT = 200;
    private static final long WINDOW_SECONDS = 60L;

    /** Buckets for auth endpoints, keyed by remote IP. */
    private final ConcurrentHashMap<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    /** Buckets for all other endpoints, keyed by remote IP. */
    private final ConcurrentHashMap<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ip = resolveClientIp(httpRequest);
        String uri = httpRequest.getRequestURI();

        boolean isAuthEndpoint = uri.startsWith("/api/auth/");
        Bucket bucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(ip, k -> buildBucket(AUTH_LIMIT))
                : generalBuckets.computeIfAbsent(ip, k -> buildBucket(GENERAL_LIMIT));

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            sendRateLimitResponse(httpResponse);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Bucket buildBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.classic(limit, io.github.bucket4j.Refill.intervally(limit, Duration.ofSeconds(WINDOW_SECONDS)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Take only the first IP in a potentially comma-separated list
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of(
                "error", "Too Many Requests",
                "retryAfter", WINDOW_SECONDS
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
