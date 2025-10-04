package com.wind.common.config;

import com.alibaba.fastjson2.JSON;
import com.wind.common.WindConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

/**
 * @author wuxp
 * @date 2025-09-24 13:35
 **/
class WindSystemConfigRepositoryTests {

    private final WindSystemConfigRepository repository = new WindSystemConfigRepository(mockStorage());

    @Test
    void testRequireConfig() {
        String result = repository.requireConfig("test", String.class, WindConstants.EMPTY);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(WindConstants.EMPTY, result);
    }

    @Test
    void testGetJsonConfig() {
        Map<String, Object> result = repository.getJsonConfig("EXAMPLE_JSON");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get("a"));
    }

    private static SystemConfigStorage mockStorage() {
        SystemConfigStorage result = Mockito.mock(SystemConfigStorage.class);
        Mockito.when(result.getConfig(eq("EXAMPLE_JSON"))).thenReturn(JSON.toJSONString(Map.of("a", "1")));
        return result;
    }
}
