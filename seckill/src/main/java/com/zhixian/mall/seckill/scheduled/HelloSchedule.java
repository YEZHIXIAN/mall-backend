package com.zhixian.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class HelloSchedule {

    @Scheduled(cron = "0 * * * * ?")
    public void hello() {

        log.info("hello");
    }
}
