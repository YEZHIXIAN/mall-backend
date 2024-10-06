package com.zhixian.mall.product.exception;

import com.zhixian.mall.common.exception.BizCodeEnum;
import com.zhixian.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice(basePackages = "com.zhixian.mall.product.controller")
public class MallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("Parameter format validation failed: {}, error type: {}", e.getMessage(), e.getClass());
        Map<String, String> errorMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((item) -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("error", throwable);
        return R.error();
    }
}
