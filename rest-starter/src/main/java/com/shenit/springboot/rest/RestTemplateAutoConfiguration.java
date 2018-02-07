package com.shenit.springboot.rest;

import com.shenit.commons.utils.ShenStrings;
import com.shenit.springboot.mvc.converters.GosuGsonHttpConverter;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 注册RestTemplate
 * Created by jiangnan on 12/06/2017.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)   //高优先级加载
@EnableConfigurationProperties(RestTemplateAutoConfiguration.RestTemplateProperties.class)
@Configuration
public class RestTemplateAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RestTemplateAutoConfiguration.class);
    private final RestTemplateProperties properties;

    public RestTemplateAutoConfiguration(RestTemplateProperties properties){
        this.properties = properties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        int processes = Runtime.getRuntime().availableProcessors() * 4;
        Netty4ClientHttpRequestFactory reqFactory = new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(processes));
        reqFactory.afterPropertiesSet();    //设置默认的SSL上下文
        //connection timeout for 10s
        return builder.setConnectTimeout(properties.connTimeout)
                .messageConverters(new GosuGsonHttpConverter(),new StringHttpMessageConverter(StandardCharsets.UTF_8))
                //read timeout for 30s
                .setReadTimeout(properties.readTimeout)
                .requestFactory(reqFactory)
                .interceptors((request,body,chain) -> {
                    if(!request.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
                        request.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
                    }
                    return chain.execute(request,body);
                })
                .errorHandler(new ResponseErrorHandler() {
                    @Override
                    public boolean hasError(ClientHttpResponse response) throws IOException {
                        return response.getStatusCode() != HttpStatus.OK;
                    }

                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        String respMessage = ShenStrings.join(IOUtils.readLines(response.getBody(), StandardCharsets.UTF_8));
                        LOG.warn("[handleError([response])] request with error -> {}", respMessage);
                        throw new RestClientException(respMessage);
                    }
                }).build();
    }

    @Bean
    public SSLRestTemplateWrapper sslRestTemplateWrapper() {

        SSLRestTemplateWrapper  sslRestTemplateWrapper = new SSLRestTemplateWrapper();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        sslRestTemplateWrapper.restTemplate = new RestTemplate(requestFactory);
        return sslRestTemplateWrapper;
    }

    @ConditionalOnMissingBean
    @Qualifier("simpleFixed") @Bean
    public RetryTemplate simpleFixedRetryTemplate() {
        RetryTemplate retry = new RetryTemplate();
        retry.setRetryPolicy(new SimpleRetryPolicy(properties.retries, Collections.<Class<? extends Throwable>, Boolean> singletonMap(Exception.class, true)));
        FixedBackOffPolicy fixedBackoff = new FixedBackOffPolicy();
        fixedBackoff.setBackOffPeriod(properties.delay);
        retry.setBackOffPolicy(fixedBackoff);
        return retry;
    }

    @ConfigurationProperties("rest")
    public static class RestTemplateProperties{
        private int connTimeout = 3000;  //默认连接超时3s
        private int readTimeout = 10000;  //默认读取超时10s
        private int retries = 3;   //默认重试5次
        private int delay = 10000; //两次重试之间默认10s

        public void setConnTimeout(int connectTimeout) {
            this.connTimeout = connectTimeout;
        }
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
        public void setRetries(int retries) {
            this.retries = retries;
        }
        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    public static class SSLRestTemplateWrapper{
        public RestTemplate restTemplate;
    }
}
