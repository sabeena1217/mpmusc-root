package com.mpmusc.config;

//import org.apache.hc.client5.http.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.ssl.SSLContextBuilder;
//import org.apache.hc.core5.ssl.TrustAllStrategy;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class InsecureRestTemplateConfig {
//
//    @Bean
//    public RestTemplate insecureRestTemplate() throws Exception {
//        // Build an SSL context that accepts all certificates
//        SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
//                .loadTrustMaterial(new TrustAllStrategy());
//
//        // Set up the SSL socket factory and hostname verifier
//        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
//                sslContextBuilder.build(),
//                NoopHostnameVerifier.INSTANCE
//        );
//
//        // Build HttpClient with the custom SSL socket factory
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setSSLSocketFactory(socketFactory)
//                .build();
//
//        // Set the HttpClient into the request factory for RestTemplate
//        HttpComponentsClientHttpRequestFactory factory =
//                new HttpComponentsClientHttpRequestFactory(httpClient);
//
//        return new RestTemplate(factory);
//    }
}
