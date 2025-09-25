package com.wind.common.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author wuxp
 * @date 2024-09-14 13:50
 **/
class WindReflectUtilsTests {

    @Test
    void testResolveSuperGenericTypeV1() {
        Type[] types = WindReflectUtils.resolveSuperGenericType(new Example());
        Assertions.assertEquals(2, types.length);
    }

    @Test
    void testResolveSuperGenericTypeV2() {
        Assertions.assertEquals(2, WindReflectUtils.resolveSuperGenericType(new Example2()).length);
        Type[] types = WindReflectUtils.resolveSuperGenericType(new Example2(), A.class);
        Assertions.assertEquals(1, types.length);
        Assertions.assertEquals(String.class, types[0]);
    }

    @Test
    void testFindFieldGetMethod() {
        Field field = WindReflectUtils.findField(Example2.class, "name");
        Assertions.assertNotNull(field);
        Method method = WindReflectUtils.findFieldGetMethod(field);
        Assertions.assertNotNull(method);
    }

    @Test
    void testReflectIgnoreJavaModel() {
        List<String> fieldNames = WindReflectUtils.getFieldNames(BigDecimal.class);
        Field[] fields = WindReflectUtils.findFields(BigDecimal.class, fieldNames);
        boolean match = Arrays.stream(fields).map(Field::accessFlags)
                .flatMap(Collection::stream)
                .anyMatch(accessFlag -> accessFlag == AccessFlag.PRIVATE);
        Assertions.assertTrue(match);
    }

    @Test
    void testFindFields() {
        List<String> fieldNames = WindReflectUtils.getFieldNames(Example2.class);
        Field[] fields = WindReflectUtils.findFields(Example2.class, fieldNames);
        boolean match = Arrays.stream(fields).map(Field::accessFlags)
                .flatMap(Collection::stream)
                .anyMatch(accessFlag -> accessFlag == AccessFlag.PRIVATE);
        Assertions.assertTrue(match);
    }

    @Test
    void testGetterMethods() {
        Method[] getterMethods = WindReflectUtils.getGetterMethods(Example2.class);
        Assertions.assertNotNull(getterMethods);
        Assertions.assertEquals(1, getterMethods.length);
    }

    @Test
    void testExchangeGetterHandle() throws Throwable {
        List<String> fieldNames = WindReflectUtils.getFieldNames(Example2.class);
        Field[] fields = WindReflectUtils.findFields(Example2.class, fieldNames);
        MethodHandle getterHandle = WindReflectUtils.exchangeGetterHandle(fields[0]);
        Assertions.assertNotNull(getterHandle);
        Assertions.assertEquals(fields[0].getType(), getterHandle.type().returnType());
        Example2 example2 = new Example2();
        example2.setName("1");
        Object result = getterHandle.invoke(example2);
        Assertions.assertEquals(example2.name, result);
    }

    @Test
    void testExchangeSetterHandle() throws Throwable {
        List<String> fieldNames = WindReflectUtils.getFieldNames(Example2.class);
        Field[] fields = WindReflectUtils.findFields(Example2.class, fieldNames);
        MethodHandle setterHandle = WindReflectUtils.exchangeSetterHandle(fields[0]);
        Assertions.assertNotNull(setterHandle);
        Example2 example2 = new Example2();
        setterHandle.invoke(example2, "11");
        Assertions.assertEquals("11", example2.name);
    }

    @Test
    void testExchangeMethodHandle() throws Throwable {
        Method[] getterMethods = WindReflectUtils.getGetterMethods(Example2.class);
        Assertions.assertNotNull(getterMethods);
        MethodHandle methodHandle = WindReflectUtils.exchangeMethodHandle(getterMethods[0]);
        Assertions.assertNotNull(methodHandle);
        Example2 example2 = new Example2();
        example2.setName("1");
        Object result = methodHandle.invoke(example2);
        Assertions.assertEquals(example2.name, result);

    }

    interface A<E> {

        void say(E e);
    }

    static class Example implements Function<String, List<String>> {

        @Override
        public List<String> apply(String s) {
            return Collections.emptyList();
        }
    }

    abstract static class AbstractDemo<T, R> implements Function<T, R> {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    static class Example2 extends AbstractDemo<String, List<String>> implements A<String> {

        private String name;

        @Override
        public List<String> apply(String s) {
            return Collections.emptyList();
        }

        @Override
        public void say(String s) {

        }
    }

}
