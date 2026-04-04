package com.railyatra.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "passenger_profiles", indexes = {
    @Index(name = "idx_profiles_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user"})
public class PassengerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Min(1) @Max(125)
    @Column(nullable = false)
    private Integer age;

    @NotBlank
    @Column(nullable = false, length = 15)
    private String gender;

    @Column(name = "id_type", length = 20)
    private String idType;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
