package com.zhixian.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.coupon.dao.SeckillSessionDao;
import com.zhixian.mall.coupon.entity.SeckillSessionEntity;
import com.zhixian.mall.coupon.entity.SeckillSkuRelationEntity;
import com.zhixian.mall.coupon.service.SeckillSessionService;
import com.zhixian.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {

        List<SeckillSessionEntity> list = this.list(
                new QueryWrapper<SeckillSessionEntity>()
                        .between("start_time", startTime(), endTime())
        );

        if (list != null && !list.isEmpty()) {
            return list.stream().peek(
                    session -> {
                        Long id = session.getId();
                        List<SeckillSkuRelationEntity> relations = seckillSkuRelationService.list(
                                new QueryWrapper<SeckillSkuRelationEntity>()
                                        .eq("promotion_session_id", id)
                        );
                        session.setRelationSkus(relations);
                    }
            ).collect(Collectors.toList());
        }
        return List.of();
    }

    private String startTime() {

        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {

        LocalDate acquired = LocalDate.now().plusDays(2);
        return LocalDateTime.of(acquired, LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}