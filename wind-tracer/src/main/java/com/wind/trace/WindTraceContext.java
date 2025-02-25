package com.wind.trace;

import com.wind.core.WritableContextVariables;

import javax.validation.constraints.NotBlank;

/**
 * trace context
 *
 * @author wuxp
 * @date 2023-10-20 10:31
 **/
public interface WindTraceContext extends WritableContextVariables {

    /**
     * 获取 trace id
     *
     * @return traceId
     */
    @NotBlank
    String getTraceId();

    /**
     * clear trace context
     */
    void clear();
}
