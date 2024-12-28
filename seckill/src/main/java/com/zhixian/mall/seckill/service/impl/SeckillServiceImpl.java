package com.zhixian.mall.seckill.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.seckill.feign.CouponFeignService;
import com.zhixian.mall.seckill.feign.ProductFeignService;
import com.zhixian.mall.seckill.service.SeckillService;
import com.zhixian.mall.seckill.to.SeckillSkuRedisTo;
import com.zhixian.mall.seckill.vo.SeckillSessionsWithSkus;
import com.zhixian.mall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKUSTOCK_SEMAPHONE = "seckill:stock:"; // +商品随机码

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1. 扫描需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaySession();
        if (session.getCode() == 0) {

            List<SeckillSessionsWithSkus> data = session.getData(new TypeReference<>() {
            });

            saveSessionInfos(data);
            saveSessionSkuInfos(data);

        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {

        sessions.forEach(
                session -> {
                    long startTime = session.getStartTime().getTime();
                    long endTime = session.getEndTime().getTime();
                    String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
                    List<String> skuIds = session.getRelationSkus().stream().map(
                            sku -> sku.getSkuId().toString()
                    ).collect(Collectors.toList());
                    stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
                }
        );
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {

        sessions.forEach(
                session -> {
                    BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    session.getRelationSkus().forEach(
                            seckillSkuVo -> {
                                Gson gson = new Gson();
                                SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                                // 1. sku基本数据
                                R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getId());
                                if (skuInfo.getCode() == 0) {
                                    SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<>() {
                                    });
                                    redisTo.setSkuInfo(info);
                                }

                                // 2. 秒杀信息
                                BeanUtils.copyProperties(
                                        seckillSkuVo,
                                        redisTo
                                );

                                // 3. 设置时间
                                redisTo.setStartTime(session.getStartTime().getTime());
                                redisTo.setEndTime(session.getEndTime().getTime());

                                // 4. 设置随机码
                                String token = UUID.randomUUID().toString().replace("-", "");
                                redisTo.setRandomCode(token);

                                // 5. 使用库存作为分布式信号量
                                RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + token);
                                semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());

                                // 6. 存入redis
                                String json = gson.toJson(redisTo);
                                ops.put(
                                        seckillSkuVo.getId(),
                                        json
                                );
                            }
                    );

                }
        );
    }

}
