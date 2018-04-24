package net.gradle.springboot.amqp.rabbit.configs;

import net.gradle.springboot.amqp.rabbit.converters.GsonJsonMessageConverter;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitAutoConfiguration {

    @Bean
    public MessageConverter amqpMessageConverter(ObjectProvider<ClassMapper> classMapper){
        return new GsonJsonMessageConverter(classMapper);
    }

}
