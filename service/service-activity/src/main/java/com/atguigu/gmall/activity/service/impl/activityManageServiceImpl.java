package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.*;
import com.atguigu.gmall.activity.service.activityManageService;
import com.atguigu.gmall.model.activity.*;
import com.atguigu.gmall.model.enums.ActivityType;
import com.atguigu.gmall.model.enums.CouponRangeType;
import com.atguigu.gmall.model.enums.CouponType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/8 22:58 周三
 * description: 实现类
 */
@Service
public class activityManageServiceImpl implements activityManageService {

    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Override
    public Page<ActivityInfo> getActivityInfo(Page<ActivityInfo> activityInfoPage) {
        System.out.println("第一个数："+activityInfoPage.getCurrent());
        System.out.println("第二个数："+activityInfoPage.getSize());
//        先查询实体ActivityInfo类
        QueryWrapper<ActivityInfo> activityInfoQueryWrapper = new QueryWrapper<>();
        activityInfoQueryWrapper.orderByAsc("id");
        activityInfoQueryWrapper.eq("is_deleted",0);
        List<ActivityInfo> activityInfos = activityInfoMapper.selectList(activityInfoQueryWrapper);
        List<ActivityInfo> collect = activityInfos.stream().map(activityInfo -> {
            String activityType = activityInfo.getActivityType();
            String nameByType = ActivityType.getNameByType(activityType);
            activityInfo.setActivityTypeString(nameByType);
            return activityInfo;
        }).collect(Collectors.toList());

        Page<ActivityInfo> activityInfoPage1 = activityInfoPage.setRecords(collect);

        return activityInfoPage1;
    }

    @Override
    public Page<CouponInfo> getcouponInfo(Page<CouponInfo> couponInfoPage) {
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.orderByAsc("id");
        couponInfoQueryWrapper.eq("is_deleted",0);
        List<CouponInfo> couponInfos = couponInfoMapper.selectList(couponInfoQueryWrapper);
        List<CouponInfo> collect = couponInfos.stream().map(couponInfo -> {
            String couponType = couponInfo.getCouponType();
            String nameByType = CouponType.getNameByType(couponType);
            couponInfo.setCouponTypeString(nameByType);
            String rangeType = couponInfo.getRangeType();
            String nameByType1 = CouponRangeType.getNameByType(rangeType);
            couponInfo.setRangeTypeString(nameByType1);
            return couponInfo;
        }).collect(Collectors.toList());
        Page<CouponInfo> couponInfoPage1 = couponInfoPage.setRecords(collect);
        return couponInfoPage1;
    }

    @Override
    public void saveActivityInfo(ActivityInfo activityInfo) {
        activityInfoMapper.insert(activityInfo);
    }

    @Override
    public void saveCouponInfo(CouponInfo couponInfo) {
        couponInfoMapper.insert(couponInfo);
    }


    @Override
    public void batchRemoveActivityInfo(Long[] activityid) {
        for (int i = activityid.length - 1; i >= 0; i--) {
            activityInfoMapper.deleteById(activityid[i]);
        }
    }

    @Override
    public void batchRemoveCouponInfo(Long[] couponInfoId) {
        for (int i = couponInfoId.length - 1; i >= 0; i--) {
            couponInfoMapper.deleteById(couponInfoId[i]);
        }
    }

    @Override
    public void activityInfoRemove(Long activityId) {
        activityInfoMapper.deleteById(activityId);
    }

    @Override
    public void couponInfoRemove(Long couponInfoId) {
        couponInfoMapper.deleteById(couponInfoId);
    }

    @Override
    public ActivityInfo getActivityInfoId(Long activityId) {
        ActivityInfo activityInfo = activityInfoMapper.selectById(activityId.toString());
        return activityInfo;
    }

    @Override
    public CouponInfo getCouponInfoId(Long couponInfoId) {
        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        CouponInfo couponInfo = couponInfoMapper.selectOne(queryWrapper.eq("id", couponInfoId));
        return couponInfo;
    }

    @Override
    public void couponInfoUpdate(CouponInfo couponInfo) {
    couponInfoMapper.updateById(couponInfo);
    }

    @Override
    public void activityInfoUpdate(ActivityInfo activityInfo) {
    activityInfoMapper.updateById(activityInfo);
    }

    @Override
    public ActivityRuleVo findActivityRuleList(Long activityId) {
//        查找规则  规则列表+活动范围列表
        ActivityRuleVo activityRuleVo = new ActivityRuleVo();
        activityRuleVo.setActivityId(activityId);
        QueryWrapper<ActivityRule> queryWrapper = new QueryWrapper<>();
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(queryWrapper.eq("activity_id", activityId));
        activityRuleVo.setActivityRuleList(activityRuleList);

        QueryWrapper<ActivitySku> activitySkuQueryWrapper = new QueryWrapper<>();
        activitySkuQueryWrapper.eq("activity_id",activityId);
        List<ActivitySku> activitySkus = activitySkuMapper.selectList(activitySkuQueryWrapper);
        activityRuleVo.setActivitySkuList(activitySkus);

        return activityRuleVo;
    }

    @Override
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
//        保存规则信息
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        couponRangeList.stream().forEach(couponRange -> {
            couponRangeMapper.insert(couponRange);
        });
//        更新info信息
        QueryWrapper<CouponInfo> queryWrapper = new QueryWrapper<>();
        CouponInfo couponInfo = couponInfoMapper.selectOne(queryWrapper.eq("id", couponRuleVo.getCouponId()));
        BeanUtils.copyProperties(couponRuleVo,couponInfo);
        couponInfoMapper.updateById(couponInfo);

    }

    @Override
    public CouponRuleVo findCouponRuleList(Long couponInfoId) {
//        查找活动规则coupon_info+coupon_range
        CouponRuleVo couponRuleVo1 = new CouponRuleVo();
        CouponInfo couponInfo = couponInfoMapper.selectById(couponInfoId);
        BeanUtils.copyProperties(couponInfo,couponRuleVo1);

        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",couponInfoId);
        List<CouponRange> couponRanges = couponRangeMapper.selectList(couponRangeQueryWrapper);

        couponRuleVo1.setCouponRangeList(couponRanges);
        System.out.println(couponRuleVo1.toString());
        return  couponRuleVo1;
    }
}
