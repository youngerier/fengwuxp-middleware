package com.wind.sentinel.util;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.wind.common.exception.BaseException;
import com.wind.sentinel.DefaultSentinelResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author wuxp
 * @date 2024-06-19 10:29
 **/
class SentinelFlowLimitUtilsTests {

    @BeforeEach
    void setup() {
        ParamFlowRuleManager.loadRules(Collections.singletonList(paramLimitRule()));
        FlowRuleManager.loadRules(Collections.singletonList(getLimitRule()));
    }

    @Test
    void testLimitPass() {
        DefaultSentinelResource resource = buildResource("test");
        SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        });
        Assertions.assertNull(ContextUtil.getContext());
    }

    @Test
    void testLimitPassBlock() {
        DefaultSentinelResource resource = buildResource("test");
        SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        });
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        }));
        Assertions.assertEquals("request to many", exception.getMessage());
    }

    @Test
    void testParamLimitPass() {
        DefaultSentinelResource resource = buildResource("test_p");
        SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        });
        SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        });
    }


    @Test
    void testParamLimitBlock() {
        DefaultSentinelResource resource = buildResource("test_p");
        resource.setArgs(Collections.singletonList(1));
        SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        });
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        }));
        Assertions.assertEquals("request to many", exception.getMessage());
    }

    @Test
    void testParamLimitBlockWithZhans() throws Exception {
        DefaultSentinelResource resource = buildResource("test_p");
        resource.setArgs(Collections.singletonList("zhans"));
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> SentinelFlowLimitUtils.limit(resource, () -> {
            Assertions.assertEquals(resource.getName(), ContextUtil.getContext().getName());
        }));
        Assertions.assertEquals("request to many", exception.getMessage());
    }

    private static DefaultSentinelResource buildResource(String name) {
        DefaultSentinelResource resource = new DefaultSentinelResource();
        resource.setName(name);
        resource.setContextName(name);
        resource.setResourceType(0);
        resource.setEntryType(EntryType.IN);
        return resource;
    }

    private FlowRule getLimitRule() {
        FlowRule result = new FlowRule();
        result.setResource("test");
        result.setCount(2);
        return result;
    }

    private ParamFlowRule paramLimitRule() {
        ParamFlowRule result = new ParamFlowRule();
        result.setResource("test_p");
        result.setCount(1);
        result.setParamIdx(0);
        ParamFlowItem item = new ParamFlowItem();
        item.setCount(0);
        item.setObject("zhans");
        item.setClassType(String.class.getName());
        result.setParamFlowItemList(Collections.singletonList(item));
        return result;
    }
}
