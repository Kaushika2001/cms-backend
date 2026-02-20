package com.epic.cms.dto;

import lombok.*;

/**
 * Simple DTO to return just the maskedCardId.
 * Used for lookup operations where only the ID is needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaskedCardIdDTO {
    
    private String maskedCardId; // e.g., CRD-M-943E49CC
    private String cardNumber;   // Masked card number (e.g., 589925******0233)
}
