package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/14 23:56 周二
 * description: 给外部调用的接口
 */
@RestController
@RequestMapping("api/product")
@Api(tags = "远程接口调用的七个接口")
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    /**
     * 根据skuId获取sku基本信息与图片信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuInfo/{skuId}")
    @ApiOperation(value = "根据skuId获取sku基本信息与图片信息")
    public SkuInfo getAttrValueList(@PathVariable("skuId") Long skuId) {
//        因为这个接口是调给其他模块的，所以是不用result的。
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        return skuInfo;
    }

    /**
     * 获取sku最新价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuPrice/{skuId}")
    @ApiOperation(value = "获取sku最新价格")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return manageService.getSkuPrice(skuId);

    }

    //  根据spuId 获取海报数据
    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    @ApiOperation(value = "根据spuId 获取海报数据")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return manageService.findSpuPosterBySpuId(spuId);
    }

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getAttrList/{skuId}")
    @ApiOperation(value = "通过skuId 集合来查询数据")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId) {
        return manageService.getAttrList(skuId);
    }


    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    @ApiOperation(value = "根据spuId 查询map 集合属性")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        return manageService.getSkuValueIdsMap(spuId);
    }


    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    @ApiOperation(value = "据spuId，skuId 查询销售属性集合")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) {
        return manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }


    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    @GetMapping("inner/getCategoryView/{category3Id}")
    @ApiOperation(value = "通过三级分类id查询分类信息")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("getBaseCategoryList")
    @ApiOperation(value = "获取全部分类信息")
    public List<JSONObject> getBaseCategoryList(){
      List<JSONObject>  list =manageService.getBaseCategoryList();
        return list;
    }


    /**
     * 通过品牌Id 集合来查询数据
     * @param tmId
     * @return
     */

    @GetMapping("inner/getTrademark/{tmId}")
    @ApiOperation(value = "通过品牌Id 集合来查询数据")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId){
        return manageService.getTrademarkByTmId(tmId);
    }


}
