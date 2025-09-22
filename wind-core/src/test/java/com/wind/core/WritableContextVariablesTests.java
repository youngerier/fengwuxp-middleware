package com.wind.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuxp
 * @date 2025-09-02 15:56
 **/
 class WritableContextVariablesTests {


     @Test
     void testOf(){
         WritableContextVariables variables = WritableContextVariables.of();
         variables.putVariable("name", "zhasn");
         Assertions.assertEquals("zhasn", variables.getContextVariable("name"));
     }
}
