package com.railyatra.service;

import com.railyatra.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    //@Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("🚂 Booking Confirmed — PNR: " + booking.getPnr());
            helper.setText(buildHtml(booking), true);
            mailSender.send(msg);
            log.info("Confirmation email sent for PNR: {}", booking.getPnr());
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
        }
    }

    //@Async
    public void sendWaitlistConfirmation(Booking booking) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("🎉 Your WL ticket is CONFIRMED! PNR: " + booking.getPnr());
            helper.setText("<h2>Great news! Your waitlisted ticket is now CONFIRMED.</h2>" +
                           "<p>PNR: <strong>" + booking.getPnr() + "</strong></p>" +
                           "<p>Train: " + booking.getTrain().getTrainName() + "</p>", true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("WL email send failed: {}", e.getMessage());
        }
    }

    //@Async
    public void sendRefundNotification(Booking booking, BigDecimal refundAmount) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("💰 Refund Initiated — PNR: " + booking.getPnr());
            helper.setText("<h2>Refund of ₹" + refundAmount + " has been initiated.</h2>" +
                           "<p>It will reflect in 5-7 business days.</p>", true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Refund email failed: {}", e.getMessage());
        }
    }

    private String buildHtml(Booking b) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <div style="background:#1a56db;padding:20px;color:white;text-align:center">
                <h1>🚂 RailYatra</h1><h2>Booking Confirmed!</h2>
              </div>
              <div style="padding:20px;background:#f9fafb;border:1px solid #e5e7eb">
                <p><strong>PNR:</strong> <span style="font-size:1.4em;color:#1a56db">%s</span></p>
                <p><strong>Train:</strong> %s (%s)</p>
                <p><strong>From:</strong> %s &rarr; <strong>To:</strong> %s</p>
                <p><strong>Date:</strong> %s | <strong>Class:</strong> %s</p>
                <p><strong>Status:</strong> <span style="color:green">%s</span></p>
                <p><strong>Total Paid:</strong> ₹%s</p>
                <hr/>
                <p style="color:#6b7280;font-size:0.85em">Powered by RailYatra 🚀</p>
              </div>
            </div>
            """.formatted(
                b.getPnr(), b.getTrain().getTrainName(), b.getTrain().getTrainNumber(),
                b.getTrain().getSourceStation(), b.getTrain().getDestStation(),
                b.getJourneyDate(), b.getClassType(), b.getStatus(), b.getTotalAmount());
    }
}
