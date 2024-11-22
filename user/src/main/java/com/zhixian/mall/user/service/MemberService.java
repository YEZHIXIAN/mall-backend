package com.zhixian.mall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.user.entity.MemberEntity;
import com.zhixian.mall.user.exception.PhoneExistException;
import com.zhixian.mall.user.exception.UsernameExistException;
import com.zhixian.mall.user.vo.UserLoginVo;
import com.zhixian.mall.user.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:43:55
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(UserLoginVo userLoginVo);
}

