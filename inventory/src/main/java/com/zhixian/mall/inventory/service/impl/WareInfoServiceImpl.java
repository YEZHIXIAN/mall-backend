package com.zhixian.mall.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mysql.cj.util.StringUtils;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.inventory.dao.WareInfoDao;
import com.zhixian.mall.inventory.entity.WareInfoEntity;
import com.zhixian.mall.inventory.feign.UserFeignService;
import com.zhixian.mall.inventory.service.WareInfoService;
import com.zhixian.mall.inventory.vo.FareVo;
import com.zhixian.mall.inventory.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    UserFeignService userFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isNullOrEmpty(key) && !"0".equals(key)) {
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();

        // 获取用户信息
        R info = userFeignService.info(addrId);
        MemberAddressVo memberAddressVo = info.getData(new TypeReference<>() {});

        if (memberAddressVo != null) {
            // 设置运费
            String phone = memberAddressVo.getPhone();
            BigDecimal fare = new BigDecimal(phone.substring(0, 1));
            fareVo.setFare(fare);

            // 设置地址
            fareVo.setAddress(memberAddressVo);
            return fareVo;

        }
        return null;
    }

}