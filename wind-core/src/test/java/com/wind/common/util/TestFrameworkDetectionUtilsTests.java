package com.wind.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuxp
 * @date 2025-05-06 11:10
 **/
class TestFrameworkDetectionUtilsTests {

    @Test
    void testIsTest() {
        Assertions.assertTrue(TestFrameworkDetectionUtils.isTest());
    }
}
