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
    public ApiResponse<List<Map<String, Object>>> announcements() {
        return ApiResponse.success(portalService.announcements());
    }

    @GetMapping("/forum-posts")
    public ApiResponse<List<Map<String, Object>>> forumPosts() {
        return ApiResponse.success(portalService.forumPosts());
    }

    @GetMapping("/repairers")
    public ApiResponse<List<Map<String, Object>>> repairers() {
        return ApiResponse.success(portalService.repairers());
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
