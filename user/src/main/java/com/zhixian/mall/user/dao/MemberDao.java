package com.zhixian.mall.user.dao;

import com.zhixian.mall.user.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zhixian
 * @email raininininin@gmail.com
 * @date 2024-09-23 10:43:55
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
