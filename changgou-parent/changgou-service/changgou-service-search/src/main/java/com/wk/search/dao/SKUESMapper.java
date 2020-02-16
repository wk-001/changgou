package com.wk.search.dao;

import com.wk.search.pojo.SKUInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 导入对象是SKUInfo，SKUInfo的主键ID是Long类型
 */
public interface SKUESMapper extends ElasticsearchRepository<SKUInfo,Long> {
}
