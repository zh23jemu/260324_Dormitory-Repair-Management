package com.platform.warranty.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceQueryDTO extends PageQueryDTO {

    private String keyword;      // 模糊匹配标题或描述中的关键词

}