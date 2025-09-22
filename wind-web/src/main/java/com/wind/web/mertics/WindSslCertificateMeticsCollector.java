package com.wind.web.mertics;

import com.wind.common.WindConstants;
import com.wind.common.exception.BaseException;
import com.wind.common.exception.DefaultExceptionCode;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

/**
 * ssl 证书信息指标采集
 *
 * @author wuxp
 * @date 2025-09-15 16:54
 **/
@Slf4j
public record WindSslCertificateMeticsCollector() {

    private static final String DEFAULT_PORT = "443";

    private static final String HOST_TAG_NAME = "host";

    private static final String SUBJECT_TAG_NAME = "subject";

    private static final String ISSUER_TAG_NAME = "issuer";

    private static final String STATUS_TAG_NAME = "status";

    private static final String METRIC_GROUP_NAME = "domain.ssl.certificate.status";

    private static final String METRIC_DESCRIPTION = "SSL certificate remaining valid days";

    @NotNull
    public HostSslCertificateInfo getHostSslCertificateInfo(String host) {
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
                return new HostSslCertificateInfo(host, cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName(), status, (int) availableDays,
                        cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        notAfter.atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (Exception exception) {
            log.warn("Domain ssl metric collect error, host = {}, message = {}", host, exception.getMessage());
            throw new BaseException(DefaultExceptionCode.COMMON_ERROR, "get host ssl certificate info error", exception);
        }
    }

    /**
     * 采集 ssl 证书指标信息
     */
    public void collect(MeterRegistry registry, Collection<String> hosts) {
        for (String host : hosts) {
            try {
                HostSslCertificateInfo info = getHostSslCertificateInfo(host);
                Gauge.builder(METRIC_GROUP_NAME, () -> info.availableDays)
                        .description(METRIC_DESCRIPTION)
                        .tags(SUBJECT_TAG_NAME, info.subject(),
                                HOST_TAG_NAME, info.host(),
                                STATUS_TAG_NAME, info.status())
                        .register(registry);
            } catch (Exception exception) {
                // 失败时也可以暴露为状态 -1
                Gauge.builder(METRIC_GROUP_NAME, () -> -1)
                        .description(METRIC_DESCRIPTION)
                        .tags(SUBJECT_TAG_NAME, WindConstants.UNKNOWN,
                                HOST_TAG_NAME, host,
                                ISSUER_TAG_NAME, WindConstants.UNKNOWN,
                                STATUS_TAG_NAME, WindConstants.UNKNOWN)
                        .register(registry);
            }
        }
    }

    private String[] parseHost(String host) {
        if (host.contains(":")) {
            return host.split(":");
        }
        return new String[]{host, DEFAULT_PORT};
    }

    public record HostSslCertificateInfo(String host, String subject, String issuer, String status, int availableDays, LocalDateTime beginTime, LocalDateTime endTime) {

    }
}
