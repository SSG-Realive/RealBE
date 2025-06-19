package com.realive.service.admin.logs;

import com.realive.domain.logs.PenaltyLog;
import com.realive.dto.logs.PenaltyLogCreateRequest;
import com.realive.repository.logs.PenaltyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPenaltyService {
    private final PenaltyLogRepository penaltyLogRepository;

    public PenaltyLog create(PenaltyLogCreateRequest request) {
        PenaltyLog entity = new PenaltyLog();
        entity.setCustomerId(request.getCustomerId());
        entity.setReason(request.getReason());
        entity.setPoints(request.getPoints());
        entity.setDescription(request.getDescription());
        return penaltyLogRepository.save(entity);
    }
}