package com.shenit.springboot.rest.annotations;

import java.lang.annotation.*;

/**
 * 用于声明多个请求的HTTP Header.
 * @author jiangnan
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Headers {
    /**
     * 设置头名称
     * @return
     */
    Header[] value();
}
