package com.zhixian.mall.product.vo;

import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo {

  /**
   * 分类名称
   */
  private String catelogName;

  /**
   * 分组名称
   */
  private String groupName;

  /**
   * 分类路径
   */
  private Long[] catelogPath;

}
