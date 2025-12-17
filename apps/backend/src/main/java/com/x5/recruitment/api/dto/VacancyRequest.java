package com.x5.recruitment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyRequest {

    @Schema(description = "Program code. If omitted, generated from title")
    private String code;

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;
    private String department;
    private String location;
    private Integer positionsAvailable;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
