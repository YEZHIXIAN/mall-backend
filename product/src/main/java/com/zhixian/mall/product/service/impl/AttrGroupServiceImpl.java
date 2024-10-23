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
import com.zhixian.mall.product.entity.AttrAttrgroupRelationEntity;
import com.zhixian.mall.product.entity.AttrEntity;
import com.zhixian.mall.product.entity.AttrGroupEntity;
import com.zhixian.mall.product.service.AttrGroupService;
import com.zhixian.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrDao attrDao;

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

}