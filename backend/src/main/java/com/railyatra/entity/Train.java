package com.railyatra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "trains", indexes = {
    @Index(name = "idx_trains_route", columnList = "source_station,dest_station"),
    @Index(name = "idx_trains_number", columnList = "train_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"bookings"})
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "train_number", unique = true, nullable = false, length = 10)
    private String trainNumber;

    @NotBlank
    @Column(name = "train_name", nullable = false, length = 100)
    private String trainName;

    @NotBlank
    @Column(name = "source_station", nullable = false, length = 100)
    private String sourceStation;

    @NotBlank
    @Column(name = "dest_station", nullable = false, length = 100)
    private String destStation;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    @Min(1)
    @Column(name = "journey_mins", nullable = false)
    private Integer journeyMins;

    @Column(name = "total_seats_sl") @Builder.Default private Integer totalSeatsSl = 0;
    @Column(name = "total_seats_3a") @Builder.Default private Integer totalSeats3a = 0;
    @Column(name = "total_seats_2a") @Builder.Default private Integer totalSeats2a = 0;
    @Column(name = "total_seats_1a") @Builder.Default private Integer totalSeats1a = 0;

    @Column(name = "base_fare_sl", precision = 8, scale = 2) @Builder.Default private BigDecimal baseFareSl = BigDecimal.ZERO;
    @Column(name = "base_fare_3a", precision = 8, scale = 2) @Builder.Default private BigDecimal baseFare3a = BigDecimal.ZERO;
    @Column(name = "base_fare_2a", precision = 8, scale = 2) @Builder.Default private BigDecimal baseFare2a = BigDecimal.ZERO;
    @Column(name = "base_fare_1a", precision = 8, scale = 2) @Builder.Default private BigDecimal baseFare1a = BigDecimal.ZERO;

    @Column(name = "is_active") @Builder.Default private Boolean isActive = true;

    @Column(name = "days_of_run", length = 50) @Builder.Default
    private String daysOfRun = "DAILY";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @JsonIgnore
    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    public Integer getTotalSeatsByClass(String classType) {
        return switch (classType.toUpperCase()) {
            case "SL" -> totalSeatsSl;
            case "3A" -> totalSeats3a;
            case "2A" -> totalSeats2a;
            case "1A" -> totalSeats1a;
            default -> 0;
        };
    }

    public BigDecimal getBaseFareByClass(String classType) {
        return switch (classType.toUpperCase()) {
            case "SL" -> baseFareSl;
            case "3A" -> baseFare3a;
            case "2A" -> baseFare2a;
            case "1A" -> baseFare1a;
            default -> BigDecimal.ZERO;
        };
    }
}
