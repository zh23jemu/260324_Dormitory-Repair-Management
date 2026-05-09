package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.admin.ClassRequest;
import com.dormrepair.dto.admin.CollegeRequest;
import com.dormrepair.dto.admin.DictSaveRequest;
import com.dormrepair.dto.admin.MajorRequest;
import com.dormrepair.dto.admin.MaterialRequest;
import com.dormrepair.dto.admin.RepairTypeSaveRequest;
import com.dormrepair.dto.admin.ServiceMessageReplyRequest;
import com.dormrepair.dto.admin.StatusUpdateRequest;
import com.dormrepair.dto.admin.UserCreateRequest;
import com.dormrepair.dto.admin.UserUpdateRequest;
import com.dormrepair.dto.auth.ResetPasswordRequest;
import com.dormrepair.service.AdminService;
import com.dormrepair.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> users(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.users(pageNum, pageSize));
    }

    @PostMapping("/users")
    public ApiResponse<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        adminService.createUser(request);
        return ApiResponse.success();
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        adminService.updateUser(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.success();
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        adminService.updateUserStatus(id, request);
        return ApiResponse.success();
    }

    @PutMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/statistics/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(adminService.overview());
    }

    @GetMapping("/statistics/repair-type")
    public ApiResponse<List<Map<String, Object>>> repairTypeStats() {
        return ApiResponse.success(adminService.repairTypeStats());
    }

    @GetMapping("/statistics/building-heat")
    public ApiResponse<List<Map<String, Object>>> buildingHeat() {
        return ApiResponse.success(adminService.buildingHeatStats());
    }

    @GetMapping("/statistics/rating")
    public ApiResponse<List<Map<String, Object>>> ratingStats() {
        return ApiResponse.success(adminService.ratingStats());
    }

    @GetMapping("/statistics/status")
    public ApiResponse<List<Map<String, Object>>> statusStats() {
        return ApiResponse.success(adminService.statusStats());
    }

    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> logs(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.logs(pageNum, pageSize));
    }

    @GetMapping("/dicts")
    public ApiResponse<List<Map<String, Object>>> dicts() {
        return ApiResponse.success(adminService.dicts());
    }

    @PostMapping("/dicts")
    public ApiResponse<Void> createDict(@Valid @RequestBody DictSaveRequest request) {
        adminService.createDict(request);
        return ApiResponse.success();
    }

    @PutMapping("/dicts/{id}")
    public ApiResponse<Void> updateDict(@PathVariable Long id, @Valid @RequestBody DictSaveRequest request) {
        adminService.updateDict(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/dicts/{id}")
    public ApiResponse<Void> deleteDict(@PathVariable Long id) {
        adminService.deleteDict(id);
        return ApiResponse.success();
    }

    @GetMapping("/service-messages")
    public ApiResponse<Map<String, Object>> serviceMessages(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.serviceMessages(pageNum, pageSize));
    }

    @PutMapping("/service-messages/{id}/reply")
    public ApiResponse<Void> replyServiceMessage(@PathVariable Long id, @Valid @RequestBody ServiceMessageReplyRequest request) {
        adminService.replyServiceMessage(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/forum-posts")
    public ApiResponse<Map<String, Object>> forumPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.forumPosts(status, pageNum, pageSize));
    }

    @PutMapping("/forum-posts/{id}/status")
    public ApiResponse<Void> updateForumPostStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        adminService.updateForumPostStatus(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/forum-posts/{id}")
    public ApiResponse<Void> deleteForumPost(@PathVariable Long id) {
        adminService.deleteForumPost(id);
        return ApiResponse.success();
    }

    @GetMapping("/ratings")
    public ApiResponse<Map<String, Object>> ratings(
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.ratings(score, pageNum, pageSize));
    }

    @DeleteMapping("/ratings/{id}")
    public ApiResponse<Void> deleteRating(@PathVariable Long id) {
        adminService.deleteRating(id);
        return ApiResponse.success();
    }

    @GetMapping("/repair-types")
    public ApiResponse<List<Map<String, Object>>> repairTypes() {
        return ApiResponse.success(adminService.repairTypes());
    }

    @PostMapping("/repair-types")
    public ApiResponse<Void> createRepairType(@Valid @RequestBody RepairTypeSaveRequest request) {
        adminService.createRepairType(request);
        return ApiResponse.success();
    }

    @PutMapping("/repair-types/{id}")
    public ApiResponse<Void> updateRepairType(@PathVariable Long id, @Valid @RequestBody RepairTypeSaveRequest request) {
        adminService.updateRepairType(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/repair-types/{id}")
    public ApiResponse<Void> deleteRepairType(@PathVariable Long id) {
        adminService.deleteRepairType(id);
        return ApiResponse.success();
    }

    @GetMapping("/school-options")
    public ApiResponse<Map<String, Object>> schoolOptions() {
        return ApiResponse.success(adminService.schoolOptions());
    }

    @PostMapping("/school-colleges")
    public ApiResponse<Void> createCollege(@Valid @RequestBody CollegeRequest request) {
        adminService.createCollege(request);
        return ApiResponse.success();
    }

    @PutMapping("/school-colleges/{id}")
    public ApiResponse<Void> updateCollege(@PathVariable Long id, @Valid @RequestBody CollegeRequest request) {
        adminService.updateCollege(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/school-colleges/{id}")
    public ApiResponse<Void> deleteCollege(@PathVariable Long id) {
        adminService.deleteCollege(id);
        return ApiResponse.success();
    }

    @PostMapping("/school-majors")
    public ApiResponse<Void> createMajor(@Valid @RequestBody MajorRequest request) {
        adminService.createMajor(request);
        return ApiResponse.success();
    }

    @PutMapping("/school-majors/{id}")
    public ApiResponse<Void> updateMajor(@PathVariable Long id, @Valid @RequestBody MajorRequest request) {
        adminService.updateMajor(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/school-majors/{id}")
    public ApiResponse<Void> deleteMajor(@PathVariable Long id) {
        adminService.deleteMajor(id);
        return ApiResponse.success();
    }

    @PostMapping("/school-classes")
    public ApiResponse<Void> createClass(@Valid @RequestBody ClassRequest request) {
        adminService.createClass(request);
        return ApiResponse.success();
    }

    @PutMapping("/school-classes/{id}")
    public ApiResponse<Void> updateClass(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        adminService.updateClass(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/school-classes/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        adminService.deleteClass(id);
        return ApiResponse.success();
    }

    @GetMapping("/materials")
    public ApiResponse<Map<String, Object>> materials(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(adminService.materials(pageNum, pageSize));
    }

    @PostMapping("/materials")
    public ApiResponse<Void> createMaterial(@Valid @RequestBody MaterialRequest request) {
        adminService.createMaterial(request);
        return ApiResponse.success();
    }

    @PutMapping("/materials/{id}")
    public ApiResponse<Void> updateMaterial(@PathVariable Long id, @Valid @RequestBody MaterialRequest request) {
        adminService.updateMaterial(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/materials/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
        adminService.deleteMaterial(id);
        return ApiResponse.success();
    }
}
