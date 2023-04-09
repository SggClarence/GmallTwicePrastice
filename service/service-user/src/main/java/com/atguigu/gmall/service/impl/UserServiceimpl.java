package com.atguigu.gmall.service.impl;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.mapper.UserInfoMapper;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/3/30 21:33 周四
 * description:
 */
@Service
public class UserServiceimpl implements UserService {



    @Autowired
    private UserInfoMapper userInfoMapper;


       /**
            * @Date :2023/3/30 21:58
            * @param :
            * @return :
            * @description :
            * @author :clarence
            */
    public UserInfo login(UserInfo userInfo) {
//  select * from user_info where login_name =? and passwd= ?
//        注意密码是加密：
//        先将密码进行加密。
        String passwd = userInfo.getPasswd();
//        加密工具类

        String encrypt = MD5.encrypt(passwd);
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("login_name",userInfo.getLoginName());
        userInfoQueryWrapper.eq("passwd",encrypt);
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfoQueryWrapper);
        if (!ObjectUtils.isEmpty(userInfo1)){
            return userInfo1;
        }
        return null;
    }

}
