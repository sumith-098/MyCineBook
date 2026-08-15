package com.cinebook.payment.entity;

import jakarta.persistence.*;

/** Singleton row (id always 1) — platform-wide Razorpay credentials, admin-managed. */
@Entity
@Table(name = "razorpay_config")
public class RazorpayConfig {

    @Id
    private Integer id = 1;

    @Column(name = "key_id", length = 100)
    private String keyId;

    // AES-256-GCM encrypted (see CryptoUtil) — never returned in any API response.
    @Column(name = "key_secret_encrypted", length = 500)
    private String keySecretEncrypted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public String getKeySecretEncrypted() { return keySecretEncrypted; }
    public void setKeySecretEncrypted(String keySecretEncrypted) { this.keySecretEncrypted = keySecretEncrypted; }
}
