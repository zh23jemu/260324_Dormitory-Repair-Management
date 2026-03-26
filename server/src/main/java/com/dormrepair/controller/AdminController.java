package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.admin.StatusUpdateRequest;
import com.dormrepair.dto.admin.UserCreateRequest;
import com.dormrepair.dto.admin.UserUpdateRequest;
import com.dormrepair.dto.auth.ResetPasswordRequest;
import com.dormrepair.service.AdminService;
import com.dormrepair.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.success(adminService.users());
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

    @GetMapping("/logs")
    public ApiResponse<List<Map<String, Object>>> logs() {
        return ApiResponse.success(adminService.logs());
    }

    @GetMapping("/dicts")
    public ApiResponse<List<Map<String, Object>>> dicts() {
        return ApiResponse.success(adminService.dicts());
    }
}
