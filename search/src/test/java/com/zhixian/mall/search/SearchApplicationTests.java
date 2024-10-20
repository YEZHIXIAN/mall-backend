package com.zhixian.mall.search;

import lombok.Data;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.IndexBoost;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    @Test
    public void indexData() throws IOException {
        User user = new User();
        user.setId("1");
        user.setUserName("张三");
        user.setAge(20);
        user.setGender("男");

        esTemplate.save(user);
    }

    @Test
    public void searchData() {

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("address", "mill"))
                .withIndicesBoost(new IndexBoost("bank",1.0f))
                .withAggregations(AggregationBuilders.terms("ageAgg").field("age").size(10)
                        .subAggregation(AggregationBuilders.avg("balanceAvg").field("balance")))
                .build();
        SearchHits<Bank> search = esTemplate.search(searchQuery, Bank.class);
        List<Bank> collect = search.stream().map(SearchHit::getContent).collect(Collectors.toList());
        System.out.println(search);
    }

    @Test
    void contextLoads() {
        System.out.println("hello");
    }

    @Nested
    @Data
    @Document(indexName = "user")
    class User {
        @Id
        private String id;
        private String userName;
        private Integer age;
        private String gender;
    }

    @Data
    @Nested
    @Document(indexName = "bank")
    public class Bank {

        @Id
        private String id;

        private Integer accountNumber;
        private Integer balance;
        private String firstName;
        private String lastName;
        private Integer age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
}
