package com.wind.web.mertics;

import com.wind.common.WindConstants;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * ssl 证书信息指标采集
 *
 * @author wuxp
 * @date 2025-09-15 16:54
 **/
@Slf4j
public record WindSslCertificateMeticsCollector(MeterRegistry meterRegistry, Supplier<Collection<String>> hostsSupplier) {

    private static final String DEFAULT_PORT = "443";

    private static final String HOST_TAG_NAME = "host";

    private static final String SUBJECT_TAG_NAME = "subject";

    private static final String ISSUER_TAG_NAME = "issuer";

    private static final String STATUS_TAG_NAME = "status";

    private static final String METRIC_GROUP_NAME = "domain.ssl.certificate.status";

    private static final String METRIC_DESCRIPTION = "SSL certificate remaining valid days";

    /**
     * 采集 ssl 证书指标信息
     */
    public void collect() {
        Collection<String> hosts = hostsSupplier.get();
        for (String host : hosts) {
            try {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                String[] parts = parseHost(host);
                try (SSLSocket socket = (SSLSocket) factory.createSocket(parts[0], Integer.parseInt(parts[1]))) {
                    socket.startHandshake();
                    SSLSession session = socket.getSession();
                    X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];
                    Instant notAfter = cert.getNotAfter().toInstant();
                    long availableDays = Instant.now().until(notAfter, ChronoUnit.DAYS);
                    String status = availableDays > 0 ? "VALID" : "EXPIRED";
                    log.info("Domain ssl metric collect, host = {}, subject = {}, status= {}, availableDays = {}", host, cert.getSubjectX500Principal(), status, availableDays);
                    // 用标签区分域名和状态
                    Gauge.builder(METRIC_GROUP_NAME, () -> availableDays)
                            .description(METRIC_DESCRIPTION)
                            .tags(HOST_TAG_NAME, host,
                                    SUBJECT_TAG_NAME, cert.getSubjectX500Principal().getName(),
                                    ISSUER_TAG_NAME, cert.getIssuerX500Principal().getName(),
                                    STATUS_TAG_NAME, status)
                            .register(meterRegistry);
                }
            } catch (Exception exception) {
                log.warn("Domain ssl metric collect error, host = {}, message = {}", host, exception.getMessage());
                // 失败时也可以暴露为状态 -1
                Gauge.builder(METRIC_GROUP_NAME, () -> -1)
                        .description(METRIC_DESCRIPTION)
                        .tags(HOST_TAG_NAME, host,
                                SUBJECT_TAG_NAME, WindConstants.UNKNOWN,
                                ISSUER_TAG_NAME, WindConstants.UNKNOWN,
                                STATUS_TAG_NAME, "ERROR")
                        .register(meterRegistry);
            }
        }
    }

    private String[] parseHost(String host) {
        if (host.contains(":")) {
            return host.split(":");
        }
        return new String[]{host, DEFAULT_PORT};
    }
}
