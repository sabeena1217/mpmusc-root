package com.mpmusc.invoker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpmusc.config.AwsProperties;
import com.mpmusc.core.GenderDistributionResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.math.BigDecimal;

@Component("aws")
@RequiredArgsConstructor
public class AwsFunctionInvoker implements FunctionInvoker {

    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private LambdaClient lambdaClient;

    @PostConstruct
    public void init() {
        lambdaClient = LambdaClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getAccessKey(),
                                awsProperties.getSecretKey())))
                .build();
    }

    @Override
    public ProviderResponse invoke(String filename) throws JsonProcessingException {
        String jsonPayload = "{\"filename\":\"" + filename + "\"}";

        InvokeRequest request = InvokeRequest.builder()
                .functionName(awsProperties.getLambda().getFunctionName())
                .payload(SdkBytes.fromUtf8String(jsonPayload))
                .build();
        InvokeResponse response = lambdaClient.invoke(request);
        String jsonResponse = response.payload().asUtf8String();

        ObjectMapper objectMapper = new ObjectMapper();
        GenderDistributionResult result = objectMapper.readValue(jsonResponse, GenderDistributionResult.class);
        long executionTimeMs = result.getExecutionTimeMillis();

        boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

        System.out.println("[AWS] " + jsonResponse);
        return new ProviderResponse(jsonResponse, success, success ? null : jsonResponse, executionTimeMs);


//        String jsonPayload = "{\"filename\":\"" + filename + "\"}";
//        LambdaClient lambda = LambdaClient.create();
//        InvokeRequest request = InvokeRequest.builder()
//                .functionName(functionName)
//                .payload(SdkBytes.fromUtf8String(jsonPayload))
//                .build();
//        InvokeResponse response = lambda.invoke(request);
//        return response.payload().asUtf8String();
    }

    @Override
    public String getRegion() {
        return awsProperties.getRegion();
    }

    @Override
    public BigDecimal getEstimatedCost() {
        return awsProperties.getEstimatedCost();
    }

    @Override
    public int getConcurrency() {
        return awsProperties.getConcurrency();
    }

}