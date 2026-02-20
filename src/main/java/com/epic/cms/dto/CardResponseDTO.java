package com.epic.cms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Card with masked card number.
 * Used in GET requests to hide sensitive card number information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDTO {

    private String maskedCardId; // Unique identifier (e.g., CRD-M-943E49CC)
    private String cardNumber; // This will be masked (e.g., 123456******3456)
    private LocalDate expiryDate;
    private String cardStatus;
    private String cardStatusDescription;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;
}
