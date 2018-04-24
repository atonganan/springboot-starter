package net.gradle.springboot.redis.configs;

import net.gradle.commons.utils.ShenStrings;
import net.gradle.springboot.redis.RedisClient;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * RestTempalte的配置支持,主要修改了对于序列化部分的行为.
 * Created by jiangnan on 05/06/2017.
 */
@Configuration
@ConfigurationProperties("redis")
public class RedisTemplateConfiguration {
    private String namespace;
    private String schema;

    public void setNamespace(String namespace) {
        this.namespace = namespace;

    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private RedisSerializer<Object> strSerializer = new RedisSerializer<Object>(){

        @Override
        public byte[] serialize(Object o) throws SerializationException {
            return ShenStrings.bytes(o);
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            return ArrayUtils.isEmpty(bytes) ? null : new String(bytes,StandardCharsets.UTF_8);
        }
    };

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        setSerializer(template);
        return template;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        setSerializer(template);
        return template;
    }


    @Bean
    public RedisClient redisClient(RedisTemplate<String, Object> tmpl){
        return new RedisClient(namespace,schema,tmpl);
    }

    private void setSerializer(RedisTemplate<?,?> tmpl) {
        tmpl.setKeySerializer(strSerializer);
        tmpl.setValueSerializer(strSerializer);
        tmpl.setHashValueSerializer(strSerializer);
        tmpl.setHashKeySerializer(strSerializer);
    }
}
