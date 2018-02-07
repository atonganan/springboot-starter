package com.shenit.springboot.beans.enums;

/**
 * 带有code的枚举类型.
 */
public interface CodedEnum<T> {
    /**
     * 获取代号
     * @return
     */
    T getCode();

}
