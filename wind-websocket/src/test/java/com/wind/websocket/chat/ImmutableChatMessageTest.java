package com.wind.websocket.chat;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author wuxp
 * @date 2025-05-30 17:48
 **/
class ImmutableChatMessageTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        // 注册 Java 8 时间模块
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerializeDeserialize() throws Exception {
        ChatMessageContent content = new ChatMessageContent(
                ChatMessageContentType.TEXT,
                "Hello, world!",
                Collections.singletonMap("metaKey", "metaValue")
        );

        ImmutableChatMessage message = new ImmutableChatMessage(
                "msg-001",
                "userA",
                "session123",
                Arrays.asList(content, content),
                LocalDateTime.of(2025, 5, 30, 16, 0, 0),
                1L,
                Collections.singletonMap("metaKey", "metaValue")
        );

        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(message);
        System.out.println("Serialized JSON:\n" + json);

        // 反序列化回对象
        ImmutableChatMessage deserialized = objectMapper.readValue(json, ImmutableChatMessage.class);

        // 断言字段值正确
        Assertions.assertEquals(message.getId(), deserialized.getId());
        Assertions.assertEquals(message.getFromUserId(), deserialized.getFromUserId());
        Assertions.assertEquals(message.getSessionId(), deserialized.getSessionId());
        Assertions.assertEquals(message.getBody().size(), deserialized.getBody().size());
        Assertions.assertEquals(message.getBody().get(0).getContent(), deserialized.getBody().get(0).getContent());
        Assertions.assertEquals(message.getGmtCreate(), deserialized.getGmtCreate());
        Assertions.assertEquals(message.getSequenceId(), deserialized.getSequenceId());
        Assertions.assertEquals(message.getMetadata().get("metaKey"), deserialized.getMetadata().get("metaKey"));
    }
}
