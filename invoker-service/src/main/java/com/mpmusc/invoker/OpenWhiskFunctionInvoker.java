package com.mpmusc.invoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpmusc.config.OpenWhiskProperties;
import com.mpmusc.core.GenderDistributionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component("openwhisk")
@RequiredArgsConstructor
public class OpenWhiskFunctionInvoker implements FunctionInvoker {

    private final OpenWhiskProperties openWhiskProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openwhisk.api.host}")
    private String apiHost;
    @Value("${openwhisk.namespace}")
    private String namespace;
    @Value("${openwhisk.action}")
    private String actionName;
    @Value("${openwhisk.api.key}")
    private String authKey;

    @Autowired
    @Qualifier("trustAllRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public ProviderResponse invoke(String filename) throws JsonProcessingException {
        String url = String.format(
                "%s/api/v1/namespaces/%s/actions/%s?blocking=true&result=true",
                apiHost, namespace, actionName);

        // Build Basic Auth header
        String encodedAuth = Base64.getEncoder()
                .encodeToString(authKey.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct JSON body using ObjectMapper
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("filename", filename);
        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String jsonResponse = response.getBody();

            GenderDistributionResult result = objectMapper.readValue(jsonResponse, GenderDistributionResult.class);
            long executionTimeMs = result.getExecutionTimeMillis();
            System.out.println("[OPENWHISK] " + jsonResponse);
            return new ProviderResponse(jsonResponse, true, null, executionTimeMs);

        } catch (HttpClientErrorException e) {
            // OpenWhisk error: show OpenWhisk's response
            System.out.println("[OPENWHISK] " + e.getMessage());
            return new ProviderResponse(
                    String.format("OpenWhisk error (%d): %s", e.getRawStatusCode(), e.getResponseBodyAsString()),
                    false,
                    e.getMessage(),
                    0
            );
        } catch (Exception e) {
            // Other unknown error
            System.out.println("[OPENWHISK] " + e.getMessage());
            return new ProviderResponse(
                    "Unexpected error invoking OpenWhisk: " + e.getMessage(),
                    false,
                    e.getMessage(),
                    0
            );
        }
    }

    @Override
    public String getRegion() {
        return openWhiskProperties.getRegion();
    }

    @Override
    public BigDecimal getEstimatedCost() {
        return openWhiskProperties.getEstimatedCost();
    }

    @Override
    public int getConcurrency() {
        return openWhiskProperties.getConcurrency();
    }

}