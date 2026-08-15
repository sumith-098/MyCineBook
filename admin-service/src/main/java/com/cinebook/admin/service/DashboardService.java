package com.cinebook.admin.service;

import com.cinebook.admin.dto.DashboardDto;
import com.cinebook.admin.entity.SettlementStatus;
import com.cinebook.admin.repository.SettlementRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final SiblingServiceClient siblingServiceClient;
    private final SettlementRepository settlementRepository;

    public DashboardService(SiblingServiceClient siblingServiceClient, SettlementRepository settlementRepository) {
        this.siblingServiceClient = siblingServiceClient;
        this.settlementRepository = settlementRepository;
    }

    public DashboardDto build(String bearerToken) {
        var authStats = siblingServiceClient.authStats(bearerToken);
        var catalogStats = siblingServiceClient.catalogStats(bearerToken);
        var bookingStats = siblingServiceClient.bookingStats(bearerToken);
        var pendingOwners = siblingServiceClient.pendingOwners(bearerToken);
        var recentBookings = siblingServiceClient.recentBookings(bearerToken);

        long pendingSettlementOwners = settlementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .filter(s -> s.getStatus() == SettlementStatus.PENDING)
                .map(s -> s.getOwnerId())
                .collect(Collectors.toSet()).size();

        DashboardDto dto = new DashboardDto();
        dto.setCustomerCount(authStats.getCustomerCount());
        dto.setPendingOwnerCount(authStats.getPendingOwnerCount());
        dto.setTheaterCount(catalogStats.getTheaterCount());
        dto.setMovieCount(catalogStats.getMovieCount());
        dto.setBookingCount(bookingStats.getTotalBookings());
        dto.setRevenue(bookingStats.getTotalRevenue());
        dto.setPendingSettlementOwnerCount(pendingSettlementOwners);
        dto.setPendingOwners(pendingOwners);
        dto.setRecentBookings(recentBookings);
        return dto;
    }
}
