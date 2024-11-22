package com.zhixian.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhixian.mall.common.utils.PageUtils;
import com.zhixian.mall.common.utils.Query;
import com.zhixian.mall.user.dao.MemberDao;
import com.zhixian.mall.user.entity.MemberEntity;
import com.zhixian.mall.user.entity.MemberLevelEntity;
import com.zhixian.mall.user.exception.PhoneExistException;
import com.zhixian.mall.user.exception.UsernameExistException;
import com.zhixian.mall.user.service.MemberLevelService;
import com.zhixian.mall.user.service.MemberService;
import com.zhixian.mall.user.vo.UserLoginVo;
import com.zhixian.mall.user.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterVo userRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();

        // 检查用户名和手机号是否唯一
        checkPhoneUnique(userRegisterVo.getPhone());
        checkUsernameUnique(userRegisterVo.getUserName());

        // 设置用户信息
        memberEntity.setUsername(userRegisterVo.getUserName());
        memberEntity.setMobile(userRegisterVo.getPhone());

        // 设置密码
        String encode = new BCryptPasswordEncoder().encode(userRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        this.save(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {

        String loginAccount = userLoginVo.getLoginAccount();
        String password = userLoginVo.getPassword();
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAccount).or().eq("mobile", loginAccount));

        if (memberEntity != null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (bCryptPasswordEncoder.matches(password, memberEntity.getPassword())) {
                return memberEntity;
            }
        }
        return null;
    }

}