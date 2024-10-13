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
    if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
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

        if (relation != null) {
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

      int count = relationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>()
          .eq("attr_id", attr.getAttrId()));

      if (count > 0) {
        relationService.update(
            relationEntity,
            new UpdateWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_id", attr.getAttrId()));
      } else {
        relationService.save(relationEntity);
      }
    }
  }
}