package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/7 2:01 周二
 * description: 商品首页展示
 */
@Controller
public class IndexController {


    @Resource
    private ProductFeignClient productFeignClient;

    @GetMapping({"/","index.html"})
    public String index(HttpServletRequest request){
//      此处thymeleaf解析出错。
        List result = productFeignClient.getBaseCategoryList();
        request.setAttribute("list",result);
        return "index/index";
    }





}
