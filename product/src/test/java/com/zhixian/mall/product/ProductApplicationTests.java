package com.zhixian.mall.product;

import com.zhixian.mall.product.entity.BrandEntity;
import com.zhixian.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(15L);
        brandEntity.setName("apple");
        brandService.updateById(brandEntity);
        System.out.println("success...");
    }

}
