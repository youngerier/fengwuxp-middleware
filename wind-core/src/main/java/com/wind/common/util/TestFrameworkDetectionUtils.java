package com.wind.common.util;

import com.wind.common.WindConstants;

/**
 * 测试框架检测工具类
 *
 * @author wuxp
 * @date 2025-05-06 11:04
 **/
public final class TestFrameworkDetectionUtils {

    private static final String JUNIT = "JUnit";

    private static final String TESTNG = "TestNG";

    private static final String UNKNOWN = WindConstants.UNKNOWN;

    private static final String FRAMEWORK = detectFramework();

    private TestFrameworkDetectionUtils() {
        throw new AssertionError();
    }

    public static boolean isTest() {
        return !UNKNOWN.equals(FRAMEWORK);
    }

    private static String detectFramework() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            String className = e.getClassName();
            if (className.startsWith("org.testng.")) {
                return TESTNG;
            }
            if (className.startsWith("org.junit.")) {
                return JUNIT;
            }
        }
        return UNKNOWN;
    }
}
