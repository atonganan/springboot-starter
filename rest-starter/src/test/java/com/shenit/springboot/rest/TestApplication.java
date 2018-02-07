package com.shenit.springboot.rest;

import com.shenit.springboot.rest.annotations.EnableRestClient;
import com.shenit.springboot.rest.annotations.EnableResteasyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.shenit.springboot.rest")
@EnableRestClient(basePackages = "com.shenit.springboot.rest")
@EnableResteasyClient
public class TestApplication{

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class);
    }
}
