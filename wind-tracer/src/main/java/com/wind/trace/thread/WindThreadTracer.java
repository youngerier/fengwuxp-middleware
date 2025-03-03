package com.wind.trace.thread;


import com.wind.common.util.IpAddressUtils;
import com.wind.sequence.SequenceGenerator;
import com.wind.trace.WindTraceContext;
import com.wind.trace.WindTracer;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.wind.common.WindConstants.LOCAL_HOST_IP_V4;
import static com.wind.common.WindConstants.TRACE_ID_NAME;

/**
 * 线程上下文 trace工具类
 *
 * @author wuxp
 * @date 2023-12-29 09:57
 **/
public final class WindThreadTracer implements WindTracer {

    /**
     * traceId 生成器
     */
    private static final SequenceGenerator TRACE_GENERATOR = () -> SequenceGenerator.randomAlphanumeric(32);

    /**
     * 线程 trace context
     */
    private static final ThreadLocal<Map<String, Object>> TRACE_CONTEXT = ThreadLocal.withInitial(HashMap::new);

    @Override
    public void trace() {
        trace(getTraceId());
    }

    @Override
    public void trace(String traceId) {
        trace(traceId, Collections.emptyMap());
    }

    @Override
    public void trace(String traceId, @NotNull Map<String, Object> contextVariables) {
        putVariable(TRACE_ID_NAME, StringUtils.hasText(traceId) ? traceId : TRACE_GENERATOR.next());
        putVariable(LOCAL_HOST_IP_V4, IpAddressUtils.getLocalIpv4WithCache());
        Objects.requireNonNull(contextVariables, "argument contextVariables must not null").forEach(this::putVariable);
    }

    @Override
    public WindTraceContext getTraceContext() {
        Map<String, Object> contextVariables = Collections.unmodifiableMap(TRACE_CONTEXT.get());
        return new WindTraceContext() {
            @Override
            public String getTraceId() {
                String traceId = getContextVariable(TRACE_ID_NAME);
                if (traceId == null) {
                    // 没有则生成
                    traceId = TRACE_GENERATOR.next();
                    putVariable(TRACE_ID_NAME, traceId);
                }
                return traceId;
            }

            @Override
            public Map<String, Object> getContextVariables() {
                return contextVariables;
            }
        };
    }

    @Override
    public void putVariable(String name, Object val) {
        Map<String, Object> variables = TRACE_CONTEXT.get();
        variables.put(name, val);
        if (val instanceof String) {
            // 字符传类型变量同步到 MDC 中， TODO 待优化
            MDC.put(name, (String) val);
        }
    }

    @Override
    public void clear() {
        MDC.clear();
        Map<String, Object> variables = TRACE_CONTEXT.get();
        if (variables != null) {
            variables.clear();
        }
        TRACE_CONTEXT.remove();
    }
}
