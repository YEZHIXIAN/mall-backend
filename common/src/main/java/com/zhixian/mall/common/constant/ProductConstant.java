package com.zhixian.mall.common.constant;

import lombok.Getter;

public class ProductConstant {

    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private final int code;

        private final String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }
}
