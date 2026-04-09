package com.dormrepair.controller;

import com.dormrepair.common.ApiResponse;
import com.dormrepair.dto.repair.RepairFeedbackRequest;
import com.dormrepair.service.RepairerService;
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
@RequestMapping("/api/repairer")
public class RepairerController {

    private final RepairerService repairerService;

    public RepairerController(RepairerService repairerService) {
        this.repairerService = repairerService;
    }

    @GetMapping("/repair-orders")
    public ApiResponse<List<Map<String, Object>>> myOrders(@RequestParam(required = false) String status) {
        return ApiResponse.success(repairerService.myOrders(status));
    }

    @GetMapping("/repair-orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.success(repairerService.detail(id));
    }

    @PostMapping("/repair-orders/{id}/accept")
    public ApiResponse<Void> accept(@PathVariable Long id) {
        repairerService.accept(id);
        return ApiResponse.success();
    }

    @PostMapping("/repair-orders/{id}/feedback")
    public ApiResponse<Void> feedback(@PathVariable Long id, @Valid @RequestBody RepairFeedbackRequest request) {
        repairerService.feedback(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> statistics(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo
    ) {
        return ApiResponse.success(repairerService.statistics(dateFrom, dateTo));
    }
}
