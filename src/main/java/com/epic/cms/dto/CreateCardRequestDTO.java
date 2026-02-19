package com.epic.cms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequestDTO {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{13,16}", message = "Card number must be 13-16 digits")
    private String cardNumber;

    @NotBlank(message = "Request reason code is required")
    @Pattern(regexp = "ACTI|CDCL", message = "Request reason code must be ACTI or CDCL")
    private String requestReasonCode;

    @Size(max = 500, message = "Remark cannot exceed 500 characters")
    private String remark;
}
