package net.gradle.springboot.amqp.rabbit.converters;

import com.google.gson.reflect.TypeToken;
import net.gradle.commons.utils.GsonUtils;
import net.gradle.commons.utils.ShenStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Gson的消息转换工具.
 */
public class GsonJsonMessageConverter extends AbstractJsonMessageConverter {
    private static final Logger LOG = LoggerFactory.getLogger(GsonJsonMessageConverter.class);
    public static final String TYPE_ARGS = "__TypeArgs__";

    public GsonJsonMessageConverter(ObjectProvider<ClassMapper> classMapper ) {
        ClassMapper mapper = classMapper.getIfAvailable();
        setClassMapper(mapper != null ? mapper : new DefaultClassMapper());
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        getClassMapper().fromClass(object.getClass(),messageProperties);
        byte[] bytes = GsonUtils.format(object).getBytes(StandardCharsets.UTF_8);
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(getDefaultCharset());
        messageProperties.setContentLength(bytes.length);


        return new Message(bytes,messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {

        String data = new String(message.getBody(), StandardCharsets.UTF_8);
        if (LOG.isTraceEnabled()) LOG.trace("[fromMessage([message])] received data -> {}", data);
        Type type = getClassMapper().toClass(message.getMessageProperties());
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        try {
            if (headers.containsKey(TYPE_ARGS)) {
                Type[] typeArgs = GsonUtils.parse(ShenStrings.str(headers.get(TYPE_ARGS)), new TypeToken<Type[]>() {});
                //针对范型的处理
                TypeToken token = TypeToken.getParameterized(type, typeArgs);
                if (LOG.isTraceEnabled()) LOG.trace("[fromMessage([message])] token:[{}], typeArgs:[{}]",
                        GsonUtils.format(token), GsonUtils.format(typeArgs));
                if (token != null && token.getType() != null) type = token.getType();
            }
        }catch(Exception ex){
            if (LOG.isDebugEnabled()) LOG.debug("[fromMessage([message])] Parse message[{}] with exception. ", GsonUtils.format(message),ex );
        }
        return GsonUtils.parse(data, type);
    }

}
