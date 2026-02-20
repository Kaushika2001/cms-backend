package com.epic.cms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequestDTO {

    // NEW: Preferred way to identify cards (e.g., CRD-M-943E49CC)
    private String maskedCardId;

    // LEGACY: Still supported for backward compatibility (13-16 digit card number)
    @Pattern(regexp = "\\d{13,16}", message = "Card number must be 13-16 digits", 
             groups = ValidationGroups.CardNumberValidation.class)
    private String cardNumber;

    @NotBlank(message = "Request reason code is required")
    @Pattern(regexp = "ACTI|CDCL", message = "Request reason code must be ACTI or CDCL")
    private String requestReasonCode;

    @Size(max = 500, message = "Remark cannot exceed 500 characters")
    private String remark;

    /**
     * Validates that either maskedCardId or cardNumber is provided.
     */
    @AssertTrue(message = "Either maskedCardId or cardNumber must be provided")
    public boolean isEitherCardIdentifierProvided() {
        return (maskedCardId != null && !maskedCardId.isBlank()) ||
               (cardNumber != null && !cardNumber.isBlank());
    }

    /**
     * Validation groups for conditional validation.
     */
    public interface ValidationGroups {
        interface CardNumberValidation {}
    }
}
