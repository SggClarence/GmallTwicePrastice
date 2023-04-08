package com.atguigu.gmall.controller;

import com.alibaba.fastjson.JSONObject;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.service.UserService;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/30 21:03 周四
 * description: 前端登录页面
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private  UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     * @param userInfo
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
        System.out.println("开始登录信息认证");
        UserInfo  info =  userService.login(userInfo);
        System.out.println("开始将返回信息放到map中去");


        HashMap<String, Object> map = new HashMap<>();
        if (info!=null){
            String token = UUID.randomUUID().toString().replace("-", "");
            map.put("token",token);
            map.put("NickName",info.getNickName());
            System.out.println("给前端返回："+map);

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("userId",userInfo.getId());
            jsonObject.put("ip", IpUtil.getIpAddress(request));
//            将数据写入到redis中去
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,jsonObject.toJSONString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
        }else {
            return Result.fail().message("用户名或者密码错误");
        }

        return Result.ok(map);
    }


    /**
     * 退出登录
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+ request.getHeader("token"));
        return Result.ok();
    }



}
