package com.railyatra.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "passengers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"booking"})
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String name;

    @Min(1) @Max(125)
    @Column(nullable = false)
    private Integer age;

    @NotBlank
    @Column(nullable = false, length = 15)
    private String gender;

    @Column(name = "seat_number", length = 10)
    private String seatNumber;

    @Column(name = "berth_pref", length = 20)
    private String berthPref;
}
