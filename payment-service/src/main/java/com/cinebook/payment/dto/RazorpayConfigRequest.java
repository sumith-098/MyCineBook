package com.cinebook.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class RazorpayConfigRequest {
    @NotBlank(message = "Key ID is required")
    private String keyId;
    @NotBlank(message = "Key secret is required")
    private String keySecret;

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public String getKeySecret() { return keySecret; }
    public void setKeySecret(String keySecret) { this.keySecret = keySecret; }
}
