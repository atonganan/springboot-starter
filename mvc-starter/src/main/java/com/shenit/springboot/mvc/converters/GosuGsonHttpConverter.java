package com.shenit.springboot.mvc.converters;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.shenit.commons.utils.GsonUtils;
import com.shenit.commons.utils.ShenStrings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * 项目专用GSON消息处理器.
 */
public class GosuGsonHttpConverter extends GsonHttpMessageConverter {
    private static final Logger LOG = LoggerFactory.getLogger(GosuGsonHttpConverter.class);

    public GosuGsonHttpConverter() {
        super();
        setGson(GsonUtils.inst());
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        TypeToken<?> token = getTypeToken(clazz);
        return readTypeToken(token, inputMessage);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        TypeToken<?> token = getTypeToken(type);
        return readTypeToken(token, inputMessage);
    }

    private Object readTypeToken(TypeToken<?> token, HttpInputMessage inputMessage) throws IOException {
        Reader json = new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage.getHeaders()));
        try {
            if (LOG.isTraceEnabled()) {
                String body = ShenStrings.join(IOUtils.readLines(json), StringUtils.EMPTY);
                LOG.trace("[readTypeToken([token, inputMessage])] body -> {}", body);
                return getGson().fromJson(body, token.getType());
            }
            return getGson().fromJson(json, token.getType());
        } catch (JsonParseException ex) {
            throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    private Charset getCharset(HttpHeaders headers) {
        if (headers == null || headers.getContentType() == null || headers.getContentType().getCharset() == null) {
            return DEFAULT_CHARSET;
        }
        return headers.getContentType().getCharset();
    }

}