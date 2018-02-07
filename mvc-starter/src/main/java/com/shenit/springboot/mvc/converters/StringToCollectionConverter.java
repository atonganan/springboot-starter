package com.shenit.springboot.mvc.converters;

import com.shenit.commons.utils.GsonUtils;
import com.shenit.commons.utils.ShenStrings;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 把字符串转换成Set
 */
@Component
public final class StringToCollectionConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return GsonUtils.parse(ShenStrings.str(source),targetType.getObjectType());
    }
}
