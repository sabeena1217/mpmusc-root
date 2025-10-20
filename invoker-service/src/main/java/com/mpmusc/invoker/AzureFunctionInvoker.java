package com.mpmusc.invoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpmusc.config.AzureProperties;
import com.mpmusc.core.GenderDistributionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component("azure")
@RequiredArgsConstructor
public class AzureFunctionInvoker implements FunctionInvoker {

    private final AzureProperties azureProperties;

    @Value("${azure.function.url}")
    private String functionUrl; // e.g. "https://mpmusc-azure.azurewebsites.net/api/genderdistribution"

    @Override
    public ProviderResponse invoke(String filename) throws JsonProcessingException {
        RestTemplate rest = new RestTemplate();
        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create JSON body
        Map<String, String> body = new HashMap<>();
        body.put("filename", filename); // adjust according to your GenderDistributionRequest

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        // Call POST
        String jsonResponse = rest.postForObject(functionUrl, request, String.class);
//        boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

        ObjectMapper objectMapper = new ObjectMapper();
        GenderDistributionResult result = objectMapper.readValue(jsonResponse, GenderDistributionResult.class);
        long executionTimeMs = result.getExecutionTimeMillis();

//        System.out.println("[AZURE] " + jsonResponse);
        return new ProviderResponse(jsonResponse, true, null, executionTimeMs);
    }

    @Override
    public String getRegion() {
        return azureProperties.getRegion();
    }

    @Override
    public BigDecimal getEstimatedCost() {
        return azureProperties.getEstimatedCost();
    }

    @Override
    public int getConcurrency() {
        return azureProperties.getConcurrency();
    }

}

/*



{
  "distribution": {
    "Engineering": {
      "Female": 22.29,
      "Male": 75.27,
      "Non-binary": 2.44
    },
    "Education": {
      "Female": 53.67,
      "Male": 40.39,
      "Non-binary": 5.95
    },
    "Finance": {
      "Male": 50.44,
      "Female": 44.53,
      "Non-binary": 5.03
    },
    "Legal": {
      "Male": 50.21,
      "Female": 44.48,
      "Non-binary": 5.31
    },
    "Healthcare": {
      "Male": 30.0,
      "Female": 63.3,
      "Non-binary": 6.7
    },
    "HR": {
      "Male": 30.43,
      "Female": 63.24,
      "Non-binary": 6.33
    },
    "IT": {
      "Male": 69.46,
      "Female": 27.45,
      "Non-binary": 3.09
    },
    "Manufacturing": {
      "Female": 18.04,
      "Male": 79.97,
      "Non-binary": 1.99
    },
    "Marketing": {
      "Female": 45.33,
      "Male": 49.91,
      "Non-binary": 4.76
    }
  },
  "executionTimeMillis": 566
}
 */