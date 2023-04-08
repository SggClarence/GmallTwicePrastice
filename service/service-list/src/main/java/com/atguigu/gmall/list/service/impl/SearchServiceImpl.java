package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;



import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;



/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/29 16:09 周三
 * description: ES实现类
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 上架商品列表
     *
     * @param skuId
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //       要给Goods进行赋值。
        //查询sku对应的平台属性；将平台属性都赋值给ES
        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
//        将平台属性赋值给SearchAttr;同样的给实体类进行赋值，从而插入到es中去。
       if (CollectionUtils.isEmpty(attrList)){
           List<SearchAttr> searchAttrList = attrList.stream().map(attr -> {
               SearchAttr searchAttr = new SearchAttr();
               searchAttr.setAttrId(attr.getId());
               searchAttr.setAttrName(attr.getAttrName());
               List<BaseAttrValue> attrValueList = attr.getAttrValueList();
//            一个sku只有一个属性值；
               String valueName = attrValueList.get(0).getValueName();
               searchAttr.setAttrValue(valueName);
               return searchAttr;
           }).collect(Collectors.toList());
           goods.setAttrs(searchAttrList);
       }

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

//      处理品牌信息
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (trademark!=null){
            goods.setTmLogoUrl(trademark.getLogoUrl());
            goods.setTmName(trademark.getTmName());
            goods.setTmId(trademark.getId());
        }

//        再获取这个基本的属性信息
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (categoryView!=null){
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory3Name(categoryView.getCategory3Name());
        }
//        赋值基本的属性信息
//        goods.setHotScore();
//        热点数据信息先不进行处理，查看的时候在进行处理
        goods.setCreateTime(new Date());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setId(skuInfo.getId());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setTitle(skuInfo.getSkuName());

       goodsRepository.save(goods);
    }

    /**
     * 下架商品列表
     *
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }


    /**
     * 更新热点
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
//        定义数据的热度信息的 key
        String hotKey="hotScore";
// 保存数据  hostscore     sku:28   1
        Double hostscore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (hostscore%10==0){
            Optional<Goods> goodsRepositoryById = goodsRepository.findById(skuId);
            Goods goods = goodsRepositoryById.get();
            goods.setHotScore(hostscore.longValue());
            goodsRepository.save(goods);
        }
    }


    /**
     * 搜索列表
     *
     * @param searchParam
     * @return
     * @throws IOException
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
//  构建dsl语句
        // 构建dsl语句

        SearchRequest searchRequest = this.buildQueryDsl(searchParam);

        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(response);



        SearchResponseVo responseVO = this.parseSearchResult(response);

        responseVO.setPageSize(searchParam.getPageSize());

        responseVO.setPageNo(searchParam.getPageNo());

        long totalPages = (responseVO.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();

        responseVO.setTotalPages(totalPages);

        return responseVO;
    }



    private SearchRequest buildQueryDsl(SearchParam searchParam){
//      构建dsl查询语句；
//        构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        构建BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        判断查询的条件关键字是否为空
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
//            如果不是空的则进行查询；
//            查询小米手机；小米and手机两个条件
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            boolQueryBuilder.must(title);
        }

//        构建品牌查询
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
//            构造查询条件
//            trademark=2:华为
            String[] split = StringUtils.split(trademark, ":");
            if (split!=null && split.length==2)
//                根据id去查找的数据
            {
                TermQueryBuilder tmId = QueryBuilders.termQuery("tmId", split[0]);
                boolQueryBuilder.filter(tmId);
            }

        }

//        构建分类过滤 用户在点击的时候，只能点击一个值，所以此处用term
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            TermQueryBuilder category1Id = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            boolQueryBuilder.filter(category1Id);
        }

        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            TermQueryBuilder category2Id = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            boolQueryBuilder.filter(category2Id);
        }

        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            TermQueryBuilder category3Id = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            boolQueryBuilder.filter(category3Id);
        }

        // 构建平台属性查询
        // 23:4G:运行内存
        String[] props = searchParam.getProps();
        if (props.length>0 && props!=null) {
            for (String prop : props) {
                String[] split = StringUtils.split(prop, ":");
                if (split != null && split.length > 0) {
//                    构建镶嵌查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//                    嵌套查询子查询
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
//                  构建子查询中的过滤条件
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
//                   ScoreMode.Node ?
                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    boolQueryBuilder.filter(boolQuery);
                }

            }
        }
//            执行查询方法
            searchSourceBuilder.query(boolQueryBuilder);
//            构建分页
            int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(searchParam.getPageSize());

//            排序;按照价格还是金钱
            String order = searchParam.getOrder();
            if (!StringUtils.isEmpty(order)){
                String[] split = StringUtils.split(order, ":");
                if (split.length>0 && split!=null){
//                    排序的字段
                    String filed =null;
//                     数组中的第一个参数
                    switch (split[0]) {
                        case "1":
                            filed = "hotScore";
                            break;
                        case "2":
                            filed = "price";
                            break;
                    }
                    searchSourceBuilder.sort(filed,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
                }else {
//                    没有传值的时候是默认值
                    searchSourceBuilder.sort("hotScore",SortOrder.DESC);
                }

            }
//            构建高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.postTags("</span>");
            highlightBuilder.preTags("<span style=color:red>");

            searchSourceBuilder.highlighter(highlightBuilder);


//            设置品牌聚合
            TermsAggregationBuilder termsAggregationBuilder =        AggregationBuilders.terms("tmIdAgg").field("tmId")
                    .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                    .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

//            设置平台属性聚合
            searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                    .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

//          结果集过滤
            searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

            SearchRequest searchRequest  = new SearchRequest("goods");

            searchRequest.source(searchSourceBuilder);
            System.out.println("dsl:"+searchSourceBuilder.toString());

    return searchRequest;
    }
    
    
    
    /**《我亦无他，唯手熟耳!》
         * @Date 2023/3/30 2:23
         * @param
         * @return 返回结果(请补充)
         * @description :制作返回结果集
         */
    
    private SearchResponseVo parseSearchResult(SearchResponse response){
        SearchHits hits = response.getHits();

        //声明对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
//        获取品牌的集合
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo trademark = new SearchResponseTmVo();
//            获取品牌id
            trademark.setTmId((Long.parseLong(((Terms.Bucket) bucket).getKeyAsString())));

            //trademark.setTmId(Long.parseLong(bucket.getKeyAsString()));

            //获取品牌名称

            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();

            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");

            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();

            trademark.setTmName(tmName);

            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            trademark.setTmLogoUrl(tmLogoUrl);

            return trademark;

        }).collect(Collectors.toList());

        searchResponseVo.setTrademarkList(trademarkList);

        //赋值商品列表

        SearchHit[] subHits = hits.getHits();

        List<Goods> goodsList = new ArrayList<>();

        if (subHits!=null && subHits.length>0){

            //循环遍历

            for (SearchHit subHit : subHits) {

                // 将subHit 转换为对象

                Goods goods = JSONObject.parseObject(subHit.getSourceAsString(), Goods.class);



                //获取高亮

                if (subHit.getHighlightFields().get("title")!=null){

                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];

                    goods.setTitle(title.toString());

                }

                goodsList.add(goods);

            }

        }

        searchResponseVo.setGoodsList(goodsList);



        //获取平台属性数据

        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");

        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");

        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();

        if (!CollectionUtils.isEmpty(buckets)){

            List<SearchResponseAttrVo> searchResponseAttrVOS = buckets.stream().map(bucket -> {

                //声明平台属性对象

                SearchResponseAttrVo responseAttrVO = new SearchResponseAttrVo();

                //设置平台属性值Id

                responseAttrVO.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");

                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();

                responseAttrVO.setAttrName(nameBuckets.get(0).getKeyAsString());

                //设置规格参数列表

                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");

                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();


                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

                responseAttrVO.setAttrValueList(values);


                return responseAttrVO;


            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(searchResponseAttrVOS);

        }

        //获取总记录数

        searchResponseVo.setTotal(hits.getTotalHits().value);



        return searchResponseVo;

    }



}
