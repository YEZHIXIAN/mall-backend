package com.zhixian.mall.third.controller;

import com.zhixian.mall.third.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public String sendSms(@RequestParam(required = false) String toPhoneNumber, 
                          @RequestParam String message) {

        smsService.sendSms(toPhoneNumber, message);
        return "SMS sent successfully!";
    }
}
