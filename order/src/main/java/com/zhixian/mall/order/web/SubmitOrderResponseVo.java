package com.zhixian.mall.order.web;

import com.zhixian.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    // 错误状态码，0:成功 1:失败
    private Integer code;
}
