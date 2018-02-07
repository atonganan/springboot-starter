package com.shenit.springboot.rest.annotations;


import com.shenit.springboot.rest.proxies.ResteasyClientProxyFactory;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(ResteasyClientProxyFactory.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableResteasyClient {
}
