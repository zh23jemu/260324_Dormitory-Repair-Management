package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.admin.AnnouncementRequest;
import com.dormrepair.dto.admin.BuildingRequest;
import com.dormrepair.dto.admin.FacilityRequest;
import com.dormrepair.dto.admin.RoomRequest;
import com.dormrepair.dto.admin.StudentClassRequest;
import com.dormrepair.dto.admin.StudentRoomRequest;
import com.dormrepair.dto.repair.RepairOrderQueryRequest;
import com.dormrepair.dto.repair.RepairAssignRequest;
import com.dormrepair.dto.repair.RepairRejectRequest;
import com.dormrepair.service.DormAdminService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dorm-admin")
public class DormAdminController {

    private final DormAdminService dormAdminService;

    public DormAdminController(DormAdminService dormAdminService) {
        this.dormAdminService = dormAdminService;
    }

    @GetMapping("/repair-orders/pending-review")
    public ApiResponse<Map<String, Object>> pendingReview(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.pendingReviewOrders(pageNum, pageSize));
    }

    @GetMapping("/repair-orders")
    public ApiResponse<Map<String, Object>> repairOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.repairOrders(status, pageNum, pageSize));
    }

    @PostMapping("/repair-orders/query")
    public ApiResponse<Map<String, Object>> repairOrders(@RequestBody RepairOrderQueryRequest request) {
        return ApiResponse.success(dormAdminService.repairOrders(request));
    }

    @GetMapping("/repair-orders/{id}")
    public ApiResponse<Map<String, Object>> repairOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(dormAdminService.repairOrderDetail(id));
    }

    @PostMapping("/repair-orders/{id}/assign")
    public ApiResponse<Void> assign(@PathVariable Long id, @Valid @RequestBody RepairAssignRequest request) {
        dormAdminService.assign(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/repair-orders/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @Valid @RequestBody RepairRejectRequest request) {
        dormAdminService.reject(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/repairers")
    public ApiResponse<List<Map<String, Object>>> repairers() {
        return ApiResponse.success(dormAdminService.repairers());
    }

    @GetMapping("/repair-types")
    public ApiResponse<List<Map<String, Object>>> repairTypes() {
        return ApiResponse.success(dormAdminService.repairTypes());
    }

    @GetMapping("/repairers/recommended")
    public ApiResponse<List<Map<String, Object>>> recommendedRepairers(@RequestParam(required = false) Long repairTypeId) {
        return ApiResponse.success(dormAdminService.recommendedRepairers(repairTypeId));
    }

    @GetMapping("/buildings")
    public ApiResponse<List<Map<String, Object>>> buildings() {
        return ApiResponse.success(dormAdminService.buildings());
    }

    @PostMapping("/buildings")
    public ApiResponse<Void> createBuilding(@Valid @RequestBody BuildingRequest request) {
        dormAdminService.addBuilding(request);
        return ApiResponse.success();
    }

    @PutMapping("/buildings/{id}")
    public ApiResponse<Void> updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
        dormAdminService.updateBuilding(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/buildings/{id}")
    public ApiResponse<Void> deleteBuilding(@PathVariable Long id) {
        dormAdminService.deleteBuilding(id);
        return ApiResponse.success();
    }

    @GetMapping("/rooms")
    public ApiResponse<List<Map<String, Object>>> rooms() {
        return ApiResponse.success(dormAdminService.rooms());
    }

    @PostMapping("/rooms")
    public ApiResponse<Void> createRoom(@Valid @RequestBody RoomRequest request) {
        dormAdminService.addRoom(request);
        return ApiResponse.success();
    }

    @PutMapping("/rooms/{id}")
    public ApiResponse<Void> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        dormAdminService.updateRoom(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/rooms/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        dormAdminService.deleteRoom(id);
        return ApiResponse.success();
    }

    @GetMapping("/facilities")
    public ApiResponse<Map<String, Object>> facilities(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.facilities(roomId, keyword, status, pageNum, pageSize));
    }

    @PostMapping("/facilities")
    public ApiResponse<Void> createFacility(@Valid @RequestBody FacilityRequest request) {
        dormAdminService.createFacility(request);
        return ApiResponse.success();
    }

    @PutMapping("/facilities/{id}")
    public ApiResponse<Void> updateFacility(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        dormAdminService.updateFacility(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/facilities/{id}")
    public ApiResponse<Void> deleteFacility(@PathVariable Long id) {
        dormAdminService.deleteFacility(id);
        return ApiResponse.success();
    }

    @GetMapping("/students")
    public ApiResponse<Map<String, Object>> students(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.students(pageNum, pageSize));
    }

    @PutMapping("/students/{id}/room")
    public ApiResponse<Void> updateStudentRoom(@PathVariable Long id, @RequestBody StudentRoomRequest request) {
        dormAdminService.updateStudentRoom(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/school-options")
    public ApiResponse<Map<String, Object>> schoolOptions() {
        return ApiResponse.success(dormAdminService.schoolOptions());
    }

    @PutMapping("/students/{id}/class")
    public ApiResponse<Void> updateStudentClass(@PathVariable Long id, @Valid @RequestBody StudentClassRequest request) {
        dormAdminService.updateStudentClass(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/announcements")
    public ApiResponse<Map<String, Object>> announcements(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.success(dormAdminService.announcements(pageNum, pageSize));
    }

    @PostMapping("/announcements")
    public ApiResponse<Void> createAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        dormAdminService.createAnnouncement(request);
        return ApiResponse.success();
    }

    @PutMapping("/announcements/{id}")
    public ApiResponse<Void> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        dormAdminService.updateAnnouncement(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        dormAdminService.deleteAnnouncement(id);
        return ApiResponse.success();
    }
}
