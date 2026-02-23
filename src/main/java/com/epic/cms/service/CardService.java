package com.epic.cms.service;

import com.epic.cms.dto.CardResponseDTO;
import com.epic.cms.dto.MaskedCardIdDTO;

public interface CardService {
    Long getCardCountByStatus(String cardStatus);
    
    /**
     * Get maskedCardId for a given card number (masked or unmasked).
     * @param cardNumber The card number (can be masked like 589925******0233 or unmasked)
     * @return CardResponseDTO containing the maskedCardId and other card details
     */
    CardResponseDTO getMaskedCardIdForCardNumber(String cardNumber);
    
    /**
     * Get only the maskedCardId for a given card number (masked or unmasked).
     * @param cardNumber The card number (can be masked like 589925******0233 or unmasked)
     * @return MaskedCardIdDTO containing only the maskedCardId and masked card number
     */
    MaskedCardIdDTO getMaskedCardIdOnly(String cardNumber);
    
    /**
     * Update card status using encrypted/masked card number.
     * Supports masked card numbers (like 453201******0366) by performing pattern lookup.
     * 
     * @param cardNumber The card number (can be masked like 453201******0366 or full number)
     * @param newStatus The new status (IACT, CACT, or DACT)
     * @return CardResponseDTO with updated card details and masked card number
     */
    CardResponseDTO updateCardStatusEncrypted(String cardNumber, String newStatus);
}
