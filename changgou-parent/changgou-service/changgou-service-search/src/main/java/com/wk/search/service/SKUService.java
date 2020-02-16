package com.wk.search.service;

import java.util.Map;

public interface SKUService {

    /**
     * 条件搜索
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String,String> searchMap);

    /**
     * 导入数据到索引库
     */
    void importData();
}
