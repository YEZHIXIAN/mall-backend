package com.zhixian.mall.coupon.controller;

import com.zhixian.mall.common.to.SkuReductionTo;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.coupon.entity.SkuFullReductionEntity;
import com.zhixian.mall.coupon.service.SkuFullReductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;



/**
 * 商品满减信息
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:30:59
 */
@RestController
@RequestMapping("coupon/skufullreduction")
public class SkuFullReductionController {

    @Autowired
    private SkuFullReductionService skuFullReductionService;

    /**
     * 列表
     */
    @PostMapping("/saveInfo")
    public R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo) {

        skuFullReductionService.saveSkuReduction(skuReductionTo);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SkuFullReductionEntity skuFullReduction = skuFullReductionService.getById(id);

        return R.ok().put("skuFullReduction", skuFullReduction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.save(skuFullReduction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuFullReductionEntity skuFullReduction){
		skuFullReductionService.updateById(skuFullReduction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		skuFullReductionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
