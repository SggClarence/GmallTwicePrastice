package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseManageService;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/1/24 2:24 周二
 * description: 后台商品管理系统
 */
@Controller
@Api(tags = "商品基本属性接口")
@ResponseBody
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private BaseManageService baseManageService;

    @ApiOperation("获取一级分类信息")
    @GetMapping("/getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        System.out.println("获取一级分类信息");
//        查询所有的一级分类信息
        List<BaseCategory1> baseCategory1List = baseManageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    @ApiOperation("获取二级分类信息")
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable Long category1Id){
        System.out.println("获取一级分类信息");
//        查询所有的一级分类信息
        List<BaseCategory2> baseCategory2List = baseManageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    @ApiOperation("获取三级分类信息")
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable Long category2Id){
        System.out.println("获取一级分类信息");
//        查询所有的一级分类信息
        List<BaseCategory3> baseCategory3List = baseManageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }


    @ApiOperation("根据分类id获取属性列表")
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> getAttrInfoList(@PathVariable Long category1Id,
                                                      @PathVariable Long category2Id,
                                                      @PathVariable Long category3Id){
    List<BaseAttrInfo> baseAttrInfoList=baseManageService.getAttrInfoList(category1Id,category2Id,category3Id);
        return Result.ok(baseAttrInfoList);
    }

    @ApiOperation("添加或修改商品属性信息")
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        baseManageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @ApiOperation("回显商品平台值信息")
    @GetMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable Long attrId){
        List<BaseAttrValue>  baseAttrValueList = baseManageService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }

    /**
     * SKU分页列表
     * @param page
     * @param limit
     * @return
     */
    @ApiOperation("sku分页列表")
    @GetMapping( "/list/{page}/{limit}")
    public Result SkuIndex(@PathVariable Long page,
                           @PathVariable Long limit){
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> pageModel = baseManageService.getPage(skuInfoPage);
        return Result.ok(pageModel);
    }



    /**
     * 商品上架
     * @param skuId
     * @return
     */
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        baseManageService.onSale(skuId);
        return Result.ok();
    }

    /**
     * 商品下架
     * @param skuId
     * @return
     */
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        baseManageService.cancelSale(skuId);
        return Result.ok();
    }








}
