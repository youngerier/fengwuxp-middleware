package com.wind.sequence;

import com.wind.common.exception.AssertUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 数字自增序列号生成器
 *
 * @author wuxp
 * @date 2023-10-18 08:18
 **/
public record NumericSequenceGenerator(AtomicLong counter, int length) implements SequenceGenerator {

    public NumericSequenceGenerator(AtomicLong counter) {
        this(counter, 8);
    }

    public NumericSequenceGenerator() {
        this(new AtomicLong(0));
    }

    @Override
    public String next() {
        long seq = counter.incrementAndGet();
        AssertUtils.isTrue(String.valueOf(seq).length() <= length, "sequence exceeds maximum length");
        String format = "%0" + length + "d";
        return String.format(format, seq);
    }
}
