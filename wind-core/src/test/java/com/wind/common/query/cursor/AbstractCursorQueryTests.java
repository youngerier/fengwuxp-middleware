package com.wind.common.query.cursor;

import com.wind.common.query.supports.DefaultOrderField;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wuxp
 * @date 2025-09-30 13:43
 **/
class AbstractCursorQueryTests {


    @Test
    void testCursor() {
        ExampleQuery query = new ExampleQuery();
        query.setName("zhans");
        query.setMinGmtCreate(LocalDateTime.now());
        long lastRecordId = 1L;
        String cursor = QueryCursorUtils.generateCursor(query, lastRecordId);
        query.setCursor(cursor);
        Assertions.assertEquals(String.valueOf(lastRecordId), query.asTextId());
        Assertions.assertEquals(lastRecordId, query.asId());
    }


    @Data
    public static class ExampleQuery extends AbstractCursorQuery<DefaultOrderField> {

        private String name;

        private List<String> tags;

        private LocalDateTime minGmtCreate;

        private LocalDateTime maxGmtCreate;
    }
}
