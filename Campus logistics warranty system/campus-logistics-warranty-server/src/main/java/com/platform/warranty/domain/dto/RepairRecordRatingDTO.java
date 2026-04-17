package com.platform.warranty.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RepairRecordRatingDTO extends PageQueryDTO{
    private String keyword;
    private Long repairRecordId;
}
