package com.wind.server.logging;

import com.wind.script.auditlog.ScriptAuditLogRecorder;
import com.wind.script.auditlog.AuditLogRecorder;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * web 环境下记录操作日志支持
 *
 * @author wuxp
 * @date 2023-09-23 10:40
 **/
public class WebAuditLogRecorder extends ScriptAuditLogRecorder {

    public WebAuditLogRecorder(AuditLogRecorder recorder) {
        super(recorder, WebAuditLogRecorder::getRequestVariables);
    }

    private static Map<String, Object> getRequestVariables() {
        HttpServletRequest httpRequest = getHttpServletRequest();
        if (httpRequest == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        // 填充 http request 山下文中的变量
        Enumeration<String> attributeNames = httpRequest.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object attribute = httpRequest.getAttribute(name);
            if (attribute != null) {
                result.put(name, attribute);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Nullable
    private static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
}
