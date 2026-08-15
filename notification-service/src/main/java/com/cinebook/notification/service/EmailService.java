package com.cinebook.notification.service;

import com.cinebook.notification.dto.BookingConfirmationRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

/** Same dark/gold branded theme as the original app's emails/booking_confirm.html template. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendBookingConfirmation(BookingConfirmationRequest req) {
        String seatsJoined = String.join(", ", req.getSeats());
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"/></head>
            <body style="background:#0a0a14;margin:0;padding:0;font-family:'Helvetica Neue',Arial,sans-serif">
            <div style="max-width:560px;margin:40px auto;background:#161625;border-radius:16px;overflow:hidden;border:1px solid #252535">
              <div style="background:linear-gradient(135deg,#f5c842,#ff9d00);padding:28px;text-align:center">
                <div style="font-size:2rem">🎟</div>
                <div style="font-size:1.5rem;font-weight:800;color:#09090f;margin-top:6px">Booking Confirmed!</div>
                <div style="color:#09090f;font-size:.9rem;margin-top:4px;opacity:.7">CineBook</div>
              </div>
              <div style="padding:36px">
                <p style="color:#a0a0c0;font-size:1rem;margin:0 0 24px">Hi <strong style="color:#f0f0f8">%s</strong>, your ticket is ready!</p>
                <div style="background:#0f0f1a;border:1px solid #252535;border-radius:12px;overflow:hidden">
                  <div style="background:#1e1e30;padding:20px 24px">
                    <div style="font-size:1.3rem;font-weight:800;color:#f0f0f8;margin-bottom:4px">🎬 %s</div>
                  </div>
                  <div style="padding:20px 24px;display:grid;grid-template-columns:1fr 1fr;gap:16px">
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Theater</div>
                      <div style="color:#f0f0f8;font-weight:600;font-size:.95rem">%s</div>
                      <div style="color:#a0a0c0;font-size:.82rem">📍 %s</div>
                    </div>
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Screen</div>
                      <div style="color:#f0f0f8;font-weight:600;font-size:.95rem">%s</div>
                    </div>
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Date</div>
                      <div style="color:#f0f0f8;font-weight:600;font-size:.95rem">%s</div>
                    </div>
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Time</div>
                      <div style="color:#f0f0f8;font-weight:600;font-size:.95rem">%s</div>
                    </div>
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Seat(s)</div>
                      <div style="color:#f5c842;font-weight:800;font-size:1.2rem">%s</div>
                    </div>
                    <div>
                      <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px">Amount Paid</div>
                      <div style="color:#2ecc71;font-weight:700;font-size:.95rem">₹%s</div>
                    </div>
                  </div>
                  <div style="background:#161625;padding:16px 24px;border-top:1px solid #252535;text-align:center">
                    <div style="font-size:.75rem;color:#606080;text-transform:uppercase;letter-spacing:.06em;margin-bottom:6px">Booking Reference</div>
                    <div style="font-size:1.4rem;font-weight:800;color:#f5c842;letter-spacing:3px;font-family:monospace">%s</div>
                  </div>
                </div>
                <p style="color:#a0a0c0;font-size:.85rem;line-height:1.7;margin-top:24px">
                  Please arrive 15 minutes before showtime. Show this email or your booking reference at the counter.<br>
                  <strong style="color:#fc8181">Cancellations allowed up to 2 hours before showtime.</strong>
                </p>
              </div>
              <div style="background:#0f0f1a;padding:20px;text-align:center;border-top:1px solid #252535">
                <p style="color:#606080;font-size:.78rem;margin:0">© 2026 CineBook — Enjoy the show! 🍿</p>
              </div>
            </div>
            </body></html>
            """.formatted(
                nullSafe(req.getCustName(), "there"), req.getMovieTitle(),
                nullSafe(req.getTheaterName(), ""), nullSafe(req.getLocation(), ""),
                nullSafe(req.getScreen(), ""), req.getShowDate(), req.getShowTime(),
                seatsJoined, req.getTotalAmount(), req.getBookingRef()
            );

        return send(req.getEmail(), "🎟 Booking Confirmed — CineBook", html);
    }

    private String nullSafe(String s, String fallback) { return s == null || s.isBlank() ? fallback : s; }

    private boolean send(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromAddress, fromName);
            mailSender.send(message);
            log.info("Booking confirmation email sent to {}", toEmail);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.warn("Email send failed for {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
