package com.epic.cms.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    
    private String cardNumber;
    private LocalDate expiryDate;
    private String cardStatus;
    private BigDecimal creditLimit;
    private BigDecimal cashLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private LocalDateTime lastUpdateTime;
    private String lastUpdatedUser;
}
