package com.epic.cms.service.impl;

import com.epic.cms.dto.CardDTO;
import com.epic.cms.dto.CardResponseDTO;
import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.exception.DuplicateResourceException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.repository.ICardStatusRepository;
import com.epic.cms.service.CardService;
import com.epic.cms.util.CardEncryptionUtil;
import com.epic.cms.util.CardMaskingUtil;
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

    public List<CardDTO> getAllCards() {
        log.info("Fetching all cards");
        return cardRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getAllCardsMasked() {
        log.info("Fetching all cards with masked card numbers");
        return cardRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getCardByCardNumber(String cardNumber) {
        log.info("Fetching card with masked card number");
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        return convertToDTO(card);
    }

    public CardResponseDTO getCardByCardNumberMasked(String cardNumber) {
        log.info("Fetching card with masked card number");
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));
        return convertToResponseDTO(card);
    }

    public List<CardDTO> getCardsByStatus(String cardStatus) {
        log.info("Fetching cards with status: {}", cardStatus);
        return cardRepository.findByCardStatus(cardStatus)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getCardsByStatusMasked(String cardStatus) {
        log.info("Fetching cards with status (masked): {}", cardStatus);
        return cardRepository.findByCardStatus(cardStatus)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getExpiredCards() {
        log.info("Fetching expired cards");
        return cardRepository.findExpiredCards()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getExpiredCardsMasked() {
        log.info("Fetching expired cards (masked)");
        return cardRepository.findExpiredCards()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getCardsExpiringInDays(int days) {
        log.info("Fetching cards expiring in {} days", days);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return cardRepository.findExpiringBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardResponseDTO> getCardsExpiringInDaysMasked(int days) {
        log.info("Fetching cards expiring in {} days (masked)", days);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return cardRepository.findExpiringBetween(startDate, endDate)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public CardDTO createCard(CreateCardDTO createCardDTO) {
        log.info("Creating new card");

        // Check if card already exists
        if (cardRepository.existsByCardNumber(createCardDTO.getCardNumber())) {
            throw new DuplicateResourceException("Card", "cardNumber", createCardDTO.getCardNumber());
        }

        // Validate card status exists
        cardStatusRepository.findByStatusCode(createCardDTO.getCardStatus())
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", createCardDTO.getCardStatus()));

        Card card = Card.builder()
                .cardNumber(createCardDTO.getCardNumber())
                .expiryDate(createCardDTO.getExpiryDate())
                .cardStatus(createCardDTO.getCardStatus())
                .creditLimit(createCardDTO.getCreditLimit())
                .cashLimit(createCardDTO.getCashLimit())
                .availableCreditLimit(createCardDTO.getAvailableCreditLimit())
                .availableCashLimit(createCardDTO.getAvailableCashLimit())
                .build();

        cardRepository.insert(card);
        log.info("Card created successfully");

        return convertToDTO(cardRepository.findByCardNumber(card.getCardNumber()).get());
    }

    public CardDTO updateCard(String cardNumber, CreateCardDTO updateCardDTO) {
        log.info("Updating card");

        // Check if card exists
        Card existingCard = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", cardNumber));

        // Validate new card status exists
        cardStatusRepository.findByStatusCode(updateCardDTO.getCardStatus())
                .orElseThrow(() -> new ResourceNotFoundException("CardStatus", "statusCode", updateCardDTO.getCardStatus()));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .expiryDate(updateCardDTO.getExpiryDate())
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

    public void deleteCard(String cardNumber) {
        log.info("Deleting card");

        if (!cardRepository.existsByCardNumber(cardNumber)) {
            throw new ResourceNotFoundException("Card", "cardNumber", cardNumber);
        }

        cardRepository.deleteByCardNumber(cardNumber);
        log.info("Card deleted successfully");
    }

    public Long getCardCountByStatus(String cardStatus) {
        log.info("Getting card count for status: {}", cardStatus);
        return cardRepository.countByStatus(cardStatus);
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
