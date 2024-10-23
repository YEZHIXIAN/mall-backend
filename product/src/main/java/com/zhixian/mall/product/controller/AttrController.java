package com.zhixian.mall.product.controller;

import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.product.entity.ProductAttrValueEntity;
import com.zhixian.mall.product.service.AttrService;
import com.zhixian.mall.product.service.ProductAttrValueService;
import com.zhixian.mall.product.vo.AttrResponseVo;
import com.zhixian.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AttrController is a REST controller for managing product attributes.
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * Get a list of base attributes for a specific product (SPU).
     *
     * @param spuId the ID of the product (SPU)
     * @return a response containing the list of base attributes
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data", entities);
    }

    /**
     * Update the attributes associated with a specific product (SPU).
     *
     * @param spuId the ID of the product (SPU)
     * @param entities the list of attributes to update
     * @return a response indicating the update status
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId, entities);
        return R.ok();
    }

    /**
     * Get a paginated list of attributes for a specific category and type.
     *
     * @param params the pagination and filter parameters
     * @param catalogId the ID of the category
     * @param type the type of attributes
     * @return a response containing the paginated list of attributes
     */
    @GetMapping("/{attrType}/list/{catalogId}")
    public R baseAttrList(
            @RequestParam Map<String, Object> params,
            @PathVariable("catalogId") Long catalogId,
            @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryBaseAttrPage(params, catalogId, type);
        return R.ok().put("page", page);
    }

    /**
     * Get a paginated list of all attributes.
     *
     * @param params the pagination and filter parameters
     * @return a response containing the paginated list of attributes
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * Get information about a specific attribute.
     *
     * @param attrId the ID of the attribute
     * @return a response containing the attribute information
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrResponseVo attr = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * Save a new attribute.
     *
     * @param attr the attribute to save
     * @return a response indicating the save status
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * Update an existing attribute.
     *
     * @param attr the attribute to update
     * @return a response indicating the update status
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);
        return R.ok();
    }

    /**
     * Delete one or more attributes.
     *
     * @param attrIds the IDs of the attributes to delete
     * @return a response indicating the delete status
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));
        return R.ok();
    }
}