package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.activityManageService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * time: 2023/2/8 21:14 周三
 * description: 活动信息管理
 */
@RestController
@Api(tags = "活动信息管理")
public class activityManageController {

    @Autowired
    private activityManageService activityManageService;

    @ApiOperation("获取活动信息")
    @GetMapping("/admin/activity/activityInfo/{page}/{limit}")
    public Result getActivityInfo(
            @PathVariable Long page,
            @PathVariable Long limit){
        Page<ActivityInfo> activityInfoPage = new Page<>(page, limit);
        Page<ActivityInfo> infoIPage=activityManageService.getActivityInfo(activityInfoPage);
        return Result.ok(infoIPage);
    }

    @ApiOperation("获取优惠卷信息")
    @GetMapping("admin/activity/couponInfo/{page}/{limit}")
    public Result getcouponInfo(
            @PathVariable Long page,
            @PathVariable Long limit){
        Page<CouponInfo> couponInfoPage = new Page<>(page, limit);
        Page<CouponInfo> infoIPage=activityManageService.getcouponInfo(couponInfoPage);
        return Result.ok(infoIPage);
    }

    @ApiOperation("存储活动信息")
    @PostMapping("/admin/activity/activityInfo/save")
    public Result saveActivityInfo(@RequestBody ActivityInfo activityInfo){
       activityManageService.saveActivityInfo(activityInfo);
        return Result.ok();
    }

    @ApiOperation("存储优惠券信息")
    @PostMapping("/admin/activity/couponInfo/save")
    public Result saveCouponInfo(@RequestBody CouponInfo couponInfo){
       activityManageService.saveCouponInfo(couponInfo);
        return Result.ok();
    }

    @ApiOperation("活动信息批量删除")
    @DeleteMapping("admin/activity/activityInfo/batchRemove")
    public Result batchRemoveactivityInfo(@RequestBody Long[] activityid){
       activityManageService.batchRemoveActivityInfo(activityid);
        return Result.ok();
    }

    @ApiOperation("优惠券批量删除")
    @DeleteMapping("admin/activity/couponInfo/batchRemove")
    public Result batchRemoveCouponInfo(@RequestBody Long[] couponInfoId){
       activityManageService.batchRemoveCouponInfo(couponInfoId);
        return Result.ok();
    }

    @ApiOperation("活动删除")
    @DeleteMapping("admin/activity/activityInfo/remove/{activityId}")
    public Result activityInfoRemove(@PathVariable Long activityId){
       activityManageService.activityInfoRemove(activityId);
        return Result.ok();
    }

    @ApiOperation("优惠券删除")
    @DeleteMapping("admin/activity/couponInfo/remove/{couponInfoId}")
    public Result couponInfoRemove(@PathVariable Long couponInfoId){
       activityManageService.couponInfoRemove(couponInfoId);
        return Result.ok();
    }

   @ApiOperation("活动信息查询")
   @GetMapping("admin/activity/activityInfo/get/{activityId}")
   public Result getActivityInfoId(@PathVariable Long activityId){
       ActivityInfo activityInfo=activityManageService.getActivityInfoId(activityId);
        return Result.ok(activityInfo);
    }

   @ApiOperation("优惠券信息查询")
   @GetMapping("admin/activity/couponInfo/get/{couponInfoId}")
   public Result getCouponInfoId(@PathVariable Long couponInfoId){
      CouponInfo couponInfo =activityManageService.getCouponInfoId(couponInfoId);
        return Result.ok(couponInfo);
    }

   @ApiOperation("活动信息更新")
   @PutMapping("admin/activity/activityInfo/update")
   public Result activityInfoUpdate(@RequestBody ActivityInfo activityInfo){
    activityManageService.activityInfoUpdate(activityInfo);
        return Result.ok();
    }

    @ApiOperation("优惠券更新")
   @PutMapping("admin/activity/couponInfo/update")
   public Result couponInfoUpdate(@RequestBody CouponInfo couponInfo){
    activityManageService.couponInfoUpdate(couponInfo);
        return Result.ok();
    }


    @ApiOperation("查找活动规则")
   @GetMapping("admin/activity/activityInfo/findActivityRuleList/{activityId}")
   public Result findActivityRuleList(@PathVariable Long activityId){
    ActivityRuleVo activityRuleVo=activityManageService.findActivityRuleList(activityId);
        return Result.ok(activityRuleVo);
    }


   @ApiOperation("查找优惠券规则")
   @GetMapping("admin/activity/couponInfo/findCouponRuleList/{couponInfoId}")
   public Result findCouponRuleList(@PathVariable Long couponInfoId){
       System.out.println("到这里了吗");
    CouponRuleVo couponRuleList=activityManageService.findCouponRuleList(couponInfoId);
        return Result.ok(couponRuleList);
    }



    @ApiOperation("保存优惠券规则")
   @PutMapping("admin/activity/couponInfo/saveCouponRule")
   public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo){
    activityManageService.saveCouponRule(couponRuleVo);
        return Result.ok();
    }














}
