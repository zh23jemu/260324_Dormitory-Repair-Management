package com.platform.warranty.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data // Lombok 注解，自动生成 getter 和 setter
public class MessageQueryDTO extends PageQueryDTO{
    private String keyword;
    private Integer status;

}
