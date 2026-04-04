package com.railyatra.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull
    private Long trainId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;

    @NotBlank
    private String classType;

    @NotEmpty @Size(min = 1, max = 6)
    @Valid
    private List<PassengerDTO> passengers;

    @Data
    public static class PassengerDTO {
        @NotBlank @Size(min = 2, max = 100)
        private String name;

        @NotNull @Min(1) @Max(125)
        private Integer age;

        @NotBlank
        private String gender;

        private String berthPref;
    }
}
