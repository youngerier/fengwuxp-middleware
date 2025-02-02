package com.wind.logging.logback.mask;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.wind.mask.MaskRuleRegistry;
import com.wind.mask.ObjectMaskPrinter;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * 日志脱敏
 *
 * @author wuxp
 * @date 2024-08-07 15:47
 **/
public class MaskingMessageConverter extends ClassicConverter {

    public final static MaskRuleRegistry LOG_MASK_RULE_REGISTRY = new MaskRuleRegistry();

    private final static ObjectMaskPrinter MASKER = new ObjectMaskPrinter(LOG_MASK_RULE_REGISTRY);

    @Override
    public String convert(ILoggingEvent event) {
        Object[] argumentArray = event.getArgumentArray();
        if (ObjectUtils.isEmpty(argumentArray)) {
            // TODO 字符串处理
            return event.getFormattedMessage();
        }
        try {
            Object[] args = Arrays.stream(argumentArray)
                    .map(object -> {
                        if (requireMask(object)) {
                            return MASKER.mask(object);
                        }
                        return object;
                    })
                    .toArray(Object[]::new);
            return MessageFormatter.arrayFormat(event.getMessage(), args).getMessage();
        } catch (Throwable throwable) {
            // TODO
            return event.getFormattedMessage();
        }
    }

    private boolean requireMask(Object o) {
        Class<?> useMaskClass = getUseMaskClass(o);
        return useMaskClass != null && LOG_MASK_RULE_REGISTRY.requireMask(useMaskClass);
    }

    @SuppressWarnings("unchecked")
    private Class<?> getUseMaskClass(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Throwable) {
            return null;
        }
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType();
        } else if (ClassUtils.isAssignable(Collection.class, clazz)) {
            Collection<Object> objects = (Collection<Object>) o;
            if (objects.isEmpty()) {
                return null;
            }
            return objects.iterator().next().getClass();
        }
        return clazz;
    }

}