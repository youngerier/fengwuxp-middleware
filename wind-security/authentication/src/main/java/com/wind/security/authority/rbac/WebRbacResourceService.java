package com.wind.security.authority.rbac;

import com.wind.common.exception.AssertUtils;
import com.wind.security.core.rbac.RbacResource;
import com.wind.security.core.rbac.RbacResourceCache;
import com.wind.security.core.rbac.RbacResourceCacheSupplier;
import com.wind.security.core.rbac.RbacResourceChangeEvent;
import com.wind.security.core.rbac.RbacResourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wind.security.WebSecurityConstants.RBAC_PERMISSION_CACHE_NAME;
import static com.wind.security.WebSecurityConstants.RBAC_ROLE_CACHE_NAME;
import static com.wind.security.WebSecurityConstants.RBAC_USER_ROLE_CACHE_NAME;

/**
 * 基于缓存的 RbacResourceService
 *
 * @author wuxp
 * @date 2023-10-22 09:50
 **/
@Slf4j
@AllArgsConstructor
public class WebRbacResourceService implements RbacResourceService, ApplicationListener<RbacResourceChangeEvent>, InitializingBean {

    private final RbacResourceCacheSupplier cacheSupplier;

    private final RbacResourceService delegate;

    /**
     * 缓存刷新间隔
     */
    private final Duration cacheRefreshInterval;

    private final ScheduledExecutorService schedule = new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory("web-rbac-resource-refresher"));

    public WebRbacResourceService(RbacResourceCacheSupplier cacheSupplier, RbacResourceService delegate) {
        this(cacheSupplier, delegate, Duration.ofMinutes(3));
    }

    @Override
    public List<RbacResource.Permission> getAllPermissions() {
        Collection<RbacResource.Permission> result = getPermissionCache().values();
        return result == null ? Collections.emptyList() : result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<RbacResource.Role> getAllRoles() {
        Collection<RbacResource.Role> result = getRoleCache().values();
        return result == null ? Collections.emptyList() : result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public RbacResource.Permission findPermissionById(String permissionId) {
        return getPermissionCache().computeIfAbsent(permissionId, delegate::findPermissionById);
    }

    @Override
    public List<RbacResource.Permission> findPermissionByIds(Collection<String> permissionIds) {
        List<RbacResource.Permission> result = getPermissionCache().getAll(permissionIds);
        return result == null ? Collections.emptyList() : result;
    }

    @Nullable
    @Override
    public RbacResource.Role findRoleById(String roleId) {
        return getRoleCache().computeIfAbsent(roleId, delegate::findRoleById);
    }

    @Nullable
    @Override
    public List<RbacResource.Role> findRoleByIds(Collection<String> roleIds) {
        List<RbacResource.Role> result = getRoleCache().getAll(roleIds);
        return result == null ? Collections.emptyList() : result;
    }

    @Override
    public Set<RbacResource.Role> findRolesByUserId(String userId) {
        Set<RbacResource.Role> result = getUserRoleCache().computeIfAbsent(userId, delegate::findRolesByUserId);
        return result == null ? Collections.emptySet() : result;
    }

    @Override
    public void onApplicationEvent(@NonNull RbacResourceChangeEvent event) {
        log.info("refresh rbac cache , type = {} , ids = {}", event.getResourceType().getName(), event.getResourceIds());
        if (event.getResourceType() == RbacResource.Permission.class) {
            if (event.isDeleted()) {
                // 权限删除
                getPermissionCache().removeAll(event.getResourceIds());
            } else {
                // 权限内容变更
                delegate.findPermissionByIds(event.getResourceIds()).forEach(this::putPermissionCaches);
            }
        }

        if (event.getResourceType() == RbacResource.Role.class) {
            if (event.isDeleted()) {
                // 角色删除
                getRoleCache().removeAll(event.getResourceIds());
            } else {
                // 角色内容变更
                delegate.findRoleByIds(event.getResourceIds()).forEach(this::putRoleCaches);
            }
        }

        if (event.getResourceType() == RbacResource.User.class) {
            // 用户角色内容变更
            event.getResourceIds().forEach(userId -> putUserRoleCaches(userId, delegate.findRolesByUserId(userId)));
        }
    }

    @Override
    public void afterPropertiesSet() {
        refreshRefresh();
    }

    private void scheduleRefresh(long delay) {
        schedule.schedule(this::refreshRefresh, delay, TimeUnit.SECONDS);
    }

    private void refreshRefresh() {
        log.debug("begin refresh rbac resource");
        try {
            delegate.getAllPermissions().forEach(this::putPermissionCaches);
            delegate.getAllRoles().forEach(this::putRoleCaches);
            // 刷新在线用户角色缓存  TODO 待优化
            getUserRoleCache().keys().forEach(userId -> putUserRoleCaches(userId, delegate.findRolesByUserId(userId)));
            log.debug("refresh rbac resource end");
        } catch (Exception exception) {
            log.error("refresh rbac resource error, message = {}", exception.getMessage(), exception);
        } finally {
            scheduleRefresh(cacheRefreshInterval.getSeconds());
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T> RbacResourceCache<String, T> requiredCache(String cacheName, RbacResourceCache.CacheLoader<String, Object> loader) {
        RbacResourceCache<String, Object> result = cacheSupplier.apply(cacheName, loader);
        AssertUtils.notNull(result, String.format("获取 Cache 失败，CacheName = %s", cacheName));
        return (RbacResourceCache<String, T>) result;
    }

    @Nonnull
    private RbacResourceCache<String, RbacResource.Permission> getPermissionCache() {
        return requiredCache(RBAC_PERMISSION_CACHE_NAME, new RbacResourceCache.CacheLoader<String, Object>() {
            @Override
            public Object load(String key) {
                return delegate.findPermissionById(key);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return delegate.getAllPermissions().stream().map(RbacResource.Permission::getId).collect(Collectors.toSet());
            }
        });
    }

    @Nonnull
    private RbacResourceCache<String, RbacResource.Role> getRoleCache() {
        return requiredCache(RBAC_ROLE_CACHE_NAME, new RbacResourceCache.CacheLoader<String, Object>() {
            @Override
            public Object load(String key) {
                return delegate.findRoleById(key);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                return delegate.getAllRoles().stream().map(RbacResource.Role::getId).collect(Collectors.toSet());
            }
        });
    }

    @Nonnull
    private RbacResourceCache<String, Set<RbacResource.Role>> getUserRoleCache() {
        return requiredCache(RBAC_USER_ROLE_CACHE_NAME, new RbacResourceCache.CacheLoader<String, Object>() {
            @Override
            public Object load(String key) {
                return delegate.findRolesByUserId(key);
            }

            @Override
            public Iterable<String> loadAllKeys() {
                // 由于已登录用户数量不确定，返回空
                return Collections.emptyList();
            }
        });
    }

    private void putPermissionCaches(RbacResource.Permission permission) {
        if (permission != null) {
            getPermissionCache().put(permission.getId(), permission);
        }
    }

    private void putRoleCaches(RbacResource.Role role) {
        if (role != null) {
            getRoleCache().put(role.getId(), role);
        }
    }

    private void putUserRoleCaches(String userId, Set<RbacResource.Role> userRoles) {
        if (ObjectUtils.isEmpty(userRoles)) {
            getRoleCache().remove(userId);
        } else {
            getUserRoleCache().put(userId, userRoles);
        }
    }

}
