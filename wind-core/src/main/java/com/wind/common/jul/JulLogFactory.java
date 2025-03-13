package com.wind.common.jul;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

/**
 * @author wuxp
 * @date 2025-03-12 18:54
 **/
public final class JulLogFactory {

    private JulLogFactory() {
        throw new AssertionError();
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        Logger result = Logger.getLogger(name);
        result.addHandler(new ConsoleHandler());
        return result;
    }
}
