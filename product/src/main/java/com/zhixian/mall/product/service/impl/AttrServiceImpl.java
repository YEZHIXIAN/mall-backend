package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.constant.ProductConstant;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.AttrDao;
import com.zhixian.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zhixian.mall.product.entity.AttrEntity;
import com.zhixian.mall.product.entity.AttrGroupEntity;
import com.zhixian.mall.product.entity.CategoryEntity;
import com.zhixian.mall.product.service.AttrAttrgroupRelationService;
import com.zhixian.mall.product.service.AttrGroupService;
import com.zhixian.mall.product.service.AttrService;
import com.zhixian.mall.product.service.CategoryService;
import com.zhixian.mall.product.vo.AttrGroupRelationVo;
import com.zhixian.mall.product.vo.AttrResponseVo;
import com.zhixian.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    CategoryService categoryService;


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
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();

            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationService.save(relationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq(
                        "attr_type",
                        "base".equalsIgnoreCase(type)
                                ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                                : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }

        String key = params.get("key").toString();
        if (!key.isEmpty()) {
            wrapper.and(obj -> obj.eq("catelog_id", key).or().like("attr_group_name", key));

        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        List<AttrEntity> records = page.getRecords();

        // Convert attrEntity to attrResponseVo.
        List<AttrResponseVo> responseVos = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, attrResponseVo);

            if ("base".equalsIgnoreCase(type)) {
                // Get attribute group name.
                AttrAttrgroupRelationEntity relation = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attrEntity.getAttrId()));

                if (relation != null && relation.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(relation.getAttrGroupId());
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            // Get catelog name.
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVo;
        }).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attrEntity, attrResponseVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1. Set attr group information
            AttrAttrgroupRelationEntity relation =
                    relationService
                            .getOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                                    .eq("attr_id", attrId));

            if (relation != null) {
                attrResponseVo.setAttrGroupId(relation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(relation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 2. Get attr catelog information.
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        List<Long> path = new ArrayList<>();
        if (categoryEntity != null) {
            categoryService.findParentPath(categoryEntity.getCatId(), path);
            attrResponseVo.setCatelogName(categoryEntity.getName());
            attrResponseVo.setCatelogPath(path.toArray(new Long[0]));
        }

        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // Update related group.
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());

            int count = relationService.count(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attr.getAttrId())
            );

            if (count > 0) {
                relationService.update(
                        relationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId())
                );
            } else {
                relationService.save(relationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relations = relationService.list(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_group_id", attrgroupId)
        );

        List<Long> attrIds = relations.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());
        if (attrIds.isEmpty()) {
            return null;
        }
        return (List<AttrEntity>) this.listByIds(attrIds);
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List.of(vos).forEach(vo -> {
            relationService.remove(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", vo.getAttrId())
                            .eq("attr_group_id", vo.getAttrGroupId())
            );
        });
    }

    @Override
    public PageUtils getNoRelationAttr(Long attrgroupId, Map<String, Object> params) {
        // 1. Get category to which the group belongs.
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 2. Get attributes that don't have relation.

        // 2.1. Get groups under the category.
        List<AttrGroupEntity> attrGroups = attrGroupService.list(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", catelogId)
        );
        List<Long> attrGroupIds = attrGroups
                .stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        // 2.2. Get attributes' ids under other groups.
        List<AttrAttrgroupRelationEntity> relations =
                relationService.list(
                        new QueryWrapper<AttrAttrgroupRelationEntity>()
                                .in("attr_group_id", attrGroupIds)
                );

        List<Long> attrIds = relations
                .stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        // 2.3. Get attributes that have no relation under the category .

        QueryWrapper<AttrEntity> wrapper =
                new QueryWrapper<AttrEntity>()
                        .eq("catelog_id", catelogId)
                        .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        if (!attrIds.isEmpty()) {
            wrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (!key.isEmpty()) {
            wrapper.and((w) -> w.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }
}