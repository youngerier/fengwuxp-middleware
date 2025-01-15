package com.wind.common.util;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 字符串匹配工具类型
 *
 * @author wuxp
 * @date 2025-01-15 13:09
 **/
public final class StringMatchUtils {

    private StringMatchUtils() {
        throw new AssertionError();
    }

    /**
     * Checks if the input matches the given regex pattern.
     *
     * @param input the input string
     * @param regex the regex pattern
     * @return true if the input matches the regex pattern, false otherwise
     */
    public static boolean matches(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }
        return Pattern.compile(regex).matcher(input).matches();
    }

    /**
     * Checks if the input contains the given substring, ignoring case.
     *
     * @param input     the input string
     * @param substring the substring to check
     * @return true if the input contains the substring , false otherwise
     */
    public static boolean contains(String input, String substring) {
        if (input == null || substring == null) {
            return false;
        }
        return input.contains(substring);
    }

    /**
     * Checks if the input contains the given substring, ignoring case.
     *
     * @param input     the input string
     * @param substring the substring to check
     * @return true if the input contains the substring (ignoring case), false otherwise
     */
    public static boolean containsIgnoreCase(String input, String substring) {
        if (input == null || substring == null) {
            return false;
        }
        return contains(input.toLowerCase(), substring.toLowerCase());
    }

    /**
     * 给定一个字符串和一组待匹配的字符串集合，若字符串包含集合中任意一个子串，则匹配成功
     *
     * @param input      the input string
     * @param substrings 字符串列表
     * @return true if the input contains the substrings , false otherwise
     */
    public static boolean containsAny(String input, Collection<String> substrings) {
        if (input == null || substrings == null) {
            return false;
        }
        return substrings.stream()
                .filter(Objects::nonNull)
                .anyMatch(input::contains);
    }

    /**
     * 给定一个字符串和一组待匹配的字符串集合，若字符串包含（忽略大小写）集合中任意一个子串，则匹配成功
     *
     * @param input      the input string
     * @param substrings 字符串列表
     * @return true if the input contains the substrings (ignoring case) , false otherwise
     */
    public static boolean containsIgnoreCaseAny(String input, Collection<String> substrings) {
        if (input == null || substrings == null) {
            return false;
        }
        String lowerCaseText = input.toLowerCase();
        return substrings.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(lowerCaseText::contains);
    }
}
