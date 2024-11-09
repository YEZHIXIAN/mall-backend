package com.zhixian.mall.search.web;

import com.zhixian.mall.search.service.MallSearchService;
import com.zhixian.mall.search.vo.SearchParam;
import com.zhixian.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping(value = {"/search.html","/"})
    public String getSearchPage(SearchParam searchParam, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        SearchResult searchResult = mallSearchService.search(searchParam);
        model.addAttribute("result", searchResult);
        return "search";
    }
}
