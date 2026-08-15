package com.cinebook.payment.service;

import com.cinebook.payment.entity.RazorpayConfig;
import com.cinebook.payment.exception.ApiException;
import com.cinebook.payment.repository.RazorpayConfigRepository;
import com.cinebook.payment.util.CryptoUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RazorpayConfigService {

    private final RazorpayConfigRepository repository;
    private final CryptoUtil cryptoUtil;

    @Value("${app.razorpay.default-key-id:}")
    private String seedKeyId;
    @Value("${app.razorpay.default-key-secret:}")
    private String seedKeySecret;

    public RazorpayConfigService(RazorpayConfigRepository repository, CryptoUtil cryptoUtil) {
        this.repository = repository;
        this.cryptoUtil = cryptoUtil;
    }

    @PostConstruct
    @Transactional
    public void seedFromEnvIfEmpty() {
        if (repository.count() == 0 && !seedKeyId.isBlank() && !seedKeySecret.isBlank()) {
            RazorpayConfig config = new RazorpayConfig();
            config.setKeyId(seedKeyId);
            config.setKeySecretEncrypted(cryptoUtil.encrypt(seedKeySecret));
            repository.save(config);
        }
    }

    public record Credentials(String keyId, String keySecret) {}

    public Credentials getCredentialsOrThrow() {
        RazorpayConfig config = repository.findById(1).orElse(null);
        if (config == null || config.getKeyId() == null || config.getKeyId().isBlank()) {
            throw new ApiException("Payment gateway not configured. Please contact support.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new Credentials(config.getKeyId(), cryptoUtil.decrypt(config.getKeySecretEncrypted()));
    }

    /** keyId only — the secret is NEVER returned by any endpoint, even to admins, once saved. */
    public String getPublicKeyIdOrNull() {
        return repository.findById(1).map(RazorpayConfig::getKeyId).orElse(null);
    }

    @Transactional
    public void save(String keyId, String keySecret) {
        RazorpayConfig config = repository.findById(1).orElseGet(RazorpayConfig::new);
        config.setId(1);
        config.setKeyId(keyId.trim());
        config.setKeySecretEncrypted(cryptoUtil.encrypt(keySecret.trim()));
        repository.save(config);
    }
}
