package com.qushetai.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${rest.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${rest.read.timeout:10000}")
    private int readTimeout;

    @Value("${rest.max.connections:100}")
    private int maxConnections;

    @Value("${rest.max.connections.per.route:20}")
    private int maxConnectionsPerRoute;

    @Value("${recommendation.flask.service.timeout:10000}")
    private int flaskServiceTimeout;

    /**
     * 通用 RestTemplate Bean
     * 用于一般的 HTTP 请求
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        // 设置连接池相关参数（如果需要使用连接池，可以使用 HttpComponentsClientHttpRequestFactory）
        return new RestTemplate(factory);
    }

    /**
     * 专门用于 Flask 推荐服务的 RestTemplate Bean
     * 使用独立的超时配置
     */
    @Bean("flaskRestTemplate")
    public RestTemplate flaskRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(flaskServiceTimeout); // 使用 Flask 服务的超时配置

        return new RestTemplate(factory);
    }

    /**
     * 使用 RestTemplateBuilder 创建更灵活的 RestTemplate
     * 支持重试机制和拦截器
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout));
    }

    /**
     * 用于标签服务的 RestTemplate
     * 使用独立的配置
     */
    @Bean("tagServiceRestTemplate")
    public RestTemplate tagServiceRestTemplate(
            @Value("${tag.service.timeout:5000}") int tagServiceTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(tagServiceTimeout);

        return new RestTemplate(factory);
    }

    /**
     * 用于长时间运行任务的 RestTemplate
     * 设置较长的超时时间
     */
    @Bean("longRunningRestTemplate")
    public RestTemplate longRunningRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30秒连接超时
        factory.setReadTimeout(60000);    // 60秒读取超时

        return new RestTemplate(factory);
    }
}