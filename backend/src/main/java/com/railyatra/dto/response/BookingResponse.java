package com.railyatra.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String pnr;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destStation;
    private LocalDate journeyDate;
    private String classType;
    private String status;
    private Integer waitlistNumber;
    private BigDecimal totalAmount;
    private BigDecimal convenienceFee;
    private LocalDateTime bookedAt;
    private List<PassengerInfo> passengers;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PassengerInfo {
        private String name;
        private Integer age;
        private String gender;
        private String seatNumber;
        private String berthPref;
    }
}
