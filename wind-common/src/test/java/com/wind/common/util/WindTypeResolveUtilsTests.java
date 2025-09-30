package com.wind.common.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author wuxp
 * @date 2025-09-30 13:28
 **/
class WindTypeResolveUtilsTests {

    @Test
    void testResolveSuperGenericTypeV1() {
        Type[] types = WindTypeResolveUtils.resolveSuperGenericType(new Example());
        Assertions.assertEquals(2, types.length);
    }

    @Test
    void testResolveSuperGenericTypeV2() {
        Assertions.assertEquals(2, WindTypeResolveUtils.resolveSuperGenericType(new Example2()).length);
        Type[] types = WindTypeResolveUtils.resolveSuperGenericType(new Example2(), A.class);
        Assertions.assertEquals(1, types.length);
        Assertions.assertEquals(String.class, types[0]);
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
