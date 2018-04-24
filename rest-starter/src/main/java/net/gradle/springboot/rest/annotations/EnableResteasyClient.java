package net.gradle.springboot.rest.annotations;


import net.gradle.springboot.rest.proxies.ResteasyClientProxyFactory;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(ResteasyClientProxyFactory.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableResteasyClient {
}
