package com.wind.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuxp
 * @date 2025-04-07 10:03
 **/
class DelegateObjectUtilsTests {


    @Test
    void testFind() {
        ExampleObject1 example = new ExampleObject1(new ExampleObject2(new ExampleObject3(null)));
        Assertions.assertEquals("ExampleObject1",  DelegateObjectUtils.findDelegate(example, ExampleObject1.class).sayHello());
        Assertions.assertEquals("ExampleObject2",  DelegateObjectUtils.requireDelegate(example, ExampleObject2.class).sayHello());
        Assertions.assertEquals("ExampleObject3",  DelegateObjectUtils.findDelegate(example, ExampleObject3.class).sayHello());
    }


    @AllArgsConstructor
    @Getter
    public abstract static class AbstractDelegateObject {

        private final AbstractDelegateObject delegate;

        public String sayHello() {
            return getClass().getSimpleName();
        }
    }

    public static class ExampleObject1 extends AbstractDelegateObject {

        public ExampleObject1(AbstractDelegateObject delegate) {
            super(delegate);
        }

    }

    public static class ExampleObject2 extends AbstractDelegateObject {

        public ExampleObject2(AbstractDelegateObject delegate) {
            super(delegate);
        }

    }

    public static class ExampleObject3 extends AbstractDelegateObject {

        public ExampleObject3(AbstractDelegateObject delegate) {
            super(delegate);
        }
    }
}
