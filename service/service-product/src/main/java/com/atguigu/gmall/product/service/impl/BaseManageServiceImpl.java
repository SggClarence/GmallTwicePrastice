package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.BaseManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/1/27 14:42 周五
 * description: 商品service接口实现类
 */
@Service
public class BaseManageServiceImpl implements BaseManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;


    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @return 商品的全部一级分类列表
     * @Date 2023/1/27 14:41
     * @description :
     */
    @Override
    public List<BaseCategory1> getCategory1() {
//        调用mapper信息
        QueryWrapper<BaseCategory1> baseCategory1QueryWrapper = new QueryWrapper<>();
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(baseCategory1QueryWrapper);
        return baseCategory1List;
    }

    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @param category1Id
     * @return 商品的全部二级分类列表
     * @Date 2023/1/27 14:41
     * @description :
     */
    @Override
    public List<BaseCategory2> getCategory2(long category1Id) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
        return baseCategory2s;
    }

    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @param category2Id
     * @return 商品的全部三级分类列表
     * @Date 2023/1/27 14:41
     * @description :
     */
    @Override
    public List<BaseCategory3> getCategory3(long category2Id) {
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id",category2Id);
        List<BaseCategory3> baseCategory3s = baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
        return baseCategory3s;
    }

    /**
     * 根据分类Id 获取平台属性数据
     * 接口说明：
     *      1，平台属性可以挂在一级分类、二级分类和三级分类
     *      2，查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3，查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     *         取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4，查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *         取出对应一级分类、二级分类与三级分类对应的平台属性
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
//        三个接口共同使用
        System.out.println("找问题");
        List<BaseAttrInfo> baseAttrInfoList= baseAttrInfoMapper.getAttrInfoList(category1Id,category2Id,category3Id);
        System.out.println("找后一个问题");
        return baseAttrInfoList;
    }

    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @param baseAttrInfo
     * @return 返回执行行数
     * @Date 2023/1/27 22:14
     * @description : 添加或修改商品属性信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
//        判断是添加还是修改,修改会上传一个id;
        if (baseAttrInfo.getId()!=null){
//         修改数据；因为商品信息主键的id是自增，新增没有id
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //        修改商品属性值的方法
            QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
            baseAttrValueQueryWrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(baseAttrValueQueryWrapper);
        }else {
//          新增信息
            baseAttrInfoMapper.insert(baseAttrInfo);
//            属性id是自增的
        }

//        对平台属性值进行处理
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        Long baseAttrInfoId = baseAttrInfo.getId();
        if (!CollectionUtils.isEmpty(baseAttrValueList)){
//        循环添加信息
     /*   baseAttrValueList.stream().forEach(attrValue->{
            attrValue.setAttrId(baseAttrInfo.getId());
            int insert = baseAttrValueMapper.insert(attrValue);
        });*/
            for (BaseAttrValue baseAttrValue:baseAttrValueList) {
                baseAttrValue.setAttrId(baseAttrInfoId);
                int insert = baseAttrValueMapper.insert(baseAttrValue);
                System.out.println("执行行数："+insert);
            }
        }

    }

    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @param attrId
     * @return 返回结果(请补充)
     * @Date 2023/1/29 22:37
     * @description :回显商品信息
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);
        return baseAttrValueList;
    }

    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> skuInfoPage) {
        Page<SkuInfo> skuInfoPage1 = skuInfoMapper.selectPage(skuInfoPage, new QueryWrapper<SkuInfo>().orderByAsc("id"));
        return skuInfoPage1;
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }
}
