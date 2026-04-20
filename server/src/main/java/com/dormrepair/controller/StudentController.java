package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.repair.RepairCreateRequest;
import com.dormrepair.dto.repair.RepairRatingRequest;
import com.dormrepair.dto.student.ForumPostRequest;
import com.dormrepair.dto.student.ServiceMessageRequest;
import com.dormrepair.dto.student.StudentProfileUpdateRequest;
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

    @GetMapping("/home-summary")
    public ApiResponse<Map<String, Object>> homeSummary() {
        return ApiResponse.success(studentService.homeSummary());
    }

    @GetMapping("/facilities")
    public ApiResponse<List<Map<String, Object>>> facilities(@RequestParam(required = false) Long roomId) {
        return ApiResponse.success(studentService.facilities(roomId));
    }

    @GetMapping("/resources")
    public ApiResponse<List<Map<String, Object>>> resources() {
        return ApiResponse.success(studentService.repairResources());
    }

    @GetMapping("/resources/{id}")
    public ApiResponse<Map<String, Object>> resourceDetail(@PathVariable Long id) {
        return ApiResponse.success(studentService.repairResourceDetail(id));
    }

    @GetMapping("/repairers")
    public ApiResponse<List<Map<String, Object>>> repairers() {
        return ApiResponse.success(studentService.repairers());
    }

    @GetMapping("/repairers/{id}")
    public ApiResponse<Map<String, Object>> repairerDetail(@PathVariable Long id) {
        return ApiResponse.success(studentService.repairerDetail(id));
    }

    @GetMapping("/forum-posts")
    public ApiResponse<List<Map<String, Object>>> forumPosts() {
        return ApiResponse.success(studentService.forumPosts());
    }

    @PostMapping("/forum-posts")
    public ApiResponse<Void> createForumPost(@Valid @RequestBody ForumPostRequest request) {
        studentService.createForumPost(request);
        return ApiResponse.success();
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
    public ApiResponse<Void> updateProfile(@RequestBody StudentProfileUpdateRequest request) {
        studentService.updateProfile(request);
        return ApiResponse.success();
    }

    @GetMapping("/service-messages")
    public ApiResponse<List<Map<String, Object>>> serviceMessages() {
        return ApiResponse.success(studentService.serviceMessages());
    }

    @PostMapping("/service-messages")
    public ApiResponse<Void> createServiceMessage(@Valid @RequestBody ServiceMessageRequest request) {
        studentService.createServiceMessage(request);
        return ApiResponse.success();
    }
}
