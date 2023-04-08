package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/30 23:20 周四
 * description: 登录模块开发
 */
@Controller
public class PassportController {

    /**
     *
     * @return
     */

//    转发
        /**
             * @Date :2023/3/31 12:40
             * @param :[request]
             * @return :java.lang.String
             * @description :
             * @author :clarence
             */
        @GetMapping("login.html")
    public String login(HttpServletRequest request){
        System.out.println("转发到login页面");
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "login";
    }


}
