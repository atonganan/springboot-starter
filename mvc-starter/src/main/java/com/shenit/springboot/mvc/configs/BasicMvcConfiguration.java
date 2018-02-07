package com.shenit.springboot.mvc.configs;

import com.shenit.commons.exp.BusinessException;
import com.shenit.commons.utils.GsonUtils;
import com.shenit.commons.utils.ShenHttps;
import com.shenit.commons.utils.ShenStrings;
import com.shenit.springboot.mvc.converters.GosuGsonHttpConverter;
import com.shenit.springboot.mvc.converters.StringToCollectionConverter;
import com.shenit.springboot.mvc.converters.TextHttpConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基础的Mvc配置类.
 * Created by jiangnan on 06/06/2017.
 */
public abstract class BasicMvcConfiguration extends WebMvcConfigurerAdapter implements EnvironmentAware {
    private static final Logger LOG = LoggerFactory.getLogger(BasicMvcConfiguration.class);
    private static final String DISPLAY_ERROR = "服务器开了个小差";

    protected Environment env;

    public BasicMvcConfiguration(){}

    public BasicMvcConfiguration(Environment env) {
        this.env = env;
    }


    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new HandlerExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

                String msg = DISPLAY_ERROR;
                if(ex instanceof ResourceAccessException) ex = (Exception) ex.getCause();

                if(ex instanceof IllegalArgumentException) {
                    msg = ex.getMessage();
                } else if(ex instanceof IllegalAccessException) {
                    msg = ex.getMessage();
                    status = HttpStatus.FORBIDDEN.value();
                } else if(ex instanceof HttpMediaTypeNotSupportedException) {
                } else if(ex instanceof RestClientException){
                    msg = ex.getMessage();
                    msg = msg.startsWith(ShenStrings.DOUBLE_QUOTE) ? msg.substring(1,msg.length() - 1) : msg;   //移除双引号
                } else if(ex instanceof IllegalStateException) {
                    msg = ex.getMessage();
                }

                if(ex instanceof BusinessException){
                    BusinessException bex = (BusinessException) ex;
                    msg = GsonUtils.format(bex.resp);
                }

                if (LOG.isInfoEnabled()) {
                    LOG.info("[resolveException([request, response, handler, ex])] Illegal call from ip -> {}, headers -> {}, url -> {},query -> {}",
                            ShenHttps.getClientIp(request),
                            GsonUtils.format(ShenHttps.getHeaderMap(request)),
                            request.getRequestURI(),
                            request.getQueryString());
                }

                if(HttpStatus.METHOD_NOT_ALLOWED.value() == status) msg = StringUtils.EMPTY;

                response.setStatus(status);
                try {
                    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                    response.getOutputStream().write((msg == null ? StringUtils.EMPTY : StringUtils.wrap(msg,'"')).getBytes(StandardCharsets.UTF_8));
                    response.getOutputStream().flush();
                } catch (IOException e) {
                    //ignore
                }
                return null;
            }
        });
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToCollectionConverter());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new GosuGsonHttpConverter());
        converters.add(new TextHttpConverter());
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }

}
