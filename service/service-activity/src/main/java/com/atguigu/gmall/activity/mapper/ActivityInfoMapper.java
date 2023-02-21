package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {

    Page<ActivityInfo> selectActivityInfoPages(long pages, long size);
}
