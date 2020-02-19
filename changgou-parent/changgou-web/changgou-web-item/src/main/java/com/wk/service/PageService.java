package com.wk.service;

public interface PageService {

    /**
     * 根据spuId生成sku静态页
     * @param spuId
     */
    void createStaticHtml(Long spuId);
}
