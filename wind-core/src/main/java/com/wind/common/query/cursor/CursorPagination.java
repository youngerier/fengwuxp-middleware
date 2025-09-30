package com.wind.common.query.cursor;

import com.wind.common.query.WindPagination;
import com.wind.common.query.supports.QueryOrderField;
import com.wind.common.util.WindReflectUtils;
import jakarta.annotation.Nullable;
import org.springframework.util.CollectionUtils;

import java.beans.Transient;
import java.util.Collections;
import java.util.List;

/**
 * 基于游标分页查询的分页对象
 *
 * @author wuxp
 * @date 2025-09-30 13:01
 **/
public interface CursorPagination<T> extends WindPagination<T> {

    /**
     * @return 总记录数据，如果不关心总数，请返回 -1
     */
    default long getTotal() {
        return -1L;
    }

    /**
     * @return 下一页游标，如果没有则返回 null
     */
    @Nullable
    String getNextCursor();

    /**
     * @return 是否有下一页
     */
    default boolean hasNext() {
        return getNextCursor() != null;
    }

    /**
     * @return {@link #getRecords()}是否为 null 或空集合
     */
    @Transient
    default boolean isEmpty() {
        return CollectionUtils.isEmpty(getRecords());
    }

    static <E> CursorPagination<E> empty() {
        return new ImmutableCursorPagination<>(-1L, Collections.emptyList(), 0, null);
    }

    /**
     * 创建下一页分页对象，查询数据的结果对象必须有 id 字段
     *
     * @param records 分页数据
     * @param query   查询参数
     * @param <E>     分页数据类型
     * @return 分页对象
     */
    static <E> CursorPagination<E> next(List<E> records, AbstractCursorQuery<? extends QueryOrderField> query) {
        E lasted = CollectionUtils.lastElement(records);
        if (lasted == null) {
            // 说明没有数据
            return empty();
        }
        // TODO 待优化
        String cursor = QueryCursorUtils.generateCursor(query, WindReflectUtils.getFieldValue("id", lasted));
        return of(-1L, records, query.getQuerySize(), cursor);
    }

    static <E> CursorPagination<E> of(long total, List<E> records, int querySize, String nextCursor) {
        return new ImmutableCursorPagination<>(total, records, querySize, nextCursor);
    }
}
