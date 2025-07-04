package com.realive.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionPaymentRequestDTO {
    @NotNull
    private final Integer auctionId;
    
    @NotNull
    private final String paymentKey;

    @NotNull
    private Long amount;

} 