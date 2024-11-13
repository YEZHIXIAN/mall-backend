package com.zhixian.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.product.dao.AttrAttrgroupRelationDao;
import com.zhixian.mall.product.dao.AttrDao;
import com.zhixian.mall.product.dao.AttrGroupDao;
import com.zhixian.mall.product.dao.SkuSaleAttrValueDao;
import com.zhixian.mall.product.entity.*;
import com.zhixian.mall.product.service.AttrAttrgroupRelationService;
import com.zhixian.mall.product.service.AttrGroupService;
import com.zhixian.mall.product.service.ProductAttrValueService;
import com.zhixian.mall.product.vo.AttrGroupWithAttrsVo;
import com.zhixian.mall.product.vo.SkuItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catalogId) {
        IPage<AttrGroupEntity> page;

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isNullOrEmpty(key)) {
            wrapper.and(obj -> obj.eq("attr_id", key).or().like("attr_name", key));
        }

        if (catalogId != 0) {
            wrapper.eq("catalog_id", catalogId);
        }
        page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * Retrieves a list of attribute groups along with their attributes for a given catalog ID.
     *
     * @param catalogId the ID of the catalog for which the attribute groups and their attributes are to be retrieved
     * @return a list of attribute groups with their associated attributes for the specified catalog ID
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatalogId(Long catalogId) {
        List<AttrGroupEntity> attrGroups = this.list(
                new QueryWrapper<AttrGroupEntity>().eq("catalog_id", catalogId)
        );

        return attrGroups.stream().map((attrGroup) -> {
            // 1. Get relations of the attrGroup with their attrs.
            List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_group_id", attrGroup.getAttrGroupId())
            );

            // 2. Get attrs under the attrGroup using relations.
            List<AttrEntity> attrs = relations
                    .stream()
                    .map((relation) -> attrDao.selectById(relation.getAttrId()))
                    .collect(Collectors.toList());

            // 3. Create vo with attrs.
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroup, vo);
            vo.setAttrs(attrs);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos) {
        // 查询当前spu对应的所有属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        Map<Long, Integer> attrGroupCount = new HashMap<>();
        Map<Long, List<SkuItemVo.SpuBaseAttrVo>> attrGroupAttrsMap = new HashMap<>();

        List<AttrGroupEntity> attrGroupEntities = new ArrayList<>(productAttrValueEntities.stream()
                .map((productAttrValueEntity) -> {
                    // 拿到属性id
                    Long attrId = productAttrValueEntity.getAttrId();
                    AttrEntity attrEntity = attrDao.selectById(attrId);

                    // 查出每个属性所属的属性分组
                    AttrAttrgroupRelationEntity relation = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
                    Long attrGroupId = relation.getAttrGroupId();
                    if (attrGroupCount.containsKey(attrGroupId)) {
                        attrGroupCount.put(attrGroupId, -1);
                    } else {
                        attrGroupCount.put(attrGroupId, 1);
                    }

                    // 若为基本属性，则添加到对应的属性分组下
                    if (attrEntity.getAttrType() == 0) {
                        SkuItemVo.SpuBaseAttrVo baseAttrVo = new SkuItemVo.SpuBaseAttrVo();
                        baseAttrVo.setAttrName(attrEntity.getAttrName());
                        baseAttrVo.setAttrValue(productAttrValueEntity.getAttrValue());
                        if (attrGroupAttrsMap.containsKey(attrGroupId)) {
                            attrGroupAttrsMap.get(attrGroupId).add(baseAttrVo);
                        } else {
                            attrGroupAttrsMap.put(attrGroupId, new ArrayList<>(List.of(baseAttrVo)));
                        }
                    }
                    // 若为销售属性，则添加到销售属性列表中
                    else {
                        SkuItemVo.SkuItemSaleAttrVo saleAttrVo = new SkuItemVo.SkuItemSaleAttrVo();
                        saleAttrVo.setAttrId(attrId);
                        saleAttrVo.setAttrName(attrEntity.getAttrName());
                        List<String> values = List.of(productAttrValueEntity.getAttrValue().split(";"));
                        List<SkuItemVo.AttrValueWithSkuIdVo> collect = values.stream()
                                .map(
                                        (value) -> {
                                            List<SkuSaleAttrValueEntity> attrValue = skuSaleAttrValueDao.selectList(new QueryWrapper<SkuSaleAttrValueEntity>().eq("attr_value", value));
                                            StringBuilder skuIds = new StringBuilder();
                                            for (SkuSaleAttrValueEntity skuSaleAttrValueEntity : attrValue) {
                                                skuIds.append(skuSaleAttrValueEntity.getSkuId());
                                            }
                                            SkuItemVo.AttrValueWithSkuIdVo attrValueWithSkuIdVo = new SkuItemVo.AttrValueWithSkuIdVo();
                                            attrValueWithSkuIdVo.setAttrValue(value);
                                            attrValueWithSkuIdVo.setSkuIds(skuIds.toString());
                                            return attrValueWithSkuIdVo;
                                        }
                                ).collect(Collectors.toList());
                        saleAttrVo.setAttrValues(collect);
                        saleAttrVos.add(saleAttrVo);
                    }
                    return attrGroupDao.selectById(attrGroupId);

                })
                .collect(Collectors.toMap(
                        AttrGroupEntity::getAttrGroupId,
                        entity -> entity,
                        (oldValue, newValue) -> oldValue
                ))
                .values());

        return attrGroupEntities.stream()
                .map((attrGroupEntity -> {
                    SkuItemVo.SpuItemAttrGroupVo attrGroupVo = new SkuItemVo.SpuItemAttrGroupVo();
                    attrGroupVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId()));

                    // 设置当前属性分组下的所有基本属性
                    List<SkuItemVo.SpuBaseAttrVo> spuBaseAttrVos = attrGroupAttrsMap.get(attrGroupEntity.getAttrGroupId());

                    attrGroupVo.setAttrs(spuBaseAttrVos);
                    attrGroupVo.setGroupName(attrGroupEntity.getAttrGroupName());

                    return attrGroupVo;
                })).collect(Collectors.toList());
    }

}