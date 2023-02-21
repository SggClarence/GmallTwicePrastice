package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/5 3:46 周日
 * description: sku
 */
@RestController
@Api(tags = "商品SKU接口")
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;


    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    @GetMapping("spuImageList/{spuId}")
    @ApiOperation("根据spuId 查询spuImageList")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable Long spuId){
        List<SpuImage> spuImageList =manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    /**
     * 保存sku
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    @ApiOperation("保存sku")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        // 调用服务层
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }



}
