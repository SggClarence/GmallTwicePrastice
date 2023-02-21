package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface activityManageService {
    Page<ActivityInfo> getActivityInfo(Page<ActivityInfo> activityInfoPage);

    Page<CouponInfo> getcouponInfo(Page<CouponInfo> couponInfoPage);

    void saveActivityInfo(ActivityInfo activityInfo);

    void saveCouponInfo(CouponInfo couponInfo);

    void batchRemoveActivityInfo(Long[] activityid);

    void batchRemoveCouponInfo(Long[] couponInfoId);

    void activityInfoRemove(Long activityId);

    void couponInfoRemove(Long couponInfoId);

    ActivityInfo getActivityInfoId(Long activityId);

    CouponInfo getCouponInfoId(Long couponInfoId);

    void couponInfoUpdate(CouponInfo couponInfo);

    void activityInfoUpdate(ActivityInfo activityInfo);

    ActivityRuleVo findActivityRuleList(Long activityId);

    void saveCouponRule(CouponRuleVo couponRuleVo);

    CouponRuleVo findCouponRuleList(Long couponInfoId);
}
