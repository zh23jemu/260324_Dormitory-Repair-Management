package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.admin.StudentRoomRequest;
import com.dormrepair.dto.repair.RepairCreateRequest;
import com.dormrepair.dto.repair.RepairRatingRequest;
import com.dormrepair.service.DormAdminService;
import com.dormrepair.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final DormAdminService dormAdminService;

    public StudentController(StudentService studentService, DormAdminService dormAdminService) {
        this.studentService = studentService;
        this.dormAdminService = dormAdminService;
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Map<String, Object>>> announcements() {
        return ApiResponse.success(dormAdminService.announcements());
    }

    @GetMapping("/repair-types")
    public ApiResponse<List<Map<String, Object>>> repairTypes() {
        return ApiResponse.success(studentService.repairTypes());
    }

    @GetMapping("/rating-indicators")
    public ApiResponse<List<Map<String, Object>>> ratingIndicators() {
        return ApiResponse.success(studentService.ratingIndicators());
    }

    @PostMapping("/repair-orders")
    public ApiResponse<Void> createRepairOrder(@Valid @RequestBody RepairCreateRequest request) {
        studentService.createRepairOrder(request);
        return ApiResponse.success();
    }

    @GetMapping("/repair-orders")
    public ApiResponse<List<Map<String, Object>>> myOrders(@RequestParam(required = false) String status) {
        return ApiResponse.success(studentService.myRepairOrders(status));
    }

    @GetMapping("/repair-orders/{id}")
    public ApiResponse<Map<String, Object>> orderDetail(@PathVariable Long id) {
        return ApiResponse.success(studentService.myRepairOrderDetail(id));
    }

    @PostMapping("/repair-orders/{id}/rating")
    public ApiResponse<Void> rate(@PathVariable Long id, @Valid @RequestBody RepairRatingRequest request) {
        studentService.rateRepairOrder(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.success(studentService.myProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody StudentRoomRequest request) {
        studentService.updateProfile(request);
        return ApiResponse.success();
    }
}
