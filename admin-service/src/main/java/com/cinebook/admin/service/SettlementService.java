package com.cinebook.admin.service;

import com.cinebook.admin.dto.*;
import com.cinebook.admin.entity.Settlement;
import com.cinebook.admin.entity.SettlementStatus;
import com.cinebook.admin.exception.ApiException;
import com.cinebook.admin.repository.SettlementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SiblingServiceClient siblingServiceClient;
    private final SettlementRepository settlementRepository;
    private final CommissionConfigService commissionConfigService;

    public SettlementService(SiblingServiceClient siblingServiceClient, SettlementRepository settlementRepository,
                              CommissionConfigService commissionConfigService) {
        this.siblingServiceClient = siblingServiceClient;
        this.settlementRepository = settlementRepository;
        this.commissionConfigService = commissionConfigService;
    }

    public SettlementsResponseDto summary(String bearerToken) {
        BigDecimal commissionPct = commissionConfigService.getCommissionPct();

        List<AdminOwnerSummaryDto> owners = siblingServiceClient.activeOwners(bearerToken);
        List<AdminTheaterLiteDto> allTheaters = siblingServiceClient.allTheaters(bearerToken);

        // owner_id -> list of their theater_ids (theater ownership lives in catalog-service)
        Map<Long, List<Long>> theatersByOwner = allTheaters.stream()
                .filter(t -> t.getOwnerId() != null)
                .collect(Collectors.groupingBy(AdminTheaterLiteDto::getOwnerId,
                        Collectors.mapping(AdminTheaterLiteDto::getTheaterId, Collectors.toList())));

        // Ask booking-service for razorpay-paid earnings across ALL theaters in one call,
        // then bucket the results back per owner — avoids one round trip per owner.
        List<Long> allTheaterIds = allTheaters.stream().map(AdminTheaterLiteDto::getTheaterId).toList();
        Map<Long, BigDecimal> earningsByTheater = siblingServiceClient.earningsByTheater(bearerToken, allTheaterIds).stream()
                .collect(Collectors.toMap(TheaterEarningLiteDto::getTheaterId, TheaterEarningLiteDto::getTotalEarned));

        List<OwnerSettlementSummaryDto> summaries = owners.stream().map(owner -> {
            List<Long> theaterIds = theatersByOwner.getOrDefault(owner.getOwnerId(), List.of());
            BigDecimal totalEarned = theaterIds.stream()
                    .map(id -> earningsByTheater.getOrDefault(id, BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal adminCut = totalEarned.multiply(commissionPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal ownerShare = totalEarned.subtract(adminCut);
            BigDecimal alreadyPaid = settlementRepository.sumByOwnerIdAndStatus(owner.getOwnerId(), SettlementStatus.PAID);
            BigDecimal pending = ownerShare.subtract(alreadyPaid).max(BigDecimal.ZERO);

            OwnerSettlementSummaryDto dto = new OwnerSettlementSummaryDto();
            dto.setOwnerId(owner.getOwnerId());
            dto.setOwnerName(owner.getName());
            dto.setOwnerEmail(owner.getEmail());
            dto.setTotalEarned(totalEarned);
            dto.setAdminCut(adminCut);
            dto.setOwnerShare(ownerShare);
            dto.setAlreadyPaid(alreadyPaid);
            dto.setPendingAmount(pending);
            return dto;
        }).toList();

        SettlementsResponseDto response = new SettlementsResponseDto();
        response.setCommissionPct(commissionPct);
        response.setOwners(summaries);
        response.setRecentSettlements(settlementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toDto).toList());
        return response;
    }

    @Transactional
    public SettlementDto markPaid(Long ownerId, MarkSettlementPaidRequest req) {
        if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Amount must be greater than 0.", HttpStatus.BAD_REQUEST);
        }
        LocalDate today = LocalDate.now();
        Settlement settlement = new Settlement();
        settlement.setOwnerId(ownerId);
        settlement.setAmount(req.getAmount());
        settlement.setPeriodFrom(today);
        settlement.setPeriodTo(today);
        settlement.setStatus(SettlementStatus.PAID);
        settlement.setNotes(req.getNotes());
        settlement.setPaidAt(LocalDateTime.now());
        settlement = settlementRepository.save(settlement);
        return toDto(settlement);
    }

    private SettlementDto toDto(Settlement s) {
        SettlementDto dto = new SettlementDto();
        dto.setSettlementId(s.getSettlementId());
        dto.setOwnerId(s.getOwnerId());
        dto.setAmount(s.getAmount());
        dto.setStatus(s.getStatus().name());
        dto.setNotes(s.getNotes());
        dto.setPaidAt(s.getPaidAt() == null ? null : s.getPaidAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}
