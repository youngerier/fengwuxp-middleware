package com.wind.security.configuration;

import com.wind.common.locks.JdkLockFactory;
import com.wind.common.locks.LockFactory;
import com.wind.security.authentication.WindAuthenticationProperties;
import com.wind.security.authentication.jwt.JwtProperties;
import com.wind.security.authentication.jwt.JwtTokenCodec;
import com.wind.security.authority.SimpleSecurityAccessOperations;
import com.wind.security.authority.WebRequestAuthorizationManager;
import com.wind.security.authority.rbac.WindRbacUserResourceService;
import com.wind.security.authority.rbac.WindSecurityRbacProperties;
import com.wind.security.core.SecurityAccessOperations;
import com.wind.security.core.rbac.RbacResourceSupplier;
import com.wind.security.core.rbac.RbacUserResourceService;
import com.wind.security.web.util.RequestMatcherUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.wind.common.WindConstants.ENABLED_NAME;
import static com.wind.common.WindConstants.TRUE;

/**
 * @author wuxp
 * @date 2023-09-26 13:00
 **/
@Configuration
@EnableConfigurationProperties(value = {com.wind.security.configuration.WindSecurityProperties.class})
@ConditionalOnProperty(prefix = com.wind.security.configuration.WindSecurityProperties.PREFIX, name = ENABLED_NAME, havingValue = TRUE, matchIfMissing = true)
public class WindSecurityAutoConfiguration {

    /**
     * jwt  配置 prefix
     */
    private static final String JWT_PREFIX = com.wind.security.configuration.WindSecurityProperties.PREFIX + ".jwt";

    /**
     * rbac 配置 prefix
     */
    public static final String RBAC_PREFIX = com.wind.security.configuration.WindSecurityProperties.PREFIX + ".rbac";


    /**
     * Authentication 配置 prefix
     */
    public static final String AUTHENTICATION_PREFIX = com.wind.security.configuration.WindSecurityProperties.PREFIX + ".authentication.crypto";

    @Bean
    @ConfigurationProperties(prefix = JWT_PREFIX)
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = RBAC_PREFIX)
    public WindSecurityRbacProperties windSecurityRbacProperties() {
        return new WindSecurityRbacProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = AUTHENTICATION_PREFIX)
    public WindAuthenticationProperties windAuthenticationProperties() {
        return new WindAuthenticationProperties();
    }

    @Bean
    @ConditionalOnBean(JwtProperties.class)
    public JwtTokenCodec jwtTokenCodec(JwtProperties properties) {
        return new JwtTokenCodec(properties);
    }

    @Bean
    @ConditionalOnMissingBean(LockFactory.class)
    public LockFactory jdkLockFactory() {
        return new JdkLockFactory();
    }

    @Bean
    @ConditionalOnMissingBean({SecurityAccessOperations.class})
    public SecurityAccessOperations securityAccessOperations(WindSecurityRbacProperties properties) {
        return new SimpleSecurityAccessOperations(properties.getRolePrefix());
    }

    @Bean
    @ConditionalOnBean({RbacResourceSupplier.class})
    @ConditionalOnProperty(prefix = RBAC_PREFIX + ".cache", name = ENABLED_NAME, havingValue = TRUE, matchIfMissing = true)
    public RbacUserResourceService windRbacUserResourceService(RbacResourceSupplier rbacResourceSupplier) {
        return new WindRbacUserResourceService(rbacResourceSupplier);
    }

    @Bean
    @ConditionalOnBean({WindSecurityRbacProperties.class, RbacResourceSupplier.class})
    public WebRequestAuthorizationManager webRequestAuthorizationManager(RbacResourceSupplier rbacResourceSupplier,
                                                                         SecurityAccessOperations securityAccessOperations,
                                                                         WindSecurityRbacProperties properties) {
        return new WebRequestAuthorizationManager(request -> {
            // 匹配当前请求需要的权限
            Map<String, Set<RequestMatcher>> matches = new HashMap<>();
            rbacResourceSupplier.getPermissions().stream()
                    .filter(Objects::nonNull)
                    .forEach(permission -> matches.put(permission.getId(), RequestMatcherUtils.convertMatchers(permission.getAttributes())));
            Set<String> result = new HashSet<>();
            for (Map.Entry<String, Set<RequestMatcher>> entry : matches.entrySet()) {
                if (RequestMatcherUtils.matches(entry.getValue(), request)) {
                    // 权限匹配
                    result.add(entry.getKey());
                    if (!properties.isMatchesRequestAllPermission()) {
                        //非匹配所有权限模式， 匹配到了则返回
                        break;
                    }
                }
            }
            return result;
        }, securityAccessOperations);
    }

}



