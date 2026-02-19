package com.epic.cms.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDTO {

    private String cardNumber;
    private LocalDate expiryDate;
    private String cardStatus;
    private String cardStatusDescription;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;
}
