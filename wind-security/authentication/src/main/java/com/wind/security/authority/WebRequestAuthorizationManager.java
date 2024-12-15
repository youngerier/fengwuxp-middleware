package com.wind.security.authority;

import com.wind.security.core.SecurityAccessOperations;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.function.Supplier;

import static com.wind.security.WebSecurityConstants.REQUEST_REQUIRED_AUTHORITIES_ATTRIBUTE_NAME;

/**
 * 基于请求的 rbac 权限控制
 *
 * @author wuxp
 * @date 2023-10-23 08:52
 **/
@AllArgsConstructor
@Slf4j
public class WebRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final AuthorizationDecision ACCESS_PASSED = new AuthorizationDecision(true);

    private static final AuthorizationDecision ACCESS_DENIED = new AuthorizationDecision(false);

    private final WebRequestAuthorityLoader webRequestAuthorityLoader;

    private final SecurityAccessOperations securityAccessOperations;

    @Nullable
    @Override
    public AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context) {
        if (!supplier.get().isAuthenticated()) {
            // 未登录
            return ACCESS_PASSED;
        }
        if (securityAccessOperations.isSupperAdmin()) {
            // 超级管理员
            log.debug("supper admin, allow access");
            return ACCESS_PASSED;
        }
        Collection<String> authorities = webRequestAuthorityLoader.load(context.getRequest());
        if (ObjectUtils.isEmpty(authorities)) {
            log.debug("no permission required, allow access");
            // 请求路径没有配置权限，表明该请求接口可以任意访问
            return ACCESS_PASSED;
        }
        if (log.isDebugEnabled()) {
            log.debug("request resource ={} {}, require authorities = {}", context.getRequest().getMethod(), context.getRequest().getRequestURI(),
                    authorities);
        }
        context.getRequest().setAttribute(REQUEST_REQUIRED_AUTHORITIES_ATTRIBUTE_NAME, authorities);
        return securityAccessOperations.hasAnyAuthority(authorities.toArray(new String[0])) ? ACCESS_PASSED : ACCESS_DENIED;
    }
}
