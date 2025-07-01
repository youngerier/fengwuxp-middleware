package com.wind.sequence;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 序列号类型，建议使用枚举实现
 *
 * @author wuxp
 * @date 2025-07-01 10:56
 **/
public interface WindSequenceType {

    /**
     * @return 序列号名称
     */
    @NotBlank
    String name();

    /**
     * @return 序列号前缀
     */
    @NotNull
    String getPrefix();

    /**
     * @return 序列号默认长度
     */
    int defaultLength();
}
