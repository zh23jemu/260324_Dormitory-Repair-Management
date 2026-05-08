package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.student.ForumCommentRequest;
import com.dormrepair.service.PortalService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        return ApiResponse.success(portalService.home());
    }

    @GetMapping("/announcements")
    public ApiResponse<Map<String, Object>> announcements(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(portalService.announcements(pageNum, pageSize));
    }

    @GetMapping("/forum-posts")
    public ApiResponse<Map<String, Object>> forumPosts(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(portalService.forumPosts(pageNum, pageSize));
    }

    @GetMapping("/repairers")
    public ApiResponse<Map<String, Object>> repairers(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(portalService.repairers(pageNum, pageSize));
    }

    @GetMapping("/repairers/{id}")
    public ApiResponse<Map<String, Object>> repairerDetail(@PathVariable Long id) {
        return ApiResponse.success(portalService.repairerDetail(id));
    }

    @PostMapping("/forum-posts/{postId}/comments")
    public ApiResponse<Void> createForumComment(@PathVariable Long postId, @Valid @RequestBody ForumCommentRequest request) {
        portalService.createForumComment(postId, request);
        return ApiResponse.success();
    }
}
