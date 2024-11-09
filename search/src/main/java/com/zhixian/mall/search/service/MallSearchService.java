package com.zhixian.mall.search.service;

import com.zhixian.mall.search.vo.SearchParam;
import com.zhixian.mall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
