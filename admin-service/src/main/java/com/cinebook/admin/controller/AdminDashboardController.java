package com.cinebook.admin.controller;

import com.cinebook.admin.dto.ApiResponse;
import com.cinebook.admin.dto.DashboardDto;
import com.cinebook.admin.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardDto> dashboard(HttpServletRequest request) {
        return ApiResponse.ok("OK", dashboardService.build(request.getHeader("Authorization")));
    }
}
