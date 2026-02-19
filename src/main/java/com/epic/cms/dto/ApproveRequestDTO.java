package com.epic.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestDTO {

    @NotBlank(message = "Request status code is required")
    @Pattern(regexp = "APPR|RJCT", message = "Request status code must be APPR or RJCT")
    private String requestStatusCode;

    @Size(max = 500, message = "Remark cannot exceed 500 characters")
    private String remark;
}
