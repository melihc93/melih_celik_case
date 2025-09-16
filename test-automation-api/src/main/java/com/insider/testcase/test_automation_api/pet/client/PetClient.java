package com.insider.testcase.test_automation_api.pet.client;

import com.insider.testcase.test_automation_api.config.rest.RestClient;
import com.insider.testcase.test_automation_api.pet.client.config.PetClientProperties;
import com.insider.testcase.test_automation_api.pet.client.request.QueryParamNames;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PetClient {
    private final RestClient restClient;
    private final PetClientProperties petClientProperties;
    private static final ParameterizedTypeReference<List<Pet>> PET_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    public ResponseEntity<?> findByStatus(List<String> status) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(petClientProperties.getFindByStatus())
                .queryParam(QueryParamNames.STATUS.value(), String.join(",", status))
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return restClient.exchange(
                HttpMethod.GET,
                uri,
                PET_LIST_TYPE,
                headers
        );
    }
}
