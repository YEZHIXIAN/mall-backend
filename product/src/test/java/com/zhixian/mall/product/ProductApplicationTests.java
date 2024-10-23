package com.zhixian.mall.product;

import com.zhixian.mall.product.entity.BrandEntity;
import com.zhixian.mall.product.service.BrandService;
import com.zhixian.mall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class ProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(15L);
        brandEntity.setName("apple");
        brandService.updateById(brandEntity);
        System.out.println("success...");
    }

    @Test
    void testFindPath(){
        Long[] catalogPath = categoryService.findCatalogPath(225L);
        log.info("完整路径, {}", Arrays.asList(catalogPath));
    }

}
