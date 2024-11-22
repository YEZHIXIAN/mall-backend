package com.zhixian.mall.common.exception;

import lombok.Getter;

@Getter
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，稍后再试"),
    USER_EXIST_EXCEPTION(15001, "用户已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    LOGIN_EXCEPTION(15003, "用户名或密码错误");

    private final String msg;
    private final int code;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
