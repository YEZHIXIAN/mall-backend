package com.zhixian.mall.product.app;

import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.product.entity.AttrEntity;
import com.zhixian.mall.product.entity.AttrGroupEntity;
import com.zhixian.mall.product.service.AttrAttrgroupRelationService;
import com.zhixian.mall.product.service.AttrGroupService;
import com.zhixian.mall.product.service.impl.AttrServiceImpl;
import com.zhixian.mall.product.service.impl.CategoryServiceImpl;
import com.zhixian.mall.product.vo.AttrGroupRelationVo;
import com.zhixian.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private AttrServiceImpl attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 查询属性分组下的关联属性
     *
     * @param attrgroupId 属性分组id
     * @return 该属性分组下的关联属性
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrs = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attrs);
    }

    /**
     * 查询属性分组下的未关联属性
     *
     * @param attrgroupId 属性分组id
     * @return 该属性分组下的关联属性
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(attrgroupId, params);
        return R.ok().put("data", page);
    }

    /**
     * Retrieves the attribute groups along with their attributes for a specified catalog ID.
     *
     * @param catalogId The ID of the catalog to retrieve attribute groups and their attributes from.
     * @return An instance of {@link R} containing the attribute groups and their attributes for the specified catalog ID.
     */
    @GetMapping("/{catalogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catalogId") Long catalogId) {
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatalogId(catalogId);
        return R.ok().put("data", vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catalogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catalogId) {
        PageUtils page = attrGroupService.queryPage(params, catalogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catalogId = attrGroup.getCatalogId();
        Long[] catalogPath = categoryService.findCatalogPath(catalogId);
        attrGroup.setCatalogPath(catalogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 删除关联属性分组
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);

        return R.ok();
    }

    /**
     * 为分组新增关联属性
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

}
