package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 环绕通知实现分布式锁+缓存
     * @param joinPoint
     * @return
     * @throws Throwable
     *
     *
     *    sku:skuId
     *  1.尝试从redis中获取数据
     *      定义key
     *    获取添加了注解的方法
     *    获取方法上的注解
     *    获取注解的属性prefix
     *    获取方法的参数
     * 2.获取redis的结果，进行判断
     *    有返回
     *    没有
     *     定义锁的key
     *     获取锁
     *     加锁
     *        获取了锁
     *         mysql
     *              有值无值处理
     *        没有锁
     *        自旋重试
     *
     *  兜底的操作
     *
     *
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCatchAspectOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object object=new Object();
        Object[] args = new Object[0];
        try {
            //定义key sku：30
            //获取添加了注解的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取方法上的注解
            GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
            //获取注解的前缀 key
            String prefix = annotation.prefix();
            //获取方法参数
            args = joinPoint.getArgs();
            //拼接key
            String key=prefix+ Arrays.toString(args);
            //尝试获取
//            object = redisTemplate.opsForValue().get(key);
            object=this.cacheHit(key,signature);
            //判断是否从redis中获取了数据
            if(object==null){

                //加锁
                //定义锁
                String lockKey=key+ RedisConst.SKULOCK_SUFFIX;
                //获取锁
                RLock lock = redissonClient.getLock(lockKey);
                //加锁
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断是否加锁成功
                if(result){
                    //查询数据库
                    //执行方法体
                    object = joinPoint.proceed(args);
                    //判断是否获取了结果
                    try {
                        if(object==null){
//                            Object object1=new Object();
                            //获取返回值类型
                            Class returnType = signature.getReturnType();
                            //通过反射创建对象
                            Object obj = returnType.newInstance();

                            //存储到redis
                            //JSON.toJSONString(object1) 存储json字符串到redis
                            this.redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(obj),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);

                            return obj;
                        }else{

                            this.redisTemplate.opsForValue().setIfAbsent(key, JSON.toJSONString(object),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                            return object;
                        }
                    } finally {
                        //释放锁
                        lock.unlock();
                    }
                }else{
                    //等一会
                    Thread.sleep(100);
                    //自旋
                    return gmallCatchAspectOperation(joinPoint);
                }
            }else{
                //缓存中有数据，直接返回
                return object;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return joinPoint.proceed(args);
    }

    /**
     * 处理返回值结果
     * @param key
     * @param signature
     * @return
     */
    private Object cacheHit(String key, MethodSignature signature) {

        //获取redis数据
        String strJson  = (String) this.redisTemplate.opsForValue().get(key);

        //获取返回值类型  skuInfo
        Class returnType = signature.getReturnType();
        if(!StringUtils.isEmpty(strJson)){


            //根据当前类型转换 将字符串转换成指定的类型 {}
//            if(strJson.equals("{}")){
//
//                try {
//                    return returnType.newInstance();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else{
//
//                return JSON.parseObject(strJson, returnType);
//            }
            return JSON.parseObject(strJson, returnType);


        }


        return null;
    }


}
