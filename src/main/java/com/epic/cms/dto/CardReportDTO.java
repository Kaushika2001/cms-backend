package com.epic.cms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardReportDTO {
    
    // Card Information
    private String maskedCardNumber;
    private LocalDate expiryDate;
    private BigDecimal creditLimit;
    private String cardStatusDescription;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    
    // Audit Information
    private LocalDateTime lastUpdateTime;
    private String lastUpdateUser;
}
