package com.wind.core;

import java.util.Map;

/**
 * 默认可写上下文变量实现
 *
 * @author wuxp
 * @date 2025-09-02 15:50
 **/
record DefaultWritableContextVariables(Map<String, Object> contextVariables) implements WritableContextVariables {
    @Override
    public WritableContextVariables putVariable(String name, Object val) {
        contextVariables.put(name, val);
        return this;
    }

    @Override
    public WritableContextVariables removeVariable(String name) {
        contextVariables.remove(name);
        return this;
    }

    @Override
    public Map<String, Object> getContextVariables() {
        return contextVariables;
    }
}