package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/17 0:00 周五
 * description:
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;


    /**

     * 获取sku详情信息

     * @param skuId

     * @return

     */

    @GetMapping("{skuId}")

    public Result getItem(@PathVariable Long skuId){

        Map<String,Object> result = itemService.getItemBySkuId(skuId);
        System.out.println("到item层了");
        return Result.ok(result);
    }



//    显示的时候按照热点顺序进行显示





}
