package net.gradle.springboot.rest;

import net.gradle.springboot.rest.annotations.EnableRestClient;
import net.gradle.springboot.rest.annotations.EnableResteasyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "net.gradle.springboot.rest")
@EnableRestClient(basePackages = "net.gradle.springboot.rest")
@EnableResteasyClient
public class TestApplication{

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class);
    }
}
