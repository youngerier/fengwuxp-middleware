package com.wind.common.spring;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * 排序支持的 {@link ApplicationListener}
 *
 * @author wuxp
 * @date 2024-10-30 15:23
 **/
public interface OrderedApplicationEventListener<E extends ApplicationEvent> extends ApplicationListener<E>, Ordered {

}
