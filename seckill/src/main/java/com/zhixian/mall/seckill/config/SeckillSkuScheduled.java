package com.zhixian.mall.seckill.config;

import com.zhixian.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        log.info("Scheduled: uploadSeckillSkuLatest3Days");
        seckillService.uploadSeckillSkuLatest3Days();
    }

}
