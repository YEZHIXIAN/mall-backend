package com.zhixian.mall.cart.vo;

import lombok.Data;

@Data
public class UserInfoTo {

    private String userId;

    private String userKey;

    private boolean tempUser = false;
}
