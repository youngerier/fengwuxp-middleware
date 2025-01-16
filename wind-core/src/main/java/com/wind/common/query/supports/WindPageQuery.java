package com.wind.common.query.supports;

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
    Integer getQueryPage();

    void setQueryPage(Integer queryPage);

    /**
     * @return 查询大小
     */
    Integer getQuerySize();

    void setQuerySize(Integer querySize);

    /**
     * @return 查询类型
     */
    QueryType getQueryType();

    void setQueryType(QueryType queryType);

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
    QueryOrderType[] getOrderTypes();

    void setOrderTypes(QueryOrderType[] orderTypes);
}
