package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

public interface SearchService {
//    在商品管理模块进行上下架管理，在es中查询是否存在。

    /**
     * 上架商品列表
     * @param skuId
     */
    void upperGoods(Long skuId);



    /**
     * 下架商品列表
     * @param skuId
     */
    void lowerGoods(Long skuId);


    /**
     * 更新热点
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**

     * 搜索列表

     * @param searchParam

     * @return

     * @throws IOException

     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;




}
