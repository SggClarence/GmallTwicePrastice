package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
//    商品后台管理
public interface BaseManageService {
    /**《我亦无他，唯手熟耳!》
     * @Date 2023/1/27 14:41
     * @param  无
     * @return 商品的全部一级分类列表
     * @description :
     */
    List<BaseCategory1> getCategory1();
    /**《我亦无他，唯手熟耳!》
     * @Date 2023/1/27 14:41
     * @param  无
     * @return 商品的全部二级分类列表
     * @description :
     */
    List<BaseCategory2> getCategory2(long category1Id);
    /**《我亦无他，唯手熟耳!》
     * @Date 2023/1/27 14:41
     * @param  无
     * @return 商品的全部三级分类列表
     * @description :
     */
    List<BaseCategory3> getCategory3(long category2Id);
    /**《我亦无他，唯手熟耳!》
     * @Date 2023/1/27 17:15
     * @param  category1Id, category2Id, category3Id
     * @return 返回结果(请补充)
     * @description :getAttrInfoList根据分类id获取属性列表
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);
    /**《我亦无他，唯手熟耳!》
     * @Date 2023/1/27 22:14
     * @param  baseAttrInfo
     * @return 返回执行行数
     * @description : 添加或修改商品属性信息
     */

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    /**《我亦无他，唯手熟耳!》
         * @Date 2023/1/29 22:37
         * @param  attrId
         * @return 返回结果(请补充)
         * @description :回显商品信息
         */

    List<BaseAttrValue> getAttrValueList(Long attrId);

    IPage<SkuInfo> getPage(Page<SkuInfo> skuInfoPage);

    void onSale(Long skuId);

    void cancelSale(Long skuId);
}
