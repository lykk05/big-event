package com.itheima.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI 调用专用的 RestTemplate：单例 + JDK HttpClient 连接池，避免每次请求重新建连。
 */
@Configuration
public class AiHttpConfig {

    @Bean
    public RestTemplate aiRestTemplate(
            @Value("${ai.api.connect-timeout:10000}") int connectTimeout,
            @Value("${ai.api.read-timeout:30000}") int readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return new RestTemplate(factory);
    }
}
