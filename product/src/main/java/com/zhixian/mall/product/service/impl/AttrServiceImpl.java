package com.zhixian.mall.product.service.impl;

import com.zhixian.mall.product.dao.AttrAttrgroupRelationDao;
import com.zhixian.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zhixian.mall.product.service.AttrAttrgroupRelationService;
import com.zhixian.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;

import com.zhixian.mall.product.dao.AttrDao;
import com.zhixian.mall.product.entity.AttrEntity;
import com.zhixian.mall.product.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();

        // 1. save basic attributes
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 2. save relation
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attr.getAttrGroupId());
        relationEntity.setAttrId(attrEntity.getAttrId());
        relationService.save(relationEntity);

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }

        String key = params.get("key").toString();
        if (!key.isEmpty()){
            wrapper.and(obj -> obj.eq("catelog_id", key).or().like("attr_group_name", key));

        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

}