package com.insider.testcase.test_automation_api.pet.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insider.testcase.test_automation_api.config.rest.RestClient;
import com.insider.testcase.test_automation_api.pet.client.exception.EmptyResponseException;
import com.insider.testcase.test_automation_api.pet.client.response.UploadImageResponse;
import com.insider.testcase.test_automation_api.pet.config.PetClientProperties;
import com.insider.testcase.test_automation_api.pet.client.request.QueryParamNames;
import com.insider.testcase.test_automation_api.pet.client.response.Pet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PetClient {
    private final RestClient restClient;
    private final PetClientProperties petClientProperties;
    private final ObjectMapper objectMapper;
    private final ParameterizedTypeReference<List<Pet>> PET_LIST_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private final ParameterizedTypeReference<Void> VOID_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private final ParameterizedTypeReference<Pet> PET_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private final ParameterizedTypeReference<UploadImageResponse> UPLOAD_IMAGE_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

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
                PET_LIST_RESPONSE_TYPE,
                headers
        );
    }

    public <T> ResponseEntity<?> addNewPet(T request) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(petClientProperties.getAddNewPet())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restClient.exchange(
                HttpMethod.POST,
                uri,
                request,
                VOID_RESPONSE_TYPE,
                headers
        );
    }

    @Retryable(
            retryFor = EmptyResponseException.class,
            maxAttemptsExpression = "${test-resilience.rest.pet.find-by-status.attempt}",
            backoff = @Backoff(delayExpression = "${test-resilience.rest.pet.find-by-status.delay}")
    )
    public ResponseEntity<?> findPetById(String petId) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(String.format(petClientProperties.getFindPetById(), petId))
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Pet> res = restClient.exchange(
                HttpMethod.GET,
                uri,
                PET_RESPONSE_TYPE,
                headers
        );

        if (Objects.isNull(res.getBody().getId())) throw new EmptyResponseException("Null body from " + uri, res);
        return res;
    }

    public <T> ResponseEntity<?> updateExistingPet(T request) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(petClientProperties.getUpdateExistingPet())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restClient.exchange(
                HttpMethod.PUT,
                uri,
                request,
                PET_RESPONSE_TYPE,
                headers
        );
    }

    public ResponseEntity<?> uploadImage(String petId,
                                         String additionalMetadata,
                                         Resource file,
                                         MediaType fileContentType) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(String.format(petClientProperties.getUploadAnImage(), petId))
                .build()
                .toUri();

        MultipartBodyBuilder mbb = new MultipartBodyBuilder();
        mbb.part("additionalMetadata", additionalMetadata == null ? "" : additionalMetadata);
        var filePart = mbb.part("file", file);
        if (file.getFilename() != null) filePart.filename(file.getFilename());
        if (fileContentType != null) filePart.contentType(fileContentType);

        MultiValueMap<String, HttpEntity<?>> multipart = mbb.build();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return restClient.exchange(
                HttpMethod.POST,
                uri,
                multipart,
                UPLOAD_IMAGE_RESPONSE_TYPE,
                headers
        );
    }

    public ResponseEntity<?> updatePetWithFormData(String petId, String name, String status) {
        URI uri = UriComponentsBuilder
                .fromUriString(petClientProperties.getBaseUrl())
                .path(String.format(petClientProperties.getUpdatePetInStoreWithFormData(), petId))
                .build()
                .toUri();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        if (name != null)   form.add("name", name);
        if (status != null) form.add("status", status);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return restClient.exchange(
                HttpMethod.POST,
                uri,
                form,
                UPLOAD_IMAGE_RESPONSE_TYPE,
                headers
        );
    }

    @Recover
    private ResponseEntity<?> findPetById(EmptyResponseException ex, String petId) {
        return ex.getLastResponse();
    }
}
