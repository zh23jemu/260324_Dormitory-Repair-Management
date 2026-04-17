package com.platform.warranty.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostDTO extends PageQueryDTO{
    private String keyword;
    private Integer status; // 0: 待审核, 1: 审核通过, 2: 下架
}