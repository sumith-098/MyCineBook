package com.cinebook.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends the two auth-related branded emails (OTP + owner-approval notice).
 * Booking-confirmation emails live in notification-service since that's
 * triggered by booking-service, not auth-service.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.dev-mode:false}")
    private boolean devMode;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isDevMode() { return devMode; }

    public boolean sendOtpEmail(String toEmail, String otp, String name) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px">
              <h2 style="color:#e50914">CineBook</h2>
              <p>Hi %s,</p>
              <p>Your one-time verification code is:</p>
              <div style="font-size:32px;font-weight:bold;letter-spacing:8px;text-align:center;padding:16px;background:#f5f5f5;border-radius:6px">%s</div>
              <p style="color:#666;font-size:13px">This code expires in 10 minutes. If you didn't request this, you can ignore this email.</p>
            </div>
            """.formatted(name == null || name.isBlank() ? "there" : name, otp);
        return send(toEmail, "Your CineBook OTP", html);
    }

    public boolean sendOwnerApprovedEmail(String toEmail, String name, String loginUrl) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;border:1px solid #eee;border-radius:8px">
              <h2 style="color:#e50914">CineBook</h2>
              <p>Hi %s,</p>
              <p>🎉 Your theater owner account has been approved! You can now log in and start listing your theaters and movies.</p>
              <p><a href="%s" style="display:inline-block;padding:10px 20px;background:#e50914;color:#fff;text-decoration:none;border-radius:4px">Log in to CineBook</a></p>
            </div>
            """.formatted(name, loginUrl);
        return send(toEmail, "CineBook Owner Account Approved", html);
    }

    private boolean send(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromAddress, fromName);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
            return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.warn("Email send failed for {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
