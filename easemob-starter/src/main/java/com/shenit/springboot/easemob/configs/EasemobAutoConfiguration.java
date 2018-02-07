package com.shenit.springboot.easemob.configs;

import com.shenit.commons.utils.ShenValidates;
import com.shenit.springboot.easemob.EasemobClient;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;

import javax.net.ssl.SSLException;
import java.util.Optional;

/**
 * Created by jiangnan on 17/06/2017.
 */
@Configuration
@ConditionalOnProperty(value="easemob.enabled",havingValue= "true")
@EnableConfigurationProperties({
        EasemobAutoConfiguration.Properties.class,
        EasemobAutoConfiguration.RequestProperties.class,
})
public class EasemobAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(EasemobAutoConfiguration.class);
    private final Properties props;
    private final RequestProperties reqProps;
    private ClientHttpRequestFactory factory;

    public EasemobAutoConfiguration(Properties props,RequestProperties reqProps,
                                    Optional<ClientHttpRequestFactory> factory){
        this.props = props;
        this.reqProps = reqProps;
        this.factory = factory.orElse(null);
    }

    @Bean
    public EasemobClient easeclient() throws SSLException {
        if(StringUtils.isEmpty(props.host)){
            if (LOG.isDebugEnabled()) LOG.debug("[client([])] No easemob host provided");
            throw new IllegalArgumentException("No host for easemob client!!");
        }
        int processes = ShenValidates.gt(props.poolSize ,0) ? props.poolSize :
                Runtime.getRuntime().availableProcessors();

            Netty4ClientHttpRequestFactory nettyFactory  = new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(processes));
            if (reqProps.connTimeout > 0) nettyFactory.setConnectTimeout(reqProps.connTimeout);
            if (reqProps.readTimeout > 0) nettyFactory.setReadTimeout(reqProps.readTimeout);
            nettyFactory.setSslContext(SslContextBuilder.forClient().build());
            factory = nettyFactory;

        EasemobClient client =  new EasemobClient(props.host,props.clientId,props.appkey,props.clientSecret);
        client.setRequestFactory(factory);
        if(factory != null) client.setRequestFactory(factory);

        return client;
    }


    @ConfigurationProperties(prefix = "easemob")
    public static class Properties {
        private String clientId;
        private String appkey;
        private Integer poolSize;
        private String clientSecret;
        private String host;

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public void setAppkey(String appkey) {
            this.appkey = appkey;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPoolSize(Integer poolSize) {
            this.poolSize = poolSize;
        }
    }

    @ConfigurationProperties(prefix = "easemob.request")
    public static class RequestProperties {
        private int connTimeout;
        private int readTimeout;

        public void setConnTimeout(int connTimeout) {
            this.connTimeout = connTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
}
