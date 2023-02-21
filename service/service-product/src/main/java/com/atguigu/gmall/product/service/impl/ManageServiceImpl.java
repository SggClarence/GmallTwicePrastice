package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/2 21:14 周四
 * description:
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;


    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
//        根据category3_id去查找商品信息
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        spuInfoQueryWrapper.orderByAsc("id");
        IPage<SpuInfo> infoIPage = spuInfoMapper.selectPage(spuInfoPage,spuInfoQueryWrapper);
        return infoIPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
//        获取所有的基本销售属性集合
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(new QueryWrapper<BaseSaleAttr>().orderByAsc("id"));
        return baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
//        保存spuinfo信息
//        先做一个新增功能
        if (spuInfo!=null) {
            spuInfoMapper.insert(spuInfo);
            Long spuId = spuInfo.getId();
            System.out.println("spuinfo信息添加完成");
            if (!spuInfo.getSpuImageList().isEmpty()){
                spuInfo.getSpuImageList().stream().forEach(spuImage -> {
                    spuImage.setSpuId(spuId);
                    spuImageMapper.insert(spuImage);
                });

            }

            if (!spuInfo.getSpuPosterList().isEmpty()){
                spuInfo.getSpuPosterList().stream().forEach(spuPoster -> {
                    spuPoster.setSpuId(spuId);
                    spuPosterMapper.insert(spuPoster);
                });
            }


            if (!spuInfo.getSpuSaleAttrList().isEmpty()){
                spuInfo.getSpuSaleAttrList().stream().forEach(spuSaleAttr ->{
                    spuSaleAttr.setSpuId(spuId);
                    spuSaleAttrMapper.insert(spuSaleAttr);
                     if (!spuSaleAttr.getSpuSaleAttrValueList().isEmpty()){
                         spuSaleAttr.getSpuSaleAttrValueList().stream().forEach(spuSaleAttrValue ->{
                                 spuSaleAttrValue.setSpuId(spuId);
                                 spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                                 spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttr.getId());
                                 spuSaleAttrValueMapper.insert(spuSaleAttrValue);});
                     }

                });
            }


        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(queryWrapper);
        return spuImageList;
    }

     /** 根据spuId 查询销售属性集合
        * @param spuId
        * @return
         */

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
/*//        通过外键连接查找到所有的spu_sale_attr
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(new QueryWrapper<SpuSaleAttr>().eq("spu_id", spuId));
//        然后通过spuid和spu_sale_attr_id相互关联
        spuSaleAttrList.stream().forEach(spuSaleAttr -> {
//            添加属性值
            QueryWrapper<SpuSaleAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("spu_id",spuId);
            Long baseSaleAttrId = spuSaleAttr.getBaseSaleAttrId();
            queryWrapper.eq("base_sale_attr_id",baseSaleAttrId);

            QueryWrapper<SpuSaleAttrValue> queryWrapper1 = new QueryWrapper<>();
//            queryWrapper.eq("spu_id",spuId);
            queryWrapper.and();
//            具备相同baseSaleAtr

            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        });*/
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

        /**《我亦无他，唯手熟耳!》
             * @Date 2023/2/6 2:35
             * @param
             * @return 返回结果(请补充)
             * @description :保存sku
             */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
//        首先理解表的关系,只有保存功能；
        int insert = skuInfoMapper.insert(skuInfo);
        System.out.println(skuInfo.toString());
//        然后对关联的三个表格进行属性添加
        if (!skuInfo.getSkuImageList().isEmpty()){
//            关系绑定
            skuInfo.getSkuImageList().stream().forEach(skuImage -> {
                SkuImage skuImage1 = new SkuImage();
                skuImage1.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage1);
            });
        }
        if (!skuInfo.getSkuAttrValueList().isEmpty()){
            List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
            skuAttrValueList.stream().forEach(skuAttrValue -> {
//                表格关系
                SkuAttrValue skuAttrValue1 = new SkuAttrValue();
                skuAttrValue1.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue1);
            });


        }


        if (!skuInfo.getSkuSaleAttrValueList().isEmpty()){
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            skuSaleAttrValueList.stream().forEach(skuSaleAttrValue -> {
//                表格关系绑定
                SkuSaleAttrValue skuSaleAttrValue1 = new SkuSaleAttrValue();
                skuSaleAttrValue1.setSkuId(skuInfo.getId());
                skuSaleAttrValue1.setSpuId(skuInfo.getSpuId());
//
                skuSaleAttrValue1.setSaleAttrValueId(skuSaleAttrValue.getSaleAttrValueId());
                int insert1 = skuSaleAttrValueMapper.insert(skuSaleAttrValue1);
            });
        }

    }


    @Override
    public SkuInfo getSkuInfo(Long skuId) {
//        这个方法是不对的；
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        // 根据skuId 查询图片列表集合
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        skuInfo.setSkuImageList(skuImageList);
//        少了两个实体类
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValue>().eq("sku_id", skuId));

        skuInfo.setSkuAttrValueList(skuAttrValueList);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectList(new QueryWrapper<SkuSaleAttrValue>().eq("sku_id", skuId));

        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        return skuInfo;

    }


    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(null != skuInfo) {
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");

    }


    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        QueryWrapper<SpuPoster> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("spu_id",spuId);
        List<SpuPoster> spuPosterList = spuPosterMapper.selectList(spuInfoQueryWrapper);
        return spuPosterList;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        List<Map> SkuMaps = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        HashMap<Object, Object> Map1 = new HashMap<>();
        if (!CollectionUtils.isEmpty(SkuMaps)){
            for (Map map: SkuMaps ){
                Map1.put(map.get("value_ids"), map.get("sku_id"));
            }
        }
        return Map1;
    }
//      据spuId，skuId 查询销售属性集合
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList   =spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrList;
    }

    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }
}
