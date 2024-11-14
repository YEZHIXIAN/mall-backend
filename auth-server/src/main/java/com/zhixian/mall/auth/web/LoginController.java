package com.zhixian.mall.auth.web;

import com.zhixian.mall.auth.feign.ThirdPartyFeignService;
import com.zhixian.mall.auth.vo.UserRegistVo;
import com.zhixian.mall.common.constant.AuthServerConstant;
import com.zhixian.mall.common.exception.BizCodeEnum;
import com.zhixian.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (redisCode != null) {
            long saveTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - saveTime < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = UUID.randomUUID().toString().substring(0, 5);
        code += "_" + System.currentTimeMillis();
        redisTemplate
                .opsForValue()
                .set(
                        AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
                        code,
                        10,
                        TimeUnit.MINUTES
                );

        thirdPartFeignService.sendSms(phone, code);
        System.out.println("code = " + code);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegistVo vo, BindingResult result, Model model) {
        if (result.hasErrors()) {
            Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(
                    FieldError::getField,
                    DefaultMessageSourceResolvable::getDefaultMessage,
                    (existingValue, newValue) -> existingValue
            ));
            model.addAttribute("errors", result.getFieldErrors());
            return "reg";
        }
        return "redirect:/login.html";
    }
}
