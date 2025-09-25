package com.wind.common.util;

import com.wind.common.exception.AssertUtils;
import com.wind.common.exception.BaseException;
import com.wind.common.exception.DefaultExceptionCode;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 反射工具类，JDK21+ 兼容
 * - 不再强制使用 setAccessible
 * - 增加对 MethodHandle 的支持来读写字段
 * 注意：不支持静态字段
 *
 * @author wuxp
 * @date 2024-08-02 14:57
 **/
public final class WindReflectUtils {

    private static final String ERROR_MESSAGE = "argument clazz must not null";

    private static final Field[] EMPTY = new Field[0];

    /**
     * 字段缓存
     *
     * @key 类类型
     * @value 字段列表
     */
    private static final Map<Class<?>, List<Field>> CLASS_FIELDS_CACHES = new ConcurrentReferenceHashMap<>();

    private WindReflectUtils() {
        throw new AssertionError();
    }

    /**
     * 根据注解查找 {@link Field}，会递归查找超类
     *
     * @param clazz           类类型
     * @param annotationClass 注解类型
     * @return 字段列表
     */
    @NotNull
    public static Field[] findFields(@NotNull Class<?> clazz, Class<? extends Annotation> annotationClass) {
        AssertUtils.notNull(clazz, ERROR_MESSAGE);
        AssertUtils.notNull(annotationClass, "argument annotationClass must not null");
        return getMemberFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .toArray(Field[]::new);
    }

    /**
     * 根据字段名称查找 {@link Field}，会递归查找超类
     *
     * @param clazz      类类型
     * @param fieldNames 字段名称集合
     * @return 字段列表
     */
    @NotNull
    public static Field[] findFields(@NotNull Class<?> clazz, Collection<String> fieldNames) {
        AssertUtils.notNull(clazz, ERROR_MESSAGE);
        if (fieldNames == null || fieldNames.isEmpty()) {
            return EMPTY;
        }
        Set<String> names = new HashSet<>(fieldNames);
        return getMemberFields(clazz).stream()
                .filter(field -> names.contains(field.getName()))
                .distinct()
                .toArray(Field[]::new);
    }

    /**
     * 根据字段名称查找 {@link Field}，会递归查找超类
     *
     * @param clazz     类类型
     * @param fieldName 字段名称
     * @return 字段
     */
    @NotNull
    public static Field findField(@NotNull Class<?> clazz, String fieldName) {
        Field result = findFieldNullable(clazz, fieldName);
        AssertUtils.notNull(result, String.format("not found name = %s field", fieldName));
        return result;
    }

    /**
     * 根据字段名称查找 {@link Field}，会递归查找超类
     *
     * @param clazz     类类型
     * @param fieldName 字段名称
     * @return 字段，未找到时返回 null
     */
    @Null
    public static Field findFieldNullable(@NotNull Class<?> clazz, String fieldName) {
        Field[] fields = findFields(clazz, Collections.singleton(fieldName));
        return fields.length == 0 ? null : fields[0];
    }

    /**
     * 获取类的所有字段（会递归父类）
     *
     * @param clazz 类类型
     * @return 字段数组
     */
    @NotNull
    public static Field[] getFields(@NotNull Class<?> clazz) {
        return findFields(clazz, getFieldNames(clazz));
    }

    /**
     * 获取类的所有字段名称
     *
     * @param clazz 类类型
     * @return 字段名称列表
     */
    public static List<String> getFieldNames(@NotNull Class<?> clazz) {
        AssertUtils.notNull(clazz, ERROR_MESSAGE);
        return getMemberFields(clazz).stream()
                .map(Field::getName)
                .toList();
    }

    /**
     * 获取类的所有公有方法
     *
     * @param clazz 类类型
     * @return 方法数组
     */
    public static Method[] getMethods(@NotNull Class<?> clazz) {
        AssertUtils.notNull(clazz, ERROR_MESSAGE);
        return clazz.getMethods();
    }

    /**
     * 获取字段的 getter 方法（仅支持 public）
     *
     * @param field 字段
     * @return get 方法，未找到时返回 null
     */
    public static Method findFieldGetMethod(Field field) {
        Class<?> clazz = field.getDeclaringClass();
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        // 构建 getter 方法名
        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            return clazz.getMethod("get" + capitalized);
        } catch (NoSuchMethodException e) {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                try {
                    return clazz.getMethod("is" + capitalized);
                } catch (NoSuchMethodException ignored) {
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * 获取类的所有 getter 方法（public 且符合 JavaBean 规范）
     *
     * @param clazz 类类型
     * @return 方法数组
     */
    public static Method[] getGetterMethods(Class<?> clazz) {
        AssertUtils.notNull(clazz, ERROR_MESSAGE);
        return Arrays.stream(getMethods(clazz))
                .filter(WindReflectUtils::isGetterMethod)
                .toArray(Method[]::new);
    }

    /**
     * 判断方法是否是 getter
     */
    private static boolean isGetterMethod(Method method) {
        String name = method.getName();
        if ("getClass".equals(name)) {
            // getClass 是 Object 中的方法，忽略
            return false;
        }
        boolean isGet = name.startsWith("get") && name.length() > 3;
        boolean isBooleanGet = name.startsWith("is") && name.length() > 2;
        boolean noParams = method.getParameterCount() == 0;
        boolean hasReturn = method.getReturnType() != void.class;
        boolean isNotStatic = !Modifier.isStatic(method.getModifiers());
        boolean isPublic = Modifier.isPublic(method.getModifiers());
        return isPublic && isNotStatic && noParams && hasReturn && (isGet || isBooleanGet);
    }

    /**
     * 获取成员变量（递归父类，排除静态字段）
     *
     * @param clazz 类类型
     * @return 字段列表
     */
    private static List<Field> getMemberFields(Class<?> clazz) {
        return CLASS_FIELDS_CACHES.computeIfAbsent(clazz, WindReflectUtils::getClazzFields).stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();
    }

    /**
     * 获取类及其父类的所有字段
     */
    private static List<Field> getClazzFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return Collections.emptyList();
        }
        List<Field> result = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        result.addAll(getClazzFields(clazz.getSuperclass()));
        return result;
    }

    /**
     * 获取字段的 MethodHandle Getter（JDK21+ 推荐方式）
     *
     * @param field 字段
     * @return MethodHandle
     */
    public static MethodHandle exchangeGetterHandle(@NotNull Field field) {
        AssertUtils.notNull(field, "argument field must not null");
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, "Cannot access getter for field = " + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    /**
     * 获取字段的 MethodHandle Setter（JDK21+ 推荐方式）
     *
     * @param field 字段
     * @return MethodHandle
     */
    public static MethodHandle exchangeSetterHandle(@NotNull Field field) {
        AssertUtils.notNull(field, "argument field must not null");
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, "Cannot access setter for field = " + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    /**
     * 获取方法句柄
     *
     * @param method 方法
     * @return MethodHandle
     */
    @NotNull
    public static MethodHandle exchangeMethodHandle(Method method) {
        AssertUtils.notNull(method, "argument method must not null");
        try {
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, "Cannot access method  = " + method.getDeclaringClass().getName() + "#" + method.getName(), e);
        }
    }

    /**
     * 解析对象继承的超类或实现的接口上设置的泛型
     *
     * @param bean        对象
     * @param supperClass 指定的接口或超类，如果不为空，则只获取指定超类或接口上的泛型
     * @return 父类或者接口上设置的泛型
     */
    public static Type[] resolveSuperGenericType(@NotNull Object bean, @Nullable Class<?> supperClass) {
        AssertUtils.notNull(bean, "argument bean must not null");
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        ResolvableType matchType;
        if (supperClass != null) {
            matchType = findGenericTypeRecursive(ResolvableType.forClass(targetClass), supperClass);
            if (matchType == null) {
                throw new IllegalArgumentException(targetClass.getName() + " 未实现或继承 " + supperClass.getName());
            }
        } else {
            // 未指定 supperClass，优先找直接父类，如果父类是 Object 就取第一个接口
            ResolvableType resolvableType = ResolvableType.forClass(targetClass);
            ResolvableType superType = resolvableType.getSuperType();
            if (superType != ResolvableType.NONE && superType.getRawClass() != Object.class) {
                matchType = superType;
            } else {
                ResolvableType[] interfaces = resolvableType.getInterfaces();
                matchType = (interfaces.length > 0) ? interfaces[0] : null;
            }
        }

        if (matchType == null) {
            return new Type[0];
        }

        ResolvableType[] generics = matchType.getGenerics();
        AssertUtils.notEmpty(generics, () -> targetClass.getName() + " 未设置泛型");
        return Arrays.stream(generics)
                .map(ResolvableType::getType)
                .toArray(Type[]::new);
    }

    /**
     * 解析对象继承的超类或实现的接口上设置的所有泛型
     *
     * @param bean 对象
     * @return 父类或者接口上设置的所有泛型
     */
    public static Type[] resolveSuperGenericType(@NotNull Object bean) {
        return resolveSuperGenericType(bean, null);
    }

    /**
     * 递归查找继承链/接口树上指定 supperClass 对应的 ResolvableType
     */
    @Nullable
    private static ResolvableType findGenericTypeRecursive(ResolvableType type, Class<?> supperClass) {
        if (type == ResolvableType.NONE) {
            return null;
        }
        Class<?> rawClass = type.getRawClass();
        if (supperClass.equals(rawClass)) {
            return type;
        }
        ResolvableType superType = type.getSuperType();
        ResolvableType result = findGenericTypeRecursive(superType, supperClass);
        if (result != null) {
            return result;
        }
        for (ResolvableType iface : type.getInterfaces()) {
            result = findGenericTypeRecursive(iface, supperClass);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
