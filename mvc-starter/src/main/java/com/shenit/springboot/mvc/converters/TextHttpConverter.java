package com.shenit.springboot.mvc.converters;

import com.shenit.commons.utils.ShenStrings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 默认的字符串转换器.
 */
public class TextHttpConverter extends AbstractGenericHttpMessageConverter<Object> {
    public TextHttpConverter() {
        super(new MediaType("text", "*"));
        this.setDefaultCharset(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return StringUtils.join(IOUtils.readLines(inputMessage.getBody(),StandardCharsets.UTF_8),StringUtils.EMPTY);
    }

    @Override
    protected void writeInternal(Object o, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write(ShenStrings.bytes(o,StandardCharsets.UTF_8));
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return StringUtils.join(IOUtils.readLines(inputMessage.getBody(),StandardCharsets.UTF_8),StringUtils.EMPTY);
    }
}
