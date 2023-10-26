package com.wind.context.injection;

import com.wind.common.WindConstants;
import com.wind.common.exception.AssertUtils;
import com.wind.common.exception.BaseException;
import com.wind.context.variable.annotations.ContextVariable;
import com.wind.script.spring.SpringExpressionExecutor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 基于{@link ContextVariable}注解标记的方法参数注入者
 *
 * @author wuxp
 * @date 2023-10-25 07:18
 **/
@AllArgsConstructor
public class ContextAnnotationMethodParameterInjector implements MethodParameterInjector {

    private static final ParameterInjectionDescriptor[] EMPTY = new ParameterInjectionDescriptor[0];

    /**
     * 方法参数注入原信息
     *
     * @key method
     * @value 注入字段的元信息
     */
    private final Map<Method, ParameterInjectionDescriptor[]> descriptors = new ConcurrentReferenceHashMap<>(1024);

    private final ContextVariableProvider contextVariableProvider;

    private final Predicate<Class<?>> injectMatcher;

    public ContextAnnotationMethodParameterInjector(ContextVariableProvider contextVariableProvider, Set<String> injectObjectBasePackages) {
        this.contextVariableProvider = contextVariableProvider;
        this.injectMatcher = aClass -> {
            String name = aClass.getName();
            return injectObjectBasePackages.stream().anyMatch(name::startsWith);
        };
    }

    /**
     * 注入方法参数中被注解标记的的字段或参数
     *
     * @param method    方法
     * @param arguments 方法参数
     */
    public void inject(Method method, Object[] arguments) {
        ParameterInjectionDescriptor[] injectionDescriptors = descriptors.computeIfAbsent(method, this::parseParameterInjectionDescriptors);
        if (ObjectUtils.isEmpty(injectionDescriptors)) {
            return;
        }
        Map<String, Object> variables = contextVariableProvider.get();
        for (ParameterInjectionDescriptor descriptor : injectionDescriptors) {
            Object val = descriptor.evalVariable(variables);
            if (descriptor.isInjectParameter()) {
                descriptor.injectParameter(arguments, val);
            } else {
                // 注入字段值
                descriptor.injectFieldValue(arguments[descriptor.parameterIndex], val);
            }
        }
    }

    private ParameterInjectionDescriptor[] parseParameterInjectionDescriptors(Method method) {
        List<ParameterInjectionDescriptor> result = new ArrayList<>(4);
        int paramterIndex = 0;
        Parameter[] parameters = method.getParameters();
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (isSimpleType(parameterType)) {
                ContextVariable annotation = AnnotationUtils.findAnnotation(parameters[paramterIndex], ContextVariable.class);
                if (annotation != null) {
                    result.add(new ParameterInjectionDescriptor(annotation, parameterType, null, parameters[paramterIndex].getName(), paramterIndex));
                }
            } else {
                if (isAllowInject(parameterType)) {
                    result.addAll(parseParameterInjectionDescriptors(parameterType, paramterIndex));
                }
            }
            paramterIndex++;
        }
        if (result.isEmpty()) {
            return EMPTY;
        }
        return result.toArray(new ParameterInjectionDescriptor[0]);
    }

    private List<ParameterInjectionDescriptor> parseParameterInjectionDescriptors(Class<?> parameterType, final int index) {
        return getClassFields(parameterType).stream()
                .map(field -> {
                    ContextVariable annotation = AnnotationUtils.findAnnotation(field, ContextVariable.class);
                    if (annotation == null) {
                        return null;
                    }
                    return new ParameterInjectionDescriptor(annotation, parameterType, field, null, index);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isSimpleType(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) ||
                Objects.equals(String.class, clazz) ||
                clazz.isEnum();
    }

    /**
     * 获取一个类所有的 Field，包括私有的Field，并且会递归的获取超类的字段
     *
     * @param clazz 类类型
     * @return fields
     */
    private List<Field> getClassFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(16);
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if (!Object.class.equals(superclass)) {
            fields.addAll(this.getClassFields(superclass));
        }
        return fields;
    }

    private boolean isAllowInject(Class<?> clazz) {
        return injectMatcher.test(clazz);
    }

    /**
     * 参数注入描述
     */
    @Getter
    private static final class ParameterInjectionDescriptor {

        private final String variableName;

        private final String expression;

        private final boolean required;

        private final boolean override;

        /**
         * 注入字段或参数的类型
         */
        private final Class<?> type;

        /**
         * 注入字段的 set 方法
         */
        private final Method setterMethod;

        /**
         * 取值方法
         */
        private final Method getterMethod;

        private final String fieldName;

        /**
         * 在方法参数中的索引
         */
        private final int parameterIndex;

        public ParameterInjectionDescriptor(ContextVariable variable, Class<?> type, @Nullable Field field, String parameterName, int parameterIndex) {
            this.variableName = variable.name();
            this.expression = variable.expression();
            this.override = variable.override();
            this.required = variable.required();
            this.type = type;
            this.setterMethod = field == null ? null : findSetterMethod(field);
            this.getterMethod = field == null ? null : findGetterMethod(field);
            this.fieldName = field == null ? parameterName : field.getDeclaringClass().getName() + WindConstants.SHARP + field.getName();
            this.parameterIndex = parameterIndex;
        }

        boolean isInjectParameter() {
            return setterMethod == null;
        }

        void injectParameter(Object[] arguments, Object value) {
            injectWithOverride(val -> arguments[parameterIndex] = val,
                    value,
                    () -> arguments[parameterIndex]);
        }

        void injectFieldValue(Object owner, Object value) {
            injectWithOverride(val -> ReflectionUtils.invokeMethod(setterMethod, owner, val),
                    value,
                    () -> ReflectionUtils.invokeMethod(getterMethod, owner));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void injectWithOverride(Consumer<Object> setter, Object value, Supplier<Object> getter) {
            // TODO 做类型转换？
            if (override) {
                requiredCheckWrapper(setter).accept(value);
            } else {
                if (getter.get() == null) {
                    requiredCheckWrapper(setter).accept(value);
                }
            }
        }

        Consumer<Object> requiredCheckWrapper(Consumer<Object> consumer) {
            return value -> {
                if (required) {
                    AssertUtils.notNull(value, () -> String.format("%s must not null", fieldName));
                }
                consumer.accept(value);
            };
        }


        Object evalVariable(Map<String, Object> contextVariables) {
            if (StringUtils.hasLength(variableName)) {
                return contextVariables.get(variableName);
            }
            if (StringUtils.hasLength(expression)) {
                return SpringExpressionExecutor.eval(expression, contextVariables);
            }
            throw BaseException.common("invalid annotation tag, name and expression attribute is empty");
        }

        private Method findGetterMethod(Field field) {
            String methodName = findFieldMethodName(field.getName(), "get");
            Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), methodName);
            AssertUtils.notNull(method, () -> String.format("not find get method, field = %s#%s", field.getDeclaringClass().getName(), field.getName()));
            return method;
        }

        private Method findSetterMethod(Field field) {
            String methodName = findFieldMethodName(field.getName(), "set");
            Method method = ReflectionUtils.findMethod(field.getDeclaringClass(), methodName, field.getType());
            AssertUtils.notNull(method, () -> String.format("not find set method, field = %s#%s", field.getDeclaringClass().getName(), field.getName()));
            return method;
        }

        private String findFieldMethodName(String fieldName, String action) {
            String[] chars = fieldName.split(WindConstants.EMPTY);
            chars[0] = chars[0].toUpperCase();
            return action + String.join(WindConstants.EMPTY, chars);
        }

    }
}
