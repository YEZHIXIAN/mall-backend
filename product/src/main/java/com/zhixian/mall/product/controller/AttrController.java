package com.zhixian.mall.product.controller;

import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.R;
import com.zhixian.mall.product.service.AttrService;
import com.zhixian.mall.product.vo.AttrGroupRelationVo;
import com.zhixian.mall.product.vo.AttrResponseVo;
import com.zhixian.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-24 19:14:56
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
  @Autowired
  private AttrService attrService;

  @GetMapping("/{attrType}/list/{catelogId}")
  public R baseAttrList(
      @RequestParam Map<String, Object> params,
      @PathVariable("catelogId") Long catelogId,
      @PathVariable("attrType") String type) {

    PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
    return R.ok().put("page", page);
  }

  /**
   * 列表
   */
  @RequestMapping("/list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = attrService.queryPage(params);

    return R.ok().put("page", page);
  }


  /**
   * 信息
   */
  @RequestMapping("/info/{attrId}")
  public R info(@PathVariable("attrId") Long attrId) {
    AttrResponseVo attr = attrService.getAttrInfo(attrId);

    return R.ok().put("attr", attr);
  }

  /**
   * 保存
   */
  @RequestMapping("/save")
  public R save(@RequestBody AttrVo attr) {
    attrService.saveAttr(attr);

    return R.ok();
  }

  /**
   * 修改
   */
  @RequestMapping("/update")
  public R update(@RequestBody AttrVo attr) {
    attrService.updateAttr(attr);

    return R.ok();
  }

  /**
   * 删除
   */
  @RequestMapping("/delete")
  public R delete(@RequestBody Long[] attrIds) {
    attrService.removeByIds(Arrays.asList(attrIds));

    return R.ok();
  }

  /**
   * 删除关联属性分组
   */
  @RequestMapping("/relation/delete")
  public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
    attrService.deleteRelation(vos);

    return R.ok();
  }

}
