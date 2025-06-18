package com.realive.dto.logs;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PenaltyLogCreateRequest {
    private Integer customerId;
    private String reason;
    private Integer points;
    private String description;
}