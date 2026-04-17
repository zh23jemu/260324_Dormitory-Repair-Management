package com.platform.warranty.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryDTO extends PageQueryDTO{
    /**
     * 查询条件字段
     */
    private Integer postId;        // 根据文章ID查询评论
    private Integer userId;        // 根据用户ID查询评论
    private Boolean isDeleted;     // 是否已删除（软删除）
    private String keyword;        // 模糊搜索：评论内容中的关键字

}