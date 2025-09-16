package com.wind.web.mertics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author wuxp
 * @date 2025-09-16 09:42
 **/
class WindSslCertificateMeticsCollectorTests {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final WindSslCertificateMeticsCollector collector = new WindSslCertificateMeticsCollector(meterRegistry, () -> List.of("www.baidu.com"));


    @Test
    void testCollect() {
        collector.collect();
        List<Meter> meters = meterRegistry.getMeters();
        Assertions.assertEquals(1,meters.size());
    }
}
