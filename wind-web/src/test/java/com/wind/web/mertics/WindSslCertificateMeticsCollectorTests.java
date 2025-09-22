package com.wind.web.mertics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author wuxp
 * @date 2025-09-16 09:42
 **/
@Slf4j
class WindSslCertificateMeticsCollectorTests {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final WindSslCertificateMeticsCollector collector = new WindSslCertificateMeticsCollector();

    @Test
    void testGetHostSslCertificateInfo(){
        WindSslCertificateMeticsCollector.HostSslCertificateInfo info = collector.getHostSslCertificateInfo("www.baidu.com");
        Assertions.assertEquals("CN=baidu.com,O=Beijing Baidu Netcom Science Technology Co.\\, Ltd,L=beijing,ST=beijing,C=CN", info.subject());
    }

    @Test
    void testCollect() {
        collector.collect(meterRegistry, List.of("www.baidu.com"));
        List<Meter> meters = meterRegistry.getMeters();
        Assertions.assertEquals(1, meters.size());
    }
}
