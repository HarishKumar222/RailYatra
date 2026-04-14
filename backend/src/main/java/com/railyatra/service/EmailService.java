package com.railyatra.service;

import com.railyatra.entity.Booking;
import com.sendgrid.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String apiKey;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            sendEmail(
                booking.getUser().getEmail(),
                "🚂 Booking Confirmed — PNR: " + booking.getPnr(),
                buildHtml(
                    booking.getPnr(),
                    booking.getTrain().getTrainName(),
                    booking.getTrain().getTrainNumber(),
                    booking.getTrain().getSourceStation(),
                    booking.getTrain().getDestStation(),
                    booking.getJourneyDate().toString(),
                    booking.getClassType(),
                    booking.getStatus().name(),
                    booking.getTotalAmount().toString()
                )
            );
        } catch (Exception e) {
            log.error("Email send failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendWaitlistConfirmation(Booking booking) {
        sendEmail(
            booking.getUser().getEmail(),
            "🎉 WL Confirmed! PNR: " + booking.getPnr(),
            "<h2>Your waitlisted ticket is now CONFIRMED!</h2>"
        );
    }

    @Async
    public void sendRefundNotification(Booking booking, BigDecimal refundAmount) {
        sendEmail(
            booking.getUser().getEmail(),
            "💰 Refund Initiated — PNR: " + booking.getPnr(),
            "<h2>Refund of ₹" + refundAmount + " initiated.</h2>"
        );
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", html);

            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            log.info("Email sent: status {}", response.getStatusCode());

        } catch (Exception e) {
            log.error("SendGrid API failed: {}", e.getMessage());
        }
    }

    private String buildHtml(String pnr, String trainName, String trainNo,
                            String source, String dest, String date,
                            String classType, String status, String amount) {
        return """
            <h2>Booking Confirmed</h2>
            <p>PNR: %s</p>
            <p>Train: %s (%s)</p>
            """.formatted(pnr, trainName, trainNo);
    }
}
