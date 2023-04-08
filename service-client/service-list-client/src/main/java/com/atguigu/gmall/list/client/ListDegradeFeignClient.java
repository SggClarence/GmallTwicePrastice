package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/29 21:35 周三
 * description:
 */
public class ListDegradeFeignClient implements ListFeignClient{
    /**
     * 更新商品incrHotScore
     *
     * @param skuId
     * @return
     */
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    /**
     * 上架商品
     *
     * @param skuId
     * @return
     */
    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    /**
     * 下架商品
     *
     * @param skuId
     * @return
     */
    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

    /**
     * 搜索商品
     *
     * @param listParam
     * @return
     */
    @Override
    public Result list(SearchParam listParam) {
        return null;
    }
}
