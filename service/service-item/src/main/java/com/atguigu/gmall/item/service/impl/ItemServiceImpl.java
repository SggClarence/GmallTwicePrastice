package com.atguigu.gmall.item.service.impl;




import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
//        先调用skuinfo；好像和三级分类有关系；
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        Long spuId = skuInfo.getSpuId();
        map.put("skuInfo",skuInfo);
        System.out.println("skuName的属性值为"+skuInfo.getSkuName());
        if (skuInfo!=null) {
//        这个skuinfo信息是满的；然后获取三级分类
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            map.put("categoryView", categoryView);

            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);
            List<SpuPoster> spuPosterBySpuId = productFeignClient.getSpuPosterBySpuId(spuId);
            map.put("spuPosterList", spuPosterBySpuId);
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, spuId);
            map.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
//        获取valuesSkuJson数据
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(spuId);
            String s = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", s);

        }
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
//       这个是因为前后端属性名不一致的问题处理
            List<HashMap<String, String>> skuAttrList = attrList.stream().map(baseAttrInfo -> {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("attrName", baseAttrInfo.getAttrName());
                hashMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                return hashMap;
            }).collect(Collectors.toList());
            map.put("skuAttrList",skuAttrList);
        return map;
    }
}
