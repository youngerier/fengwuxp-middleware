package com.wind.common.query.cursor;

import com.wind.common.query.supports.QueryOrderField;
import com.wind.common.query.supports.QueryOrderType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.beans.Transient;

/**
 * @author wuxp
 * @date 2025-09-30 13:12
 **/
@Data
public abstract class AbstractCursorQuery<OrderField extends QueryOrderField> implements CursorBasedQuery<OrderField> {

    /**
     * 查询大小
     */
    @NotNull
    private Integer querySize = 20;

    /**
     * 排序字段
     */
    private OrderField[] orderFields;

    /**
     * 排序类型
     */
    private QueryOrderType[] orderTypes;

    /**
     * 游标
     */
    private String cursor;

    @Nullable
    @Transient
    public String asTextId() {
        return QueryCursorUtils.checkCursorAndGetLastRecordId(this);
    }

    @Nullable
    @Transient
    public Long asId() {
        String result = asTextId();
        return result == null ? null : Long.valueOf(result);
    }

}
