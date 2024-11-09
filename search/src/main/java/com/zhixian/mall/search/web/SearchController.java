package com.zhixian.mall.search.web;

import com.zhixian.mall.search.service.MallSearchService;
import com.zhixian.mall.search.vo.SearchParam;
import com.zhixian.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping(value = {"/search.html","/"})
    public String getSearchPage(SearchParam searchParam, Model model) {
        SearchResult searchResult = mallSearchService.search(searchParam);
        searchResult.setNavs(new ArrayList<>());
        searchResult.setPageNavs(new ArrayList<>());
        model.addAttribute("result", searchResult);
        return "search";
    }
}
