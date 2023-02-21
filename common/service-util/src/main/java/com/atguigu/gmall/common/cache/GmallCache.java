package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

/**
 * 元注解：
 *    修饰注解的注解
 *  Target：使用位置
 *  Retention：声明周期
 *
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCache {

    String prefix() default "cache";
}
