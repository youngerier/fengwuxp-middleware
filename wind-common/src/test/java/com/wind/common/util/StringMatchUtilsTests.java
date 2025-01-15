package com.wind.common.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author wuxp
 * @date 2025-01-15 13:20
 **/
class StringMatchUtilsTests {

    @Test
    void testMatches() {
        assertTrue(StringMatchUtils.matches("123abc", "\\d+abc"));
        assertFalse(StringMatchUtils.matches("abc123", "\\d+abc"));
        assertFalse(StringMatchUtils.matches(null, "\\d+abc"));
        assertFalse(StringMatchUtils.matches("123abc", null));
    }

    @Test
    void testContains() {
        assertTrue(StringMatchUtils.contains("Hello World", "World"));
        assertFalse(StringMatchUtils.contains("Hello World", "world"));
        assertFalse(StringMatchUtils.contains(null, "world"));
        assertFalse(StringMatchUtils.contains("Hello World", null));
    }

    @Test
    void testContainsIgnoreCase() {
        assertTrue(StringMatchUtils.containsIgnoreCase("Hello World", "world"));
        assertFalse(StringMatchUtils.containsIgnoreCase(null, "world"));
        assertFalse(StringMatchUtils.containsIgnoreCase("Hello World", null));
        assertFalse(StringMatchUtils.containsIgnoreCase(null, null));
    }

    @Test
    void testContainsAny() {
        assertTrue(StringMatchUtils.containsAny("Hello World", Arrays.asList("World", "Hi")));
        assertFalse(StringMatchUtils.containsAny("Hello World", Arrays.asList("world", "Hi")));
        assertFalse(StringMatchUtils.containsAny("Hello World", Collections.emptyList()));
        assertFalse(StringMatchUtils.containsAny(null, Arrays.asList("World", "Hi")));
        assertFalse(StringMatchUtils.containsAny("Hello World", null));
    }

    @Test
    void testContainsIgnoreCaseAny() {
        assertTrue(StringMatchUtils.containsIgnoreCaseAny("Hello World", Arrays.asList("world", "Hi")));
        assertTrue(StringMatchUtils.containsIgnoreCaseAny("Hello World", Arrays.asList("World", "Hi")));
        assertFalse(StringMatchUtils.containsIgnoreCaseAny("Hello World", Arrays.asList("earth", "Hi")));
        assertFalse(StringMatchUtils.containsIgnoreCaseAny("Hello World", Collections.emptyList()));
        assertFalse(StringMatchUtils.containsIgnoreCaseAny(null, Arrays.asList("world", "Hi")));
        assertFalse(StringMatchUtils.containsIgnoreCaseAny("Hello World", null));
    }
}
