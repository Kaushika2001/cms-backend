package com.epic.cms.service.impl;

import com.epic.cms.config.EncryptionConfig;
import com.epic.cms.dto.CardDTO;
import com.epic.cms.dto.CardResponseDTO;
import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.dto.MaskedCardIdDTO;
import com.epic.cms.exception.DuplicateResourceException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.repository.ICardStatusRepository;
import com.epic.cms.service.CardService;
import com.epic.cms.util.CardEncryptionUtil;
import com.epic.cms.util.CardMaskingUtil;
import com.epic.cms.util.EncryptionUtil;
import com.epic.cms.util.ExpiryDateUtil;
import com.epic.cms.util.LoggerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final ICardRepository cardRepository;
    private final ICardStatusRepository cardStatusRepository;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final EncryptionConfig encryptionConfig;

    public List<CardDTO> getAllCards() {
        log.info("Fetching all cards");
        return cardRepository.findAll()
                .stream()
                .peek(this::decryptCardNumber) // Decrypt before converting
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getAllCardsMasked() {
        log.info("Fetching all cards with masked card numbers");
        return cardRepository.findAll()
                .stream()
                .peek(this::decryptCardNumber) // Decrypt before masking
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getCardByCardNumber(String cardNumber) {
        log.info("Fetching card with masked card number");
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        decryptCardNumber(card);
        return convertToDTO(card);
    }

    public CardResponseDTO getCardByCardNumberMasked(String cardNumber) {
        log.info("Fetching card with masked card number");
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        decryptCardNumber(card);
        return convertToResponseDTO(card);
    }

    public List<CardDTO> getCardsByStatus(String cardStatus) {
        log.info("Fetching cards with status: {}", cardStatus);
        return cardRepository.findByCardStatus(cardStatus)
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getCardsByStatusMasked(String cardStatus) {
        log.info("Fetching cards with status (masked): {}", cardStatus);
        return cardRepository.findByCardStatus(cardStatus)
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getExpiredCards() {
        log.info("Fetching expired cards");
        return cardRepository.findExpiredCards()
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getExpiredCardsMasked() {
        log.info("Fetching expired cards (masked)");
        return cardRepository.findExpiredCards()
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getCardsExpiringInDays(int days) {
        log.info("Fetching cards expiring in {} days", days);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return cardRepository.findExpiringBetween(startDate, endDate)
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getCardsExpiringInDaysMasked(int days) {
        log.info("Fetching cards expiring in {} days (masked)", days);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return cardRepository.findExpiringBetween(startDate, endDate)
                .stream()
                .peek(this::decryptCardNumber)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CardDTO createCard(CreateCardDTO createCardDTO) {
        LoggerUtil.info(CardServiceImpl.class, "Creating new card");

        // Check if card already exists
        if (cardRepository.existsByCardNumber(createCardDTO.getCardNumber())) {
            throw new DuplicateResourceException("Card", "cardNumber", createCardDTO.getCardNumber());
        }

        // Validate card status exists
        cardStatusRepository.findByStatusCode(createCardDTO.getCardStatus())
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", createCardDTO.getCardStatus()));

        try {
            // Get storage encryption key
            String storageKey = encryptionConfig.getStorageKey();
            
            // Encrypt ONLY the card number with storage key (AES-256-GCM)
            String encryptedCardNumber = EncryptionUtil.encrypt(
                createCardDTO.getCardNumber(), 
                storageKey
            );
            
            LoggerUtil.debug(CardServiceImpl.class, "Successfully encrypted card number for storage");
            
            // Convert expiry date from MM-YYYY to LocalDate
            LocalDate expiryLocalDate = ExpiryDateUtil.convertToLocalDate(createCardDTO.getExpiryDate());
            
            Card card = Card.builder()
                    .cardNumber(encryptedCardNumber) // Store encrypted card number
                    .expiryDate(expiryLocalDate)
                    .cardStatus(createCardDTO.getCardStatus())
                    .creditLimit(createCardDTO.getCreditLimit())
                    .cashLimit(createCardDTO.getCashLimit())
                    .availableCreditLimit(createCardDTO.getAvailableCreditLimit())
                    .availableCashLimit(createCardDTO.getAvailableCashLimit())
                    .build();

            cardRepository.insert(card);
            LoggerUtil.info(CardServiceImpl.class, "Card created successfully with encrypted card number");

            // Retrieve and return the created card (will decrypt for response)
            Card createdCard = cardRepository.findByCardNumber(card.getCardNumber()).get();
            decryptCardNumber(createdCard); // Decrypt for DTO conversion
            return convertToDTO(createdCard);
            
        } catch (Exception e) {
            LoggerUtil.error(CardServiceImpl.class, "Error creating card", e);
            throw new RuntimeException("Failed to create card", e);
        }
    }

    public CardDTO updateCard(String cardNumber, CreateCardDTO updateCardDTO) {
        log.info("Updating card");

        // Check if card exists
        Card existingCard = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));

        // Validate new card status exists
        cardStatusRepository.findByStatusCode(updateCardDTO.getCardStatus())
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", updateCardDTO.getCardStatus()));

        // Convert expiry date from MM-YYYY to LocalDate
        LocalDate expiryLocalDate = ExpiryDateUtil.convertToLocalDate(updateCardDTO.getExpiryDate());

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .expiryDate(expiryLocalDate)
                .cardStatus(updateCardDTO.getCardStatus())
                .creditLimit(updateCardDTO.getCreditLimit())
                .cashLimit(updateCardDTO.getCashLimit())
                .availableCreditLimit(updateCardDTO.getAvailableCreditLimit())
                .availableCashLimit(updateCardDTO.getAvailableCashLimit())
                .build();

        cardRepository.update(card);
        log.info("Card updated successfully");

        return convertToDTO(cardRepository.findByCardNumber(cardNumber).get());
    }

    public CardDTO updateCardStatus(String cardNumber, String status) {
        log.info("Updating card status to {}", status);

        // Check if card exists
        cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));

        // Validate new card status exists
        cardStatusRepository.findByStatusCode(status)
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", status));

        cardRepository.updateCardStatus(cardNumber, status);
        log.info("Card status updated successfully");

        return convertToDTO(cardRepository.findByCardNumber(cardNumber).get());
    }

    public Long getCardCountByStatus(String cardStatus) {
        log.info("Getting card count for status: {}", cardStatus);
        return cardRepository.countByStatus(cardStatus);
    }

    @Override
    public CardResponseDTO getMaskedCardIdForCardNumber(String cardNumber) {
        log.info("Getting maskedCardId for card number");
        
        Card card;
        
        // Check if the card number is masked (contains asterisks)
        if (CardMaskingUtil.isMasked(cardNumber)) {
            log.info("Card number is masked, performing pattern lookup");
            card = cardRepository.findByMaskedCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardNumber", cardNumber));
        } else {
            log.info("Card number is unmasked, performing direct lookup");
            card = cardRepository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        }
        
        // Decrypt card number before conversion
        decryptCardNumber(card);
        
        return convertToResponseDTO(card);
    }

    @Override
    public MaskedCardIdDTO getMaskedCardIdOnly(String cardNumber) {
        log.info("Getting maskedCardId only for card number");
        
        Card card;
        
        // Check if the card number is masked (contains asterisks)
        if (CardMaskingUtil.isMasked(cardNumber)) {
            log.info("Card number is masked, performing pattern lookup");
            card = cardRepository.findByMaskedCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardNumber", cardNumber));
        } else {
            log.info("Card number is unmasked, performing direct lookup");
            card = cardRepository.findByCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        }
        
        // Decrypt card number before masking
        decryptCardNumber(card);
        
        return MaskedCardIdDTO.builder()
                .maskedCardId(CardMaskingUtil.generateMaskedCardId(card.getCardNumber()))
                .cardNumber(CardMaskingUtil.mask(card.getCardNumber()))
                .build();
    }

    @Override
    public CardResponseDTO updateCardStatusEncrypted(String cardNumber, String newStatus) {
        LoggerUtil.info(CardServiceImpl.class, "Updating card status with encrypted/masked card number");
        
        Card card;
        
        // Check if the card number is masked (contains asterisks)
        if (CardMaskingUtil.isMasked(cardNumber)) {
            LoggerUtil.info(CardServiceImpl.class, "Card number is masked, performing pattern lookup");
            card = cardRepository.findByMaskedCardNumber(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardNumber", cardNumber));
        } else {
            LoggerUtil.info(CardServiceImpl.class, "Card number is unmasked, performing direct lookup");
            // Card number in DB is encrypted, so we need to search by the encrypted value
            // First, try to find by searching all cards and matching decrypted values
            card = findCardByUnencryptedNumber(cardNumber);
        }
        
        // Validate new card status exists
        cardStatusRepository.findByStatusCode(newStatus)
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", newStatus));
        
        // Update the card status using the encrypted card number from database
        cardRepository.updateCardStatus(card.getCardNumber(), newStatus);
        LoggerUtil.info(CardServiceImpl.class, "Card status updated successfully to: {}", newStatus);
        
        // Retrieve updated card and return with masked card number
        Card updatedCard = cardRepository.findByCardNumber(card.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", card.getCardNumber()));
        
        // Decrypt card number before conversion
        decryptCardNumber(updatedCard);
        
        return convertToResponseDTO(updatedCard);
    }
    
    /**
     * Find card by unencrypted card number.
     * Searches through all cards, decrypts them, and finds matching card number.
     * 
     * @param plainCardNumber Unencrypted card number to search for
     * @return Card entity with matching card number
     * @throws ResourceNotFoundException if card not found
     */
    private Card findCardByUnencryptedNumber(String plainCardNumber) {
        LoggerUtil.debug(CardServiceImpl.class, "Searching for card by unencrypted number: {}", 
            CardMaskingUtil.mask(plainCardNumber));
        
        String storageKey = encryptionConfig.getStorageKey();
        
        return cardRepository.findAll().stream()
            .filter(c -> {
                try {
                    // Decrypt the stored card number
                    String decryptedCardNumber = EncryptionUtil.decrypt(c.getCardNumber(), storageKey);
                    return decryptedCardNumber.equals(plainCardNumber);
                } catch (Exception e) {
                    LoggerUtil.debug(CardServiceImpl.class, "Failed to decrypt card number during search", e);
                    return false;
                }
            })
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", 
                CardMaskingUtil.mask(plainCardNumber)));
    }
    
    /**
     * Decrypt card number in a card entity.
     * Modifies the card object in place.
     * 
     * @param card Card entity with encrypted card number
     */
    private void decryptCardNumber(Card card) {
        if (card == null || card.getCardNumber() == null) {
            return;
        }
        
        try {
            // Check if card number looks like it's encrypted (contains a dot separator)
            if (card.getCardNumber().contains(".")) {
                String storageKey = encryptionConfig.getStorageKey();
                String decryptedCardNumber = EncryptionUtil.decrypt(
                    card.getCardNumber(), 
                    storageKey
                );
                card.setCardNumber(decryptedCardNumber);
                LoggerUtil.debug(CardServiceImpl.class, "Successfully decrypted card number");
            } else {
                LoggerUtil.debug(CardServiceImpl.class, "Card number appears to be unencrypted, skipping decryption");
            }
        } catch (Exception e) {
            LoggerUtil.error(CardServiceImpl.class, "Error decrypting card number for card", e);
            throw new RuntimeException("Failed to decrypt card number", e);
        }
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = CardDTO.builder()
                .cardNumber(card.getCardNumber())
                .expiryDate(card.getExpiryDate())
                .cardStatus(card.getCardStatus())
                .creditLimit(card.getCreditLimit())
                .cashLimit(card.getCashLimit())
                .availableCreditLimit(card.getAvailableCreditLimit())
                .availableCashLimit(card.getAvailableCashLimit())
                .lastUpdateTime(card.getLastUpdateTime())
                .build();

        // Fetch status description
        cardStatusRepository.findByStatusCode(card.getCardStatus())
                .ifPresent(status -> dto.setCardStatusDescription(status.getDescription()));

        return dto;
    }

    private CardResponseDTO convertToResponseDTO(Card card) {
        CardResponseDTO dto = CardResponseDTO.builder()
                .maskedCardId(CardMaskingUtil.generateMaskedCardId(card.getCardNumber()))
                .cardNumber(CardMaskingUtil.mask(card.getCardNumber()))
                .expiryDate(card.getExpiryDate())
                .cardStatus(card.getCardStatus())
                .creditLimit(card.getCreditLimit())
                .cashLimit(card.getCashLimit())
                .availableCreditLimit(card.getAvailableCreditLimit())
                .availableCashLimit(card.getAvailableCashLimit())
                .lastUpdateTime(card.getLastUpdateTime())
                .build();

        // Fetch status description
        cardStatusRepository.findByStatusCode(card.getCardStatus())
                .ifPresent(status -> dto.setCardStatusDescription(status.getDescription()));

        return dto;
    }
}
