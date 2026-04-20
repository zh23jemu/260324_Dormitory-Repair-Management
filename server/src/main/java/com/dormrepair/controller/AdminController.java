package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.admin.DictSaveRequest;
import com.dormrepair.dto.admin.ResourceRequest;
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

    @GetMapping("/statistics/status")
    public ApiResponse<List<Map<String, Object>>> statusStats() {
        return ApiResponse.success(adminService.statusStats());
    }

    @GetMapping("/statistics/repairer-workload")
    public ApiResponse<List<Map<String, Object>>> repairerWorkloadStats() {
        return ApiResponse.success(adminService.repairerWorkloadStats());
    }

    @GetMapping("/logs")
    public ApiResponse<List<Map<String, Object>>> logs() {
        return ApiResponse.success(adminService.logs());
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

    @GetMapping("/resources")
    public ApiResponse<List<Map<String, Object>>> resources() {
        return ApiResponse.success(adminService.resources());
    }

    @PostMapping("/resources")
    public ApiResponse<Void> createResource(@Valid @RequestBody ResourceRequest request) {
        adminService.createResource(request);
        return ApiResponse.success();
    }

    @PutMapping("/resources/{id}")
    public ApiResponse<Void> updateResource(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        adminService.updateResource(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/resources/{id}")
    public ApiResponse<Void> deleteResource(@PathVariable Long id) {
        adminService.deleteResource(id);
        return ApiResponse.success();
    }

    @GetMapping("/service-messages")
    public ApiResponse<List<Map<String, Object>>> serviceMessages() {
        return ApiResponse.success(adminService.serviceMessages());
    }

    @PutMapping("/service-messages/{id}/reply")
    public ApiResponse<Void> replyServiceMessage(@PathVariable Long id, @Valid @RequestBody ServiceMessageReplyRequest request) {
        adminService.replyServiceMessage(id, request);
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
}
