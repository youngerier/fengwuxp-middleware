package com.wind.common.query.supports;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * 通用的分页查询对象
 *
 * @author wuxp
 * @date 2025-01-16 16:20
 **/
public interface WindPageQuery<OrderField> {

    /**
     * @return 查询页码
     */
    @NotNull
    Integer getQueryPage();

    void setQueryPage(@NotNull Integer queryPage);

    /**
     * @return 查询大小
     */
    @NotNull
    Integer getQuerySize();

    void setQuerySize(@NotNull Integer querySize);

    /**
     * @return 查询类型
     */
    @NotNull
    QueryType getQueryType();

    void setQueryType(@NotNull QueryType queryType);

    /**
     * 排序字段和排序类型安装数组顺序一一对应
     *
     * @return 排序字段
     */
    OrderField[] getOrderFields();

    void setOrderFields(OrderField[] orderFields);

    /**
     * @return 排序类型
     */
    @Null
    QueryOrderType[] getOrderTypes();

    void setOrderTypes(@NotNull QueryOrderType[] orderTypes);
}
