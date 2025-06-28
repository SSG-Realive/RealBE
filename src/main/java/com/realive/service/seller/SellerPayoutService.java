package com.realive.service.seller;

import com.realive.dto.logs.PayoutLogDTO;
import com.realive.dto.logs.PayoutLogDetailDTO;
import com.realive.dto.seller.SellerPayoutSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface SellerPayoutService {
    List<PayoutLogDTO> getPayoutLogsBySellerId(Long sellerId);

    List<PayoutLogDTO> getPayoutLogsByDate(Long sellerId, LocalDate date);

    void generatePayoutLogIfNotExists(Long orderId);

    PayoutLogDetailDTO getPayoutLogDetail(Long sellerId, Integer payoutLogId);

    List<PayoutLogDTO> getPayoutLogsByPeriod(Long sellerId, LocalDate from, LocalDate to);

    SellerPayoutSummaryDTO getPayoutSummary(Long sellerId, LocalDate from, LocalDate to);


}
