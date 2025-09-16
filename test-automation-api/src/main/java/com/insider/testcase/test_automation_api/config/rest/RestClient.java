package com.insider.testcase.test_automation_api.config.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class RestClient {
    private final WebClient webClient;

    public <T> ResponseEntity<T> exchange(HttpMethod method,
                                          URI uri,
                                          Object body,
                                          ParameterizedTypeReference<T> responseType,
                                          @Nullable HttpHeaders headers) {

        WebClient.RequestBodySpec req = webClient
                .method(method)
                .uri(uri)
                .headers(getHeadersIfExists(headers));

        WebClient.RequestHeadersSpec<?> spec;

        if (headers == null || !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            req = req.contentType(MediaType.APPLICATION_JSON);
        }
        spec = req.bodyValue(body);

        Mono<ResponseEntity<T>> mono = spec.exchangeToMono(resp -> resp.toEntity(responseType));

        return mono.block();
    }

    public <T> ResponseEntity<T> exchange(HttpMethod method,
                                          URI uri,
                                          ParameterizedTypeReference<T> responseType,
                                          @Nullable HttpHeaders headers) {

        WebClient.RequestBodySpec req = webClient
                .method(method)
                .uri(uri)
                .headers(getHeadersIfExists(headers));

        Mono<ResponseEntity<T>> mono = req.exchangeToMono(resp -> resp.toEntity(responseType));

        return mono.block();
    }

    private Consumer<HttpHeaders> getHeadersIfExists(HttpHeaders headers) {
        return h -> {
            if (headers != null) h.addAll(headers);
        };
    }
}
