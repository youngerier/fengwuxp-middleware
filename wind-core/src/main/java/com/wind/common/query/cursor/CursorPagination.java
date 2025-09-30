package com.wind.common.query.cursor;

import com.wind.common.query.WindPagination;
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

    static <E> CursorPagination<E> next(List<E> records, int querySize, String nextCursor) {
        return of(-1L, records, querySize, nextCursor);
    }

    static <E> CursorPagination<E> of(long total, List<E> records, int querySize, String nextCursor) {
        return new ImmutableCursorPagination<>(total, records, querySize, nextCursor);
    }
}
