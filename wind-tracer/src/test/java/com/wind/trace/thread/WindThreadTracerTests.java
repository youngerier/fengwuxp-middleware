package com.wind.trace.thread;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Objects;

import static com.wind.common.WindConstants.LOCAL_HOST_IP_V4;
import static com.wind.common.WindConstants.TRACE_ID_NAME;

/**
 * @author wuxp
 * @date 2025-02-25 10:34
 **/
class WindThreadTracerTests {

    @Test
    void testGetTranceId() {
        Assertions.assertNotNull(WindThreadTracer.TRACER.getTraceContext().getContextVariables());
        String traceId = WindThreadTracer.TRACER.getTraceId();
        Assertions.assertNotNull(traceId);
        WindThreadTracer.TRACER.clear();
        WindThreadTracer.TRACER.trace();
        Assertions.assertNotEquals(traceId, WindThreadTracer.TRACER.getTraceId());
        Assertions.assertNotNull(WindThreadTracer.TRACER.getTraceContext().getContextVariable(LOCAL_HOST_IP_V4));
    }

    @Test
    void testTrace() {
        String traceId = "test";
        WindThreadTracer.TRACER.trace(traceId);
        Assertions.assertEquals(traceId, WindThreadTracer.TRACER.getTraceId());
        WindThreadTracer.TRACER.trace("2");
        Assertions.assertEquals("2", WindThreadTracer.TRACER.getTraceId());
    }

    @Test
    void testTraceVariables() {
        HashMap<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("key1", "1");
        contextVariables.put("key2", 2);
        WindThreadTracer.TRACER.trace("test001", contextVariables);
        Assertions.assertEquals("1", WindThreadTracer.TRACER.getTraceContext().getContextVariable("key1"));
        Assertions.assertEquals(2, (Integer) WindThreadTracer.TRACER.getTraceContext().getContextVariable("key2"));
        WindThreadTracer.TRACER.getTraceContext().putVariable("key3", false);
        Assertions.assertEquals(false, Objects.requireNonNull(WindThreadTracer.TRACER.getTraceContext().getContextVariable("key3")));
    }

    @Test
    void testTraceClear() {
        String traceId = "test";
        WindThreadTracer.TRACER.trace(traceId);
        Assertions.assertEquals(traceId, WindThreadTracer.TRACER.getTraceId());
        WindThreadTracer.TRACER.clear();
        Assertions.assertNotEquals(traceId, WindThreadTracer.TRACER.getTraceId());
        WindThreadTracer.TRACER.clear();
        Assertions.assertNotNull(WindThreadTracer.TRACER.getTraceContext().getContextVariable(LOCAL_HOST_IP_V4));
    }

    @Test
    void testTraceNewThreadCopy() throws Exception {
        WindThreadTracer.TRACER.trace();
        String traceId = WindThreadTracer.TRACER.getTraceId();
        Assertions.assertEquals(traceId, MDC.get(TRACE_ID_NAME));
        Thread thread = new Thread(TraceContextTask.of().decorate(() -> {
            WindThreadTracer.TRACER.trace();
            Assertions.assertEquals(traceId, WindThreadTracer.TRACER.getTraceId());
            Assertions.assertEquals(traceId, MDC.get(TRACE_ID_NAME));
            WindThreadTracer.TRACER.clear();
        }));
        thread.join();
        thread.start();
        Assertions.assertEquals(traceId, WindThreadTracer.TRACER.getTraceId());

    }

    @Test
    void testTraceNewThreadNoCopy() throws Exception {
        final String traceId = "test";
        WindThreadTracer.TRACER.trace(traceId);
        Assertions.assertEquals(traceId, MDC.get(TRACE_ID_NAME));
        Thread thread = new Thread(() -> {
            WindThreadTracer.TRACER.trace();
            Assertions.assertNotEquals(traceId, WindThreadTracer.TRACER.getTraceId());
            WindThreadTracer.TRACER.clear();
        });
        thread.join();
        thread.start();
        Assertions.assertEquals(traceId, WindThreadTracer.TRACER.getTraceId());
    }

}
