package com.insider.testcase.test_automation_api.pet.client.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PetClientLoggingAspect {

    private final ObjectMapper objectMapper;

    private static final int PREVIEW_LIMIT = 2000;
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            HttpHeaders.AUTHORIZATION, "X-API-Key", "Api-Key", "X-Auth-Token"
    );

    @Around("execution(public * com.insider.testcase.test_automation_api.pet.client.PetClient.*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();

        if (log.isDebugEnabled()) {
            log.debug("{} args={}", method, safeArgs(pjp.getArgs()));
        }

        long t0 = System.nanoTime();
        try {
            Object out = pjp.proceed();
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);

            // If the method returns a ResponseEntity, log details about it
            if (out instanceof ResponseEntity<?> res) {
                var status = res.getStatusCode();
                boolean ok2xx = status.is2xxSuccessful();

                // INFO summary (always)
                logAt(ok2xx ? LogLevel.INFO : LogLevel.WARN,
                        "{} -> status={} time={}ms bodyType={} size={}",
                        method, status.value(), tookMs,
                        bodyType(res.getBody()),
                        bodySize(res.getBody())
                );

                // DEBUG details (only when DEBUG enabled)
                if (log.isDebugEnabled()) {
                    log.debug("{} headers={}", method, redactHeaders(res.getHeaders()));
                    log.debug("{} body={}", method, truncate(toJson(res.getBody())));
                }
            } else {
                // Non-ResponseEntity return (rare, but safe)
                log.info("{} -> returnType={} time={}ms",
                        method,
                        out == null ? "null" : out.getClass().getSimpleName(),
                        tookMs);
                if (log.isDebugEnabled()) {
                    log.debug("{} return={}", method, truncate(toJson(out)));
                }
            }
            return out;

        } catch (Throwable ex) {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            log.error("{} threw {} after {}ms; args={}",
                    method, ex.getClass().getSimpleName(), tookMs, safeArgs(pjp.getArgs()), ex);
            throw ex;
        }
    }

    // ---------- helpers ----------

    private enum LogLevel { INFO, WARN }
    private void logAt(LogLevel lvl, String msg, Object... args) {
        if (lvl == LogLevel.WARN) log.warn(msg, args);
        else log.info(msg, args);
    }

    private String bodyType(Object body) {
        return body == null ? "null" : body.getClass().getSimpleName();
    }

    private int bodySize(Object body) {
        if (body == null) return -1;
        if (body instanceof Collection<?> c) return c.size();
        if (body instanceof Map<?, ?> m) return m.size();
        if (body.getClass().isArray()) return java.lang.reflect.Array.getLength(body);
        if (body instanceof CharSequence cs) return cs.length();
        return 1; // treat as single object
    }

    private Object safeArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        List<Object> out = new ArrayList<>(args.length);
        for (Object a : args) out.add(argPreview(a));
        return out;
    }

    private Object argPreview(Object a) {
        if (a == null) return null;
        // Avoid dumping file contents / binary data
        if (a instanceof Resource r) {
            return Map.of("Resource", Map.of("filename", r.getFilename(), "desc", r.getDescription()));
        }
        if (a instanceof MultiValueMap<?, ?> mv) {
            // Show keys and toString of values (not bytes)
            Map<Object, Object> m = new LinkedHashMap<>();
            mv.forEach((k, v) -> m.put(k, String.valueOf(v)));
            return Map.of("MultiValueMap", m);
        }
        // Default: JSON preview
        return truncate(toJson(a));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= PREVIEW_LIMIT) return s;
        return s.substring(0, PREVIEW_LIMIT) + "...(truncated)";
    }

    private HttpHeaders redactHeaders(HttpHeaders headers) {
        if (headers == null) return HttpHeaders.EMPTY;
        HttpHeaders copy = new HttpHeaders();
        headers.forEach((k, v) -> {
            if (SENSITIVE_HEADERS.contains(k)) copy.add(k, "***");
            else copy.put(k, v);
        });
        return copy;
    }
}
