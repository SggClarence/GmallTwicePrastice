package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.JdkIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/3 0:29 周五
 * description:
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;


    /**
     * 根据三级分类获取品牌
     *
     * @param category3Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
//        两表联查
        QueryWrapper<BaseCategoryTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarksList = baseCategoryTrademarkMapper.selectList(baseTrademarkQueryWrapper);
//        获得到了多个trademark_id
        if (!CollectionUtils.isEmpty(baseCategoryTrademarksList)){
            List<BaseTrademark> collect = baseCategoryTrademarksList.stream().map(trademark -> {
                BaseTrademark baseTrademark = baseTrademarkMapper.selectById(trademark.getTrademarkId());
                return baseTrademark;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    /**
     * 获取当前未被三级分类关联的所有品牌
     *
     * @param   category3Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
//    先查询所有品牌
        QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
        List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(queryWrapper.orderByAsc("id"));
//    再查询和三级分类未关联的品牌
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id",category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);
//    从所有的找未关联的
        if (!baseCategoryTrademarkList.isEmpty()){
//            先获得的关联的trademarkId
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(baseCategoryTrademark -> {
                Long trademarkId = baseCategoryTrademark.getTrademarkId();
                return trademarkId;
            }).collect(Collectors.toList());

            List<BaseTrademark> collect = baseTrademarkList.stream().filter(baseTrademark -> !trademarkIdList.contains(baseTrademark.getId())).collect(Collectors.toList());
            return collect;
        }
        return baseTrademarkList;
    }

    /**
     * 删除关联
     *
     * @param category3Id
     * @param trademarkId
     */
    @Override
    public void remove(Long category3Id, Long trademarkId) {
//          逻辑删除： 本质更新操作 is_deleted
        QueryWrapper<BaseCategoryTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        queryWrapper.eq("trademark_id",trademarkId);
        int delete = baseCategoryTrademarkMapper.delete(queryWrapper);
    }


    /**
     * 保存分类与品牌关联
     *
     * @param categoryTrademarkVo
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
        baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        trademarkIdList.stream().forEach(trademarkId->{
            baseCategoryTrademark.setTrademarkId(trademarkId);
            BaseTrademark baseTrademark = baseTrademarkMapper.selectById(trademarkId);
            baseCategoryTrademark.setBaseTrademark(baseTrademark);
            int insert = baseCategoryTrademarkMapper.insert(baseCategoryTrademark);
        });
    }
}
