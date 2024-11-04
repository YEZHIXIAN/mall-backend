package com.zhixian.mall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrResponseVo extends AttrVo {

  /**
   * 分类名称
   */
  private String catalogName;

  /**
   * 分组名称
   */
  private String groupName;

  /**
   * 分类路径
   */
  private Long[] catalogPath;

}
