package com.realive.service.admin;

import com.realive.domain.logs.PayoutLog;
import com.realive.domain.logs.SalesLog;
import com.realive.domain.logs.CommissionLog;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.adminsettlement.*;
import com.realive.repository.logs.PayoutLogRepository;
import com.realive.repository.logs.SalesLogRepository;
import com.realive.repository.logs.CommissionLogRepository;
import com.realive.repository.seller.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException; // 수정: jakarta.persistence 사용
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSettlementService {

    private final PayoutLogRepository payoutLogRepository;
    private final SalesLogRepository salesLogRepository;
    private final CommissionLogRepository commissionLogRepository;
    private final SellerRepository sellerRepository;

    // 정산 목록 조회 (관리자용)
    public AdminSettlementPageResponseDTO<AdminPayoutResponseDTO> getPayoutList(
            PageRequest pageRequest,
            AdminPayoutSearchConditionDTO condition) {

        // 기존 Repository의 관리자용 검색 메서드 활용
        Page<PayoutLog> payoutPage = payoutLogRepository.findAllByAdminSearchCondition(
                pageRequest,
                condition.getSellerName(),
                condition.getPeriodStart(),
                condition.getPeriodEnd()
        );

        List<AdminPayoutResponseDTO> payouts = payoutPage.getContent()
                .stream()
                .map(this::convertToAdminPayoutResponse)
                .collect(Collectors.toList());

        return AdminSettlementPageResponseDTO.<AdminPayoutResponseDTO>builder()
                .content(payouts)
                .totalElements(payoutPage.getTotalElements())
                .totalPages(payoutPage.getTotalPages())
                .currentPage(pageRequest.getPageNumber())
                .size(pageRequest.getPageSize())
                .build();
    }

    // 정산 상세 조회 (관리자용)
    public AdminPayoutDetailResponseDTO getPayoutDetail(Integer payoutId) {
        PayoutLog payout = payoutLogRepository.findById(payoutId)
                .orElseThrow(() -> new EntityNotFoundException("정산 정보를 찾을 수 없습니다."));

        // PayoutLog의 sellerId가 Integer이므로 Long으로 변환
        Seller seller = sellerRepository.findById(payout.getSellerId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("판매자 정보를 찾을 수 없습니다."));

        // 해당 기간의 판매 내역 조회 (기존 메서드 활용)
        List<SalesLog> salesLogs = salesLogRepository.findBySoldAtBetween(
                        payout.getPeriodStart(),
                        payout.getPeriodEnd()
                ).stream()
                .filter(salesLog -> salesLog.getSellerId().equals(payout.getSellerId()))
                .collect(Collectors.toList());

        // 해당 기간의 수수료 내역 조회 (기존 메서드 활용)
        List<CommissionLog> commissionLogs = commissionLogRepository
                .findBySalesLogIdIn(salesLogs.stream()
                        .map(SalesLog::getId)
                        .collect(Collectors.toList()));

        return AdminPayoutDetailResponseDTO.builder()
                .payoutId(payout.getId())
                .sellerId(seller.getId().intValue()) // Long을 Integer로 변환
                .sellerName(seller.getName())
                .sellerEmail(seller.getEmail())
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .totalSales(payout.getTotalSales())
                .totalCommission(payout.getTotalCommission())
                .payoutAmount(payout.getPayoutAmount())
                .processedAt(payout.getProcessedAt())
                .salesDetails(salesLogs.stream()
                        .map(this::convertToSalesDetail)
                        .collect(Collectors.toList()))
                .commissionDetails(commissionLogs.stream()
                        .map(this::convertToCommissionDetail)
                        .collect(Collectors.toList()))
                .build();
    }

    // 정산 통계 조회 (관리자용) - 기존 메서드들 활용
    public AdminSettlementStatisticsResponseDTO getSettlementStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // 기존 Repository 메서드들 활용
        long totalPayouts = payoutLogRepository.count();
        long recentPayouts = payoutLogRepository.countPayoutsByDateBetween(thirtyDaysAgo, today);

        Long totalPayoutAmount = payoutLogRepository.sumPayoutAmountByDateBetween(thirtyDaysAgo, today);
        Long recentPayoutAmount = payoutLogRepository.sumPayoutAmountByDateBetween(thirtyDaysAgo, today);

        return AdminSettlementStatisticsResponseDTO.builder()
                .totalPayouts(totalPayouts)
                .recentPayouts(recentPayouts)
                .totalPayoutAmount(totalPayoutAmount != null ? totalPayoutAmount.intValue() : 0)
                .recentPayoutAmount(recentPayoutAmount != null ? recentPayoutAmount.intValue() : 0)
                .build();
    }

    // 일별 매출 추이 그래프 (기존 메서드 활용)
    public List<DailyPayoutSummaryResponseDTO> getDailyPayoutSummary(
            LocalDate startDate, LocalDate endDate) {

        // 기존 Repository의 매출 추이 메서드 활용
        List<Object[]> dailySummaries = payoutLogRepository.getDailyPayoutSummary(startDate, endDate);

        return dailySummaries.stream()
                .map(row -> DailyPayoutSummaryResponseDTO.builder()
                        .date((LocalDate) row[0])
                        .totalAmount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    // 월별 정산 요약 조회 (관리자용) - 메서드 제거 (Repository에 없음)
    public List<MonthlyPayoutSummaryResponseDTO> getMonthlyPayoutSummary(
            LocalDate startDate, LocalDate endDate) {

        // PayoutLogRepository에 월별 집계 메서드가 없으므로 임시로 빈 리스트 반환
        // 실제로는 Repository에 메서드를 추가해야 함
        return List.of();
    }

    // 월별 정산 상세 조회 (관리자용)
    public MonthlyPayoutDetailResponseDTO getMonthlyPayoutDetail(String yearMonth) {
        // yearMonth를 파싱해서 해당 월의 시작일과 종료일 계산
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 해당 월의 정산 목록 조회
        PageRequest pageRequest = PageRequest.of(0, 1000); // 충분히 큰 크기
        AdminPayoutSearchConditionDTO condition = AdminPayoutSearchConditionDTO.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();

        AdminSettlementPageResponseDTO<AdminPayoutResponseDTO> payoutPage = getPayoutList(pageRequest, condition);

        // 해당 월의 총 판매액과 수수료 계산
        Integer totalSales = salesLogRepository.sumTotalPriceBySoldAtBetween(startDate, endDate);
        Integer totalCommission = commissionLogRepository.sumCommissionAmountBySellerAndDateRange(null, startDate, endDate);

        return MonthlyPayoutDetailResponseDTO.builder()
                .yearMonth(yearMonth)
                .totalPayouts((int) payoutPage.getTotalElements())
                .totalAmount(payoutPage.getContent().stream()
                        .mapToInt(AdminPayoutResponseDTO::getPayoutAmount)
                        .sum())
                .totalSales(totalSales != null ? totalSales : 0)
                .totalCommission(totalCommission != null ? totalCommission : 0)
                .payoutDetails(payoutPage.getContent())
                .build();
    }

    // Response 변환 메서드들
    private AdminPayoutResponseDTO convertToAdminPayoutResponse(PayoutLog payout) {
        // PayoutLog의 sellerId가 Integer이므로 Long으로 변환
        Seller seller = sellerRepository.findById(payout.getSellerId().longValue())
                .orElse(null);

        return AdminPayoutResponseDTO.builder()
                .payoutId(payout.getId())
                .sellerId(payout.getSellerId())
                .sellerName(seller != null ? seller.getName() : "알 수 없음")
                .sellerEmail(seller != null ? seller.getEmail() : "")
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .totalSales(payout.getTotalSales())
                .totalCommission(payout.getTotalCommission())
                .payoutAmount(payout.getPayoutAmount())
                .processedAt(payout.getProcessedAt())
                .build();
    }

    private SalesDetailResponseDTO convertToSalesDetail(SalesLog salesLog) {
        return SalesDetailResponseDTO.builder()
                .salesLogId(salesLog.getId())
                .orderItemId(salesLog.getOrderItemId())
                .productId(salesLog.getProductId())
                .quantity(salesLog.getQuantity())
                .unitPrice(salesLog.getUnitPrice())
                .totalPrice(salesLog.getTotalPrice())
                .soldAt(salesLog.getSoldAt())
                .build();
    }

    private CommissionDetailResponseDTO convertToCommissionDetail(CommissionLog commissionLog) {
        return CommissionDetailResponseDTO.builder()
                .commissionLogId(commissionLog.getId())
                .salesLogId(commissionLog.getSalesLogId())
                .commissionRate(commissionLog.getCommissionRate())
                .commissionAmount(commissionLog.getCommissionAmount())
                .recordedAt(commissionLog.getRecordedAt())
                .build();
    }
}