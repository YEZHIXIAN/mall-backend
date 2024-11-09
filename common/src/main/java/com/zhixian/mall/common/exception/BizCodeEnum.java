package com.zhixian.mall.common.exception;

public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "Unknown system exception"),
    VALID_EXCEPTION(10001, "Parameter format validation failed.");

    private final String msg;
    private final int code;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }
    public int getCode() {
        return code;
    }

}
