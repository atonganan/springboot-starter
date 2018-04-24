package net.gradle.springboot.rest.annotations;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 标有这个标注的接口，会注册成REST代理方法.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClient {
    /**
     * 地址前缀. 类中所有定义的方法的@Path所标注的地址都会拼在这个地址之后.
     * 支持SpringEL
     * @return
     */
    String value() default StringUtils.EMPTY;
}
