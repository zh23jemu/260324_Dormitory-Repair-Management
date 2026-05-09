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
    public ApiResponse<Map<String, Object>> announcements(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.announcements(pageNum, pageSize));
    }

    @GetMapping("/repair-types")
    public ApiResponse<List<Map<String, Object>>> repairTypes() {
        return ApiResponse.success(studentService.repairTypes());
    }

    @GetMapping("/buildings")
    public ApiResponse<List<Map<String, Object>>> buildings() {
        return ApiResponse.success(studentService.buildings());
    }

    @GetMapping("/rooms")
    public ApiResponse<List<Map<String, Object>>> rooms(@RequestParam(required = false) Long buildingId) {
        return ApiResponse.success(studentService.rooms(buildingId));
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

    @GetMapping("/repairers")
    public ApiResponse<Map<String, Object>> repairers(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(studentService.repairers(pageNum, pageSize));
    }

    @GetMapping("/repairers/{id}")
    public ApiResponse<Map<String, Object>> repairerDetail(@PathVariable Long id) {
        return ApiResponse.success(studentService.repairerDetail(id));
    }

    @GetMapping("/forum-posts")
    public ApiResponse<Map<String, Object>> forumPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(studentService.forumPosts(keyword, pageNum, pageSize));
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
    public ApiResponse<Map<String, Object>> myOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(studentService.myRepairOrders(status, pageNum, pageSize));
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
    public ApiResponse<Map<String, Object>> serviceMessages(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(studentService.serviceMessages(pageNum, pageSize));
    }

    @PostMapping("/service-messages")
    public ApiResponse<Void> createServiceMessage(@Valid @RequestBody ServiceMessageRequest request) {
        studentService.createServiceMessage(request);
        return ApiResponse.success();
    }
}
