package com.wind.security.authority;

import com.wind.common.WindConstants;
import com.wind.security.core.SecurityAccessOperations;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Objects;

/**
 * @param rolePrefix 默认的角色值前缀
 * @author wuxp
 * @date 2023-10-24 08:06
 */
public record SimpleSecurityAccessOperations(String rolePrefix) implements SecurityAccessOperations {

    public SimpleSecurityAccessOperations() {
        this(WindConstants.EMPTY);
    }

    @Override
    public boolean hasAnyAuthority(String... authorities) {
        return isGranted(AuthorityAuthorizationManager.hasAnyAuthority(authorities));
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        return isGranted(AuthorityAuthorizationManager.hasAnyRole(rolePrefix, roles));
    }

    private boolean isGranted(AuthorizationManager<Object> manager) {
        return Objects.requireNonNull(manager.authorize(SecurityContextHolder.getContext()::getAuthentication, Collections.emptyList())).isGranted();
    }
}
