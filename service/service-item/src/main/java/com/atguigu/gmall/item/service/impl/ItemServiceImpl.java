package com.atguigu.gmall.item.service.impl;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/17 0:30 周五
 * description:
 */
@Service
public class ItemServiceImpl  implements ItemService {

    @Resource
    private ProductFeignClient productFeignClient;

    @Autowired
    private ListFeignClient listFeignClient;

    @Autowired
    private ThreadPoolExecutor executor;
/*    @Autowired
    private ListFeignClient listFeignClient;*/

    /**
     * 获取sku详情信息
     *
     * @param skuId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getItemBySkuId(Long skuId) {

//        将item页面需要的信息都给返回回去
        HashMap<String, Object> map = new HashMap<>();
/*//        初始化布隆过滤器
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)){
            return map;
        }*/
//        因为布隆过滤器的效率比较低；所以先不用布隆过滤器。


//        先调用skuinfo；好像和三级分类有关系；
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = (CompletableFuture<SkuInfo>) CompletableFuture.supplyAsync(
                ()->{
                    SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                    Long spuId = skuInfo.getSpuId();
                    map.put("skuInfo",skuInfo);
                    System.out.println("skuName的属性值为"+skuInfo.getSkuName());
                    return skuInfo;
                },executor
        );

        CompletableFuture<Void> categoryViewfuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
                    if (skuInfo!=null) {
//        这个skuinfo信息是满的；然后获取三级分类
                        Long category3Id = skuInfo.getCategory3Id();
                        BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
                        map.put("categoryView", categoryView);
                    }    }, executor

        );


        CompletableFuture<Void> posterBySpuIdfuture = skuInfoCompletableFuture.thenAcceptAsync((Consumer<? super SkuInfo>) skuInfo -> {
            List<SpuPoster> spuPosterBySpuId = productFeignClient.getSpuPosterBySpuId(skuInfo.getSpuId());
            map.put("spuPosterList", spuPosterBySpuId);
        }, executor);


        CompletableFuture<Void> spuSaleAttrListCheckBySkufuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, executor);

        CompletableFuture<Void> skuValueIdsMapfuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            //        获取valuesSkuJson数据
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String s = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", s);
        }, executor);

//      更新商品的热度信息；每次查询都会调用这个进行更新处理。
        CompletableFuture<Void> incrHotScoreCompletableFuture  = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, executor);


        CompletableFuture<Void> skuPricefuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);

        }, executor);




//        查询平台属性
        CompletableFuture<Void> attrListfuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
//       这个是因为前后端属性名不一致的问题处理
            List<HashMap<String, String>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("attrName", baseAttrInfo.getAttrName());
                hashMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return hashMap;
            }).collect(Collectors.toList());
            map.put("skuAttrList", skuAttrList);
        }, executor);


/*
        CompletableFuture<Void> incrHotScorefuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, executor);
*/


        //组合编排
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewfuture,
                skuPricefuture,
                posterBySpuIdfuture,
                attrListfuture,
                skuValueIdsMapfuture,
                spuSaleAttrListCheckBySkufuture,
                incrHotScoreCompletableFuture
        ).join();


        return map;

    }
}
