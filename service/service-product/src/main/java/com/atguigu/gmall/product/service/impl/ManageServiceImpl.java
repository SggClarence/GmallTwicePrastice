package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.print.DocFlavor;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/2 21:14 周四
 * description:
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @GmallCache(prefix = "getSpuInfoPage")
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
//        根据category3_id去查找商品信息
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id", spuInfo.getCategory3Id());
        spuInfoQueryWrapper.orderByAsc("id");
        IPage<SpuInfo> infoIPage = spuInfoMapper.selectPage(spuInfoPage, spuInfoQueryWrapper);
        return infoIPage;
    }
    @GmallCache(prefix = "baseSaleAttrList")
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
//        获取所有的基本销售属性集合
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(new QueryWrapper<BaseSaleAttr>().orderByAsc("id"));
        return baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
//        保存spuinfo信息
//        先做一个新增功能
        if (spuInfo != null) {
            spuInfoMapper.insert(spuInfo);
            Long spuId = spuInfo.getId();
            System.out.println("spuinfo信息添加完成");
            if (!spuInfo.getSpuImageList().isEmpty()) {
                spuInfo.getSpuImageList().stream().forEach(spuImage -> {
                    spuImage.setSpuId(spuId);
                    spuImageMapper.insert(spuImage);
                });

            }

            if (!spuInfo.getSpuPosterList().isEmpty()) {
                spuInfo.getSpuPosterList().stream().forEach(spuPoster -> {
                    spuPoster.setSpuId(spuId);
                    spuPosterMapper.insert(spuPoster);
                });
            }


            if (!spuInfo.getSpuSaleAttrList().isEmpty()) {
                spuInfo.getSpuSaleAttrList().stream().forEach(spuSaleAttr -> {
                    spuSaleAttr.setSpuId(spuId);
                    spuSaleAttrMapper.insert(spuSaleAttr);
                    if (!spuSaleAttr.getSpuSaleAttrValueList().isEmpty()) {
                        spuSaleAttr.getSpuSaleAttrValueList().stream().forEach(spuSaleAttrValue -> {
                            spuSaleAttrValue.setSpuId(spuId);
                            spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                            spuSaleAttrValue.setBaseSaleAttrId(spuSaleAttr.getId());
                            spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                        });
                    }

                });
            }


        }
    }
    @GmallCache(prefix = "getSpuImageList")
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(queryWrapper);
        return spuImageList;
    }

    /**
     * 根据spuId 查询销售属性集合
     *
     * @param spuId
     * @return
     */
    @GmallCache(prefix = "getSpuSaleAttrList")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
/*//        通过外键连接查找到所有的spu_sale_attr
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(new QueryWrapper<SpuSaleAttr>().eq("spu_id", spuId));
//        然后通过spuid和spu_sale_attr_id相互关联
        spuSaleAttrList.stream().forEach(spuSaleAttr -> {
//            添加属性值
            QueryWrapper<SpuSaleAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("spu_id",spuId);
            Long baseSaleAttrId = spuSaleAttr.getBaseSaleAttrId();
            queryWrapper.eq("base_sale_attr_id",baseSaleAttrId);

            QueryWrapper<SpuSaleAttrValue> queryWrapper1 = new QueryWrapper<>();
//            queryWrapper.eq("spu_id",spuId);
            queryWrapper.and();
//            具备相同baseSaleAtr

            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapper);
            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        });*/
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /**
     * 《我亦无他，唯手熟耳!》
     *
     * @param
     * @return 返回结果(请补充)
     * @Date 2023/2/6 2:35
     * @description :保存sku
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
//        添加布隆过滤器
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.add(skuInfo.getId());

//        首先理解表的关系,只有保存功能；
        int insert = skuInfoMapper.insert(skuInfo);
        System.out.println(skuInfo.toString());
//        然后对关联的三个表格进行属性添加
        if (!skuInfo.getSkuImageList().isEmpty()) {
//            关系绑定
            skuInfo.getSkuImageList().stream().forEach(skuImage -> {
                SkuImage skuImage1 = new SkuImage();
                skuImage1.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage1);
            });
        }
        if (!skuInfo.getSkuAttrValueList().isEmpty()) {
            List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
            skuAttrValueList.stream().forEach(skuAttrValue -> {
//                表格关系
                SkuAttrValue skuAttrValue1 = new SkuAttrValue();
                skuAttrValue1.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue1);
            });


        }


        if (!skuInfo.getSkuSaleAttrValueList().isEmpty()) {
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            skuSaleAttrValueList.stream().forEach(skuSaleAttrValue -> {
//                表格关系绑定
                SkuSaleAttrValue skuSaleAttrValue1 = new SkuSaleAttrValue();
                skuSaleAttrValue1.setSkuId(skuInfo.getId());
                skuSaleAttrValue1.setSpuId(skuInfo.getSpuId());
//
                skuSaleAttrValue1.setSaleAttrValueId(skuSaleAttrValue.getSaleAttrValueId());
                int insert1 = skuSaleAttrValueMapper.insert(skuSaleAttrValue1);
            });
        }

    }


    @Override
    public SkuInfo getSkuInfo(Long skuId) {
/*
//        理清思路：防止redis宕机+缓存穿透+缓存击穿+分布式锁的原子性+回旋
//        1.数据先访问redis;如果redis没有，再访问数据库；
//        2.redis有数据，返回；
//        3.redis没有数据，去访问mysql数据库；判断有没有数据；
//        4.mysql有数据，写一份给redis
//        5.mysql没有数据，写空给数据库
//        保底方法（服务器崩溃 ），访问数据库。
//        这个方法是访问redis的；
        SkuInfo skuInfo=null;
//        如果为空，就返回这个
//        设置redis的配置 KEY
        try {
            String skuKey= RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
//        从redis中获取数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);

            if (skuInfo==null){
    //            数据为空就要访问mysql；这个时候要用到分布式锁，方式缓存击穿。
             String skuLock  =RedisConst.SKUKEY_PREFIX+skuId+ RedisConst.SKULOCK_SUFFIX;
             String uuid = UUID.randomUUID().toString().replace("-","");
    //         上锁;利用setnx命令
                Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(skuLock, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
    //            判断是否获取锁
                if (aBoolean){
    //                获取到了锁；
    //                查询数据，从mysql数据库中
                    System.out.println("获取到了锁");
                    skuInfo=getInfoDB(skuId);
    //           判断数据库的值是否是空的；
                    if (skuInfo==null){
    //                    存到redis中；防止缓存穿透，并且空值不能放太久，6分钟
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    }else {
    //                    考虑锁的原子性问题，
    //                    查询数据库有值；多放一会;放6天
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
    //                    使用lua脚本；
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    //                    设置lua脚本返回的数据类型
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        // 设置lua脚本返回类型为Long
                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);
                        // 删除key 所对应的 value
                        redisTemplate.execute(redisScript, Arrays.asList(skuLock),uuid);
                        return skuInfo;
                    }{
    //                    没有获得到线程的进行自旋
    //                    线程进行等待
                        Thread.sleep(1000);
                      return getSkuInfo(skuId);
                    }

                }else {
    //              从redis中获取的数据不为空
                    return skuInfo;
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
// 为了防止缓存宕机：从数据库中获取数据
        return getInfoDB(skuId);
*/
//        练习分布式锁
/*        SkuInfo skuInfo = null;
        try {
//       先查redis
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo == null) {
    //              如果是空的，取数据库查找;避免缓存击穿，上分布式锁
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String uuid = UUID.randomUUID().toString().replace("-", "");
                Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (lock) {
    //                  获得到了锁;
                    skuInfo=getInfoDB(skuId);
                    if (skuInfo==null){
//                        防止缓存穿透；空数据缓存6分钟
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    return skuInfo;
                    }else {
//                        如果不等于Null；存放6天
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        // 设置lua脚本返回的数据类型
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                        // 设置lua脚本返回类型为Long
                        redisScript.setResultType(Long.class);
                        redisScript.setScriptText(script);
                        // 删除key 所对应的 value
                        redisTemplate.execute(redisScript,Arrays.asList(lockKey),uuid);
                        return skuInfo;
                    }
                } else {
    //                  没有得到锁
                    Thread.sleep(100);
                    getInfoDB(skuId);
                }

            } else {
    //              不是空的。返回数据
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getInfoDB(skuId);*/
        SkuInfo skuInfo = new SkuInfo();
        System.out.println("开始去redis中查找了");
//      使用redisson去解决分布式锁的原子性问题
        try {
            String skuKey=RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            skuInfo= (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo==null){
//                查询数据库，加分布式锁，
                String lockkey =RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockkey);
                boolean b = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (b){
                    try {
//                    获得到了锁
                        skuInfo=  getInfoDB(skuId);
                        if (skuInfo==null){
    //                        防止缓存穿透
                            redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo;
                        }else {

                            redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
//                        解锁
                        lock.unlock();
                    }
                }else {
                    //自旋
                    Thread.sleep(10000);
                    getSkuInfo(skuId);
                }

            }else {
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getInfoDB(skuId);
    }

    private SkuInfo getInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        // 根据skuId 查询图片列表集合
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        skuInfo.setSkuImageList(skuImageList);
//        少了两个实体类
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValue>().eq("sku_id", skuId));

        skuInfo.setSkuAttrValueList(skuAttrValueList);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectList(new QueryWrapper<SkuSaleAttrValue>().eq("sku_id", skuId));

        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        return skuInfo;
    }

    @GmallCache(prefix = "getSkuPrice")
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (null != skuInfo) {
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");

    }

    @GmallCache(prefix = "findSpuPosterBySpuId")
    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        QueryWrapper<SpuPoster> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("spu_id", spuId);
        List<SpuPoster> spuPosterList = spuPosterMapper.selectList(spuInfoQueryWrapper);
        return spuPosterList;
    }
    @GmallCache(prefix = "getAttrList")
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }
    @GmallCache(prefix = "getSkuValueIdsMap")
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        List<Map> SkuMaps = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        HashMap<Object, Object> Map1 = new HashMap<>();
        if (!CollectionUtils.isEmpty(SkuMaps)) {
            for (Map map : SkuMaps) {
                Map1.put(map.get("value_ids"), map.get("sku_id"));
            }
        }
        return Map1;
    }

    //      据spuId，skuId 查询销售属性集合
    @GmallCache(prefix = "getSpuSaleAttrListCheckBySku")
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
        return spuSaleAttrList;
    }
    @GmallCache(prefix ="getCategoryViewByCategory3Id" )
    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
//    @GmallCache(prefix = "category")
    public List<JSONObject> getBaseCategoryList() {
/*
//        根据一个或多个属性对集合中的元素进行分组
//             声明几个json 集合
        ArrayList<JSONObject> list = new ArrayList<>();
//      获取所有分类结合
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
//        循环此集合，并按照此集合进行分组
        Map<Long, List<BaseCategoryView>> category1Map  =
        baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index=1;
//        获取一级分类下的所有数据；
        for (Map.Entry<Long,List<BaseCategoryView>> entry:category1Map.entrySet()){

//            获取一级分类ID；
            Long category1Id   = entry.getKey();
//            获取一级分类下的所有集合；
            List<BaseCategoryView> category2List1   = entry.getValue();
//             jsonobject格式
            JSONObject category1  = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",category1Id);
//          一级分类名称
            category1.put("categoryName",category2List1.get(0).getCategory1Name());
//            变量迭代
            index++;
//            循环查询二级数据
            Map<Long, List<BaseCategoryView>> category2Map  = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 声明二级分类对象集合
            List<JSONObject> category2Child = new ArrayList<>();
            // 循环遍历
            for (Map.Entry<Long, List<BaseCategoryView>> entry2  : category2Map.entrySet()) {
                // 获取二级分类Id
                Long category2Id  = entry2.getKey();
                // 获取二级分类下的所有集合
                List<BaseCategoryView> category3List  = entry2.getValue();
                // 声明二级分类对象
                JSONObject category2 = new JSONObject();

                category2.put("categoryId",category2Id);
                category2.put("categoryName",category3List.get(0).getCategory2Name());
                // 添加到二级分类集合
                category2Child.add(category2);

                List<JSONObject> category3Child = new ArrayList<>();

                // 循环三级分类数据
                category3List.stream().forEach(category3View -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3View.getCategory3Id());
                    category3.put("categoryName",category3View.getCategory3Name());

                    category3Child.add(category3);
                });

                // 将三级数据放入二级里面
                category2.put("categoryChild",category3Child);

            }
            // 将二级数据放入一级里面
            category1.put("categoryChild",category2Child);
            list.add(category1);
        }
        return list;
*/
        // 声明几个json 集合
        System.out.println("接收到请求");

        ArrayList<JSONObject> list = new ArrayList<>();

        // 声明获取所有分类数据集合

        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        // 循环上面的集合并安一级分类Id 进行分组

        Map<Long, List<BaseCategoryView>> category1Map  = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 1;

        // 获取一级分类下所有数据

        for (Map.Entry<Long, List<BaseCategoryView>> entry1  : category1Map.entrySet()) {

            // 获取一级分类Id
            Long category1Id  = entry1.getKey();
            // 获取一级分类下面的所有集合
            List<BaseCategoryView> category2List1  = entry1.getValue();

            //

            JSONObject category1 = new JSONObject();

            category1.put("index", index);

            category1.put("categoryId",category1Id);
            // 一级分类名称
            category1.put("categoryName",category2List1.get(0).getCategory1Name());

            // 变量迭代

            index++;

            // 循环获取二级分类数据

            Map<Long, List<BaseCategoryView>> category2Map  = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            // 声明二级分类对象集合

            List<JSONObject> category2Child = new ArrayList<>();

            // 循环遍历

            for (Map.Entry<Long, List<BaseCategoryView>> entry2  : category2Map.entrySet()) {

                // 获取二级分类Id

                Long category2Id  = entry2.getKey();

                // 获取二级分类下的所有集合

                List<BaseCategoryView> category3List  = entry2.getValue();

                // 声明二级分类对象

                JSONObject category2 = new JSONObject();



                category2.put("categoryId",category2Id);

                category2.put("categoryName",category3List.get(0).getCategory2Name());

                // 添加到二级分类集合

                category2Child.add(category2);



                List<JSONObject> category3Child = new ArrayList<>();



                // 循环三级分类数据

                category3List.stream().forEach(category3View -> {

                    JSONObject category3 = new JSONObject();

                    category3.put("categoryId",category3View.getCategory3Id());

                    category3.put("categoryName",category3View.getCategory3Name());

                    category3Child.add(category3);
                });
                // 将三级数据放入二级里面
                category2.put("categoryChild",category3Child);
            }

            // 将二级数据放入一级里面
            category1.put("categoryChild",category2Child);

            list.add(category1);

        }

        return list;
/*
//        获取所有发分类信息
        int index=1;
//        先获取所有的一级分类信息
        ArrayList<JSONObject> list = new ArrayList<>();
//        创建json对象，放一级分类
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //根据一级分类进行排序
        Map<Long, List<BaseCategoryView>> collect = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
//        获取一级分类下的所有二级分类信息
        for (Map.Entry<Long,List<BaseCategoryView>> entry :collect.entrySet()){
//            查询二级分类,根据一级id
            Long Category1id = entry.getKey();
//            根据一级分类id获取二级分类信息
            List<BaseCategoryView> category2List = entry.getValue();

            JSONObject  category1= new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",Category1id);
            category1.put("categoryName",category2List.get(0).getCategory1Name());
            index++;
            List<JSONObject> category2Child = new ArrayList<>();
//            查询二级分类id
            Map<Long, List<BaseCategoryView>> entry2 = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long,List<BaseCategoryView>> entrytwo :entry2.entrySet()){
//            循环将三级数据添加到二级分类中去。
                Long category3id = entrytwo.getKey();
                List<BaseCategoryView> category3list = entrytwo.getValue();
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("index",index);
                jsonObject2.put("categoryId",category3id);
                jsonObject2.put("categoryName",category3list.get(0).getCategory1Name());
                category2Child.add(jsonObject2);
                List<JSONObject> category3Child = new ArrayList<>();



                // 循环三级分类数据

                baseCategoryViewList.stream().forEach(category3View -> {

                    JSONObject category3 = new JSONObject();

                    category3.put("categoryId",category3View.getCategory3Id());

                    category3.put("categoryName",category3View.getCategory3Name());



                    category3Child.add(category3);

                });
                // 将三级数据放入二级里面

                jsonObject2.put("categoryChild",category3Child);



            }


        // 将二级数据放入一级里面

        category1.put("categoryChild",category2Child);

        list.add(category1);

        }
        return list;
//        一级分类id已经完成了，需要填充二级分类id信息，

*/

    }


    /**
     * 通过品牌Id 来查询数据
     *
     * @param tmId
     * @return
     */
    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(tmId);
        return baseTrademark;
    }
}
