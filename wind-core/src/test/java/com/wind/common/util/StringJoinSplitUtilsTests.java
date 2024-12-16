package com.wind.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * @author wuxp
 * @date 2024-12-15 18:08
 **/
class StringJoinSplitUtilsTests {

    @Test
    void testJoin() {
        String result = StringJoinSplitUtils.join("12", "34");
        Assertions.assertEquals("12,34", result);
    }

    @Test
    void testSplit() {
        Set<String> result = StringJoinSplitUtils.split("1,2");
        Assertions.assertEquals(2, result.size());
    }
}
