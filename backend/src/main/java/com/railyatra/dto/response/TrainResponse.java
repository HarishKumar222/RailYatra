package com.railyatra.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TrainResponse {
    private Long id;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destStation;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer journeyMins;
    private String daysOfRun;
    private ClassAvailability sl;
    private ClassAvailability threeA;
    private ClassAvailability twoA;
    private ClassAvailability oneA;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClassAvailability {
        private String classCode;
        private String className;
        private Integer availableSeats;
        private Integer totalSeats;
        private Integer waitlistCount;
        private BigDecimal baseFare;
        private BigDecimal dynamicFare;
        private BigDecimal totalFare;
        private String availabilityStatus;
    }
}
