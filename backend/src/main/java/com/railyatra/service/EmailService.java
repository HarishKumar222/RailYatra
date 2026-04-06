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

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            // Extract all values while session is still open
            String email     = booking.getUser().getEmail();
            String pnr       = booking.getPnr();
            String trainName = booking.getTrain().getTrainName();
            String trainNo   = booking.getTrain().getTrainNumber();
            String source    = booking.getTrain().getSourceStation();
            String dest      = booking.getTrain().getDestStation();
            String date      = booking.getJourneyDate().toString();
            String classType = booking.getClassType();
            String status    = booking.getStatus().name();
            String amount    = booking.getTotalAmount().toString();

            sendEmail(email,
                "🚂 Booking Confirmed — PNR: " + pnr,
                buildHtml(pnr, trainName, trainNo, source, dest,
                          date, classType, status, amount));
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendWaitlistConfirmation(Booking booking) {
        try {
            String email = booking.getUser().getEmail();
            String pnr   = booking.getPnr();
            String train = booking.getTrain().getTrainName();
            sendEmail(email,
                "🎉 WL Confirmed! PNR: " + pnr,
                "<h2>Your waitlisted ticket is now CONFIRMED!</h2>" +
                "<p>PNR: <strong>" + pnr + "</strong></p>" +
                "<p>Train: " + train + "</p>");
        } catch (Exception e) {
            log.error("WL email failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendRefundNotification(Booking booking, BigDecimal refundAmount) {
        try {
            String email = booking.getUser().getEmail();
            String pnr   = booking.getPnr();
            sendEmail(email,
                "💰 Refund Initiated — PNR: " + pnr,
                "<h2>Refund of ₹" + refundAmount + " initiated.</h2>" +
                "<p>Reflects in 5-7 business days.</p>");
        } catch (Exception e) {
            log.error("Refund email failed: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }
    }

    private String buildHtml(String pnr, String trainName, String trainNo,
                              String source, String dest, String date,
                              String classType, String status, String amount) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
              <div style="background:#1a56db;padding:20px;color:white;text-align:center">
                <h1>🚂 RailYatra</h1><h2>Booking Confirmed!</h2>
              </div>
              <div style="padding:20px;background:#f9fafb;border:1px solid #e5e7eb">
                <p><strong>PNR:</strong>
                   <span style="font-size:1.4em;color:#1a56db">%s</span></p>
                <p><strong>Train:</strong> %s (%s)</p>
                <p><strong>From:</strong> %s &rarr; <strong>To:</strong> %s</p>
                <p><strong>Date:</strong> %s | <strong>Class:</strong> %s</p>
                <p><strong>Status:</strong>
                   <span style="color:green">%s</span></p>
                <p><strong>Total Paid:</strong> ₹%s</p>
                <hr/>
                <p style="color:#6b7280;font-size:0.85em">
                   Powered by RailYatra 🚀</p>
              </div>
            </div>
            """.formatted(pnr, trainName, trainNo,
                          source, dest, date, classType, status, amount);
    }
}
