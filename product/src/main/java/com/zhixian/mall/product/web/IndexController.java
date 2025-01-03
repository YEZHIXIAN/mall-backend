package com.zhixian.mall.product.web;

import com.zhixian.mall.product.entity.CategoryEntity;
import com.zhixian.mall.product.service.CategoryService;
import com.zhixian.mall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() throws InterruptedException {
        return categoryService.getCatalogJson();
    }

    @GetMapping("/index/testsession")
    public String testSession(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        System.out.println("当前登录用户是：" + loginUser);
        return "index";
    }
}
