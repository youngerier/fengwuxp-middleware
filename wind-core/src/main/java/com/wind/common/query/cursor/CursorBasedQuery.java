package com.wind.common.query.cursor;

import com.wind.common.query.WindQuery;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * 基于游标分页查询对象
 *
 * @author wuxp
 * @date 2025-09-30 13:08
 **/
public interface CursorBasedQuery<OrderField> extends WindQuery<OrderField> {

    /**
     * @return 游标
     */
    @Nullable
    String getCursor();

    void setCursor(@NotNull String cursor);
}
