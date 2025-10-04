package com.wind.common;

/**
 * 区间对象
 *
 * @param minimum The minimum value in this range (inclusive).
 * @param maximum The maximum value in this range (inclusive).
 * @author wuxp
 * @date 2024-08-28 18:33
 */
public record WindRange<T extends Comparable<T>>(T minimum, T maximum, boolean openRight) {

    /**
     * 创建一个左闭又开的区间
     *
     * @param left  左值
     * @param right 右值
     * @return 区间
     */
    public static <T extends Comparable<T>> WindRange<T> leftCloseRightOpen(final T left, final T right) {
        return new WindRange<>(left, right, true);
    }

    /**
     * 创建一个左右的均闭的区间
     *
     * @param left  左值
     * @param right 右值
     * @return 区间
     */
    public static <T extends Comparable<T>> WindRange<T> between(final T left, final T right) {
        return new WindRange<>(left, right, false);
    }

    public static <T extends Comparable<T>> WindRange<T> between(T[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Byte> between(byte[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Short> between(short[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Integer> between(int[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Long> between(long[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Float> between(float[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    public static WindRange<Double> between(double[] range) {
        return new WindRange<>(range[0], range[1], false);
    }

    /**
     * @param element 匹配的元素
     * @return 是否在 range 范围内
     */
    public boolean contains(T element) {
        if (element == null) {
            return false;
        }
        if (openRight) {
            return element.compareTo(minimum) >= 0 && element.compareTo(maximum) < 0;
        } else {
            return element.compareTo(minimum) >= 0 && element.compareTo(maximum) <= 0;
        }
    }
}
