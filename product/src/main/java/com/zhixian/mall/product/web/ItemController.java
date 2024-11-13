package com.zhixian.mall.product.web;

import com.zhixian.mall.product.service.SkuInfoService;
import com.zhixian.mall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 商品详情页
     * @return 商品详情页
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId) {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        return "item";
    }
}
