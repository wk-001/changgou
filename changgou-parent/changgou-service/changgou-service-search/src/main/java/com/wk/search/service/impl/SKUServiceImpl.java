package com.wk.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.wk.goods.feign.SKUFeign;
import com.wk.goods.pojo.Sku;
import com.wk.search.dao.SKUESMapper;
import com.wk.search.pojo.SKUInfo;
import com.wk.search.service.SKUService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SKUServiceImpl implements SKUService {

    @Autowired
    private SKUFeign skuFeign;

    @Autowired
    private SKUESMapper skuesMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;        //对ES索引库的增删改查（高级搜索）

    /**
     * 多条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //搜索条件封装
        NativeSearchQueryBuilder queryBuilder = buildBasicQuery(searchMap);

        //根据搜索条件执行搜索
        Map<String, Object> resultMap = searchList(queryBuilder);

        Map<String, Object> groupMap = searchGroup(queryBuilder, searchMap);
        resultMap.putAll(groupMap);

        return resultMap;
    }

    /**
     * 查询一次，获取category、brand、spec的聚合数据
     * @param queryBuilder
     * @return
     */
    private Map<String, Object> searchGroup(NativeSearchQueryBuilder queryBuilder,Map<String, String> searchMap) {
        /**
         * addAggregation()：添加一个聚合操作（如查询个数、分组等）
         * AggregationBuilders：聚合构建对象
         * skuCategoryGroup：域的别名
         * field("categoryName")：根据categoryName这个域进行分组查询
         */

        //类目分类查询，搜索出的数据用于显示类目搜索条件，当用户选择了某一项分类，就不必再次查询显示类目数据
        //判断查询条件searchMap中是否有分类数据，没有类目数据的情况下查询
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            queryBuilder.addAggregation(AggregationBuilders.terms("skuCategoryGroup").field("categoryName"));
        }

        //品牌分类查询，搜索出的数据用于显示品牌搜索条件，当用户选择了某一个品牌，就不必再次查询显示品牌数据
        //判断查询条件searchMap中是否有品牌数据，没有品牌数据的情况下查询
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            queryBuilder.addAggregation(AggregationBuilders.terms("skuBrandGroup").field("brandName"));
        }

        //规格查询
        queryBuilder.addAggregation(AggregationBuilders.terms("skuSpecGroup").field("spec.keyword").size(10000));

        //根据条件到ElasticSearch中查询
        AggregatedPage<SKUInfo> group = elasticsearchTemplate.queryForPage(queryBuilder.build(), SKUInfo.class);

        //存放数据
        Map<String, Object> resultMap = new HashMap<>();

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryGroup = group.getAggregations().get("skuCategoryGroup");
            resultMap.put("categoryList",getGroupList(categoryGroup));
        }


        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandGroup = group.getAggregations().get("skuBrandGroup");
            resultMap.put("brandList", getGroupList(brandGroup));
        }

        StringTerms specGroup = group.getAggregations().get("skuSpecGroup");
        List<String> specList = getGroupList(specGroup);
        resultMap.put("specList",putAllSpec(specList));

        return resultMap;
    }


    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    private List<String> getGroupList(StringTerms stringTerms) {
        /*获取类目group by分组后的数据
         * group.getAggregations()：根据别名数据，获取的是聚合数据集合，可以根据多个域进行分组
         * get("skuCategorygroup")：获取指定域的集合数据
         * */
        List<String> groupList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String fieldName = bucket.getKeyAsString();//其中一个分类的名字
            groupList.add(fieldName);
        }
        return groupList;
    }


    /**
     * 搜索条件构建对象，用于封装各种搜索条件
     * @param searchMap  搜索条件
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //组合条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size()>0) {
            
            //关键词作为条件：如果前台用search?keywords="xxx"，就用商品名搜索
            String keywords = searchMap.get("keywords");
            if (StringUtils.isNotEmpty(keywords)) {
//                queryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //类目作为过滤条件：如果前台用search?category="xxx"，就用类目作为条件进行过滤搜索
            String category = searchMap.get("category");
            if (StringUtils.isNotEmpty(category)) {
                //must相当于and
                //类目不需要分词，用termQuery
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",category));
            }
            
            //品牌作为过滤条件：如果前台用search?brand="xxx"，就用品牌名作为条件进行过滤搜索
            String brand = searchMap.get("brand");
            if (StringUtils.isNotEmpty(brand)) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",brand));
            }

            //规格作为过滤条件：如果前台用search?spec_xxx="xxx"，就用规格作为条件进行过滤搜索
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //如果key以"spec_"开头，表示规格筛选
                String key = entry.getKey();
                if(key.startsWith("spec_")){
                    //规格条件的值
                    String value = entry.getValue();
                    //搜索条件为spec_xxx去掉前面的spec_ .keyword表示不分词
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //价格区间 0-500元 500-1000元 ... 3000元以上
            String price = searchMap.get("price");
            if(StringUtils.isNotEmpty(price)){
                //去掉中文的"元"和"元以上"
                price = price.replace("元", "").replace("以上", "");
                //prices[]根据-分割[0,500],[500,1000]...[3000]
                String[] prices = price.split("-");
                if (prices.length > 0) {
                    //根据prices[]的规律，第一个值一定不为空，第二个值可能不为空，大于第一个值，小于等于第二个值
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if(prices.length==2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");          //要排序的域
            String sortRule = searchMap.get("sortRule");            //排序方式：升序/降序
            if(StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortRule)){
                queryBuilder.withSort(
                        new FieldSortBuilder(sortField)         //指定排序域
                        .order(SortOrder.valueOf(sortRule)));   //指定排序规则
            }
        }

        //分页，用户如果不传入分页参数，默认第一页
        int pageNum = converterPage(searchMap);    //默认第一页
        int size = 30;       //默认每页显示数据条数
        queryBuilder.withPageable(PageRequest.of(pageNum-1,size));

        //将boolQueryBuilder填充给NativeSearchQueryBuilder
        queryBuilder.withQuery(boolQueryBuilder);
        return queryBuilder;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer converterPage(Map<String, String> searchMap){
        if (searchMap != null) {
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                System.err.println("默认第一页");
//                e.printStackTrace();
            }
        }
        return 1;  //如果出现异常就返回第一页
    }


    /**
     * 根据搜索条件执行搜索，搜索出的结果转成对象。返回响应结果
     * @param queryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder queryBuilder) {

        //高亮配置HighlightBuilder.Field
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");  //指定高亮域
        //高亮前缀
        field.preTags("<em style=\"color:red;\">");
        //高亮后缀
        field.postTags("</em>");
        //碎片长度。关键词数据的长度，有默认值
        field.fragmentSize(20);

        //添加高亮
        queryBuilder.withHighlightFields(field);

        /**
         * 1、搜索条件封装对象
         * 2、搜索的结果集（集合数据）需要转换的类型
         * AggregatedPage<SKUInfo>：搜索结果的封装
         */
//        AggregatedPage<SKUInfo> page = elasticsearchTemplate.queryForPage(queryBuilder.build(), SKUInfo.class);
        AggregatedPage<SKUInfo> page = elasticsearchTemplate.
                queryForPage(
                        queryBuilder.build()        //搜索条件封装
                        , SKUInfo.class             //数据集合要转换类型的字节码
                        , new SearchResultMapper() {    //执行搜索后将数据结果集封装到该对象
                            @Override       //SearchResponse：搜索后响应的数据，也就是结果集
                            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                                //存储所有转换后的高亮数据对象
                                List<T> list = new ArrayList<>();

                                //执行查询，获取所有数据；数据结果集包含高亮数据+非高亮数据
                                for (SearchHit hit : searchResponse.getHits()) {
                                    //分析结果集，获取【非高亮】数据
                                    SKUInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SKUInfo.class);
                                    //分析结果集，获取指定域的高亮数据；
                                    HighlightField highlightField = hit.getHighlightFields().get("name");
                                    if (highlightField != null && highlightField.getFragments()!=null) {
                                        //读取高亮数据
                                        Text[] fragments = highlightField.getFragments();
                                        StringBuffer sb = new StringBuffer();
                                        for (Text fragment : fragments) {
                                            sb.append(fragment.toString());
                                        }
                                        //将非高亮数据中的指定域替换成高亮数据
                                        skuInfo.setName(sb.toString());
                                    }
                                    //将高亮数据添加到集合中
                                    list.add((T) skuInfo);
                                }
                                /**
                                 * 返回带有高亮的数据
                                 *  搜索的集合数据，带有高亮
                                 *  分页对象信息
                                 *  搜索记录的总条数
                                 */
                                return new AggregatedPageImpl<>(list,pageable,searchResponse.getHits().getTotalHits());
                            }
                        }
                );

        //获取数据结果集
        List<SKUInfo> skuInfoList = page.getContent();
        //分页参数总记录数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();

        //封装一个Map存储所有数据并返回
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("rows",skuInfoList);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);
        return resultMap;
    }


    /**
     * 规格汇总合并，规格只需要一种，所以用set存储
     * 将list类型的{"网络":"联通2G","颜色":"黑","存储":"16G","像素":"300万像素"}",
     *           {"网络":"联通3G","颜色":"白","存储":"32G","像素":"500万像素"}
     * 变更为Map<String, Set<String>>类型的：
     *  {"手机屏幕尺寸":["5寸","5.5寸"],"网络":["移动4G","联通4G","电信4G"],"颜色":["红","紫","白","蓝"]}
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的Map对象
        Map<String, Set<String>> allSpec = new HashMap<>();

        //循环specList，每个spec对应一条记录
        for (String s : specList) {
            //将取出的JSON数据转成Map
            Map<String,String> specMap = JSON.parseObject(s, Map.class);

            //将Map对象转换成一个Map<String,Set<String>>。规格不能重复，所以用set
            //循环所有Map
            for (Map.Entry<String, String> specEntry : specMap.entrySet()) {
                //取出当前的Map，并获取对应的key和value
                String key = specEntry.getKey();        //规格名字
                String value = specEntry.getValue();    //规格值

                /*将当前循环的数据合并到一个Map<String,Set<String>>中
                * 从allSpec中获取当前规格对应的set集合数据，如果new一个set会覆盖之前的数据*/
                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {      //如果之前没有该规格就新建一个集合
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }

    /**
     * 导入数据到索引库
     */
    @Override
    public void importData() {
        //Feign调用，查询List<Sku>
        Result<List<Sku>> skuList = skuFeign.findAll();

        /**
         * 将List<Sku>转成List<SkuInfo>
         * skuList.getData()是一个sku对象的集合，转成JSON后是一个集合里面装着集合[{skuJSON1},{skuJSON2}]
         * JSON格式只是字符串，不存在类型引用，可以转成任意对象
         */
        List<SKUInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuList.getData()),SKUInfo.class);

        //根据spec的key生成动态域
        for (SKUInfo skuInfo : skuInfoList) {
            //将tb_sku表的spec数据（JSON类型）转成map类型
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);

            /*如果需要生成动态域，只需要将该域存入一个Map对象中即可，Map对象的key会生成一个域，域的名字就是map的key
            * Map的value会作为对象对应域的值*/
            skuInfo.setSpecMap(specMap);
        }

        //调用Dao实现数据批量导入
        skuesMapper.saveAll(skuInfoList);
    }
}
