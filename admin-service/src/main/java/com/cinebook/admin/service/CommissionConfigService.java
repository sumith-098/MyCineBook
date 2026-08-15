package com.cinebook.admin.service;

import com.cinebook.admin.entity.CommissionConfig;
import com.cinebook.admin.repository.CommissionConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CommissionConfigService {

    private final CommissionConfigRepository repository;

    @Value("${app.commission.default-pct:10.0}")
    private double defaultPct;

    public CommissionConfigService(CommissionConfigRepository repository) {
        this.repository = repository;
    }

    public BigDecimal getCommissionPct() {
        return repository.findById(1).map(CommissionConfig::getCommissionPct).orElse(BigDecimal.valueOf(defaultPct));
    }

    @Transactional
    public void setCommissionPct(BigDecimal pct) {
        CommissionConfig config = repository.findById(1).orElseGet(CommissionConfig::new);
        config.setId(1);
        config.setCommissionPct(pct);
        repository.save(config);
    }
}
