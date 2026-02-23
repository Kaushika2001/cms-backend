package com.epic.cms.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardDTO {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{13,16}", message = "Card number must be 13-16 digits")
    private String cardNumber;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])-(20[2-9][0-9]|2[1-9][0-9]{2})", message = "Expiry date must be in MM-YYYY format (e.g., 02-2026)")
    private String expiryDate;

    @NotBlank(message = "Card status is required")
    @Pattern(regexp = "IACT|CACT|DACT", message = "Card status must be IACT, CACT, or DACT")
    private String cardStatus;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.0", message = "Credit limit must be non-negative")
    private BigDecimal creditLimit;

    @NotNull(message = "Cash limit is required")
    @DecimalMin(value = "0.0", message = "Cash limit must be non-negative")
    private BigDecimal cashLimit;

    @NotNull(message = "Available credit limit is required")
    @DecimalMin(value = "0.0", message = "Available credit limit must be non-negative")
    private BigDecimal availableCreditLimit;

    @NotNull(message = "Available cash limit is required")
    @DecimalMin(value = "0.0", message = "Available cash limit must be non-negative")
    private BigDecimal availableCashLimit;
}
