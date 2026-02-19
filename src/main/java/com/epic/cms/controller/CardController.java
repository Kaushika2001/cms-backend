package com.epic.cms.controller;

import com.epic.cms.dto.CardDTO;
import com.epic.cms.dto.CardResponseDTO;
import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.service.CardService;
import com.epic.cms.service.impl.CardServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Card Management", description = "APIs for managing cards")
public class CardController {

    private final CardServiceImpl cardService;

    @GetMapping
    @Operation(summary = "Get all cards", description = "Retrieve a list of all cards with masked card numbers")
    public ResponseEntity<List<CardResponseDTO>> getAllCards() {
        log.info("GET /api/v1/cards - Get all cards");
        List<CardResponseDTO> cards = cardService.getAllCardsMasked();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardNumber}")
    @Operation(summary = "Get card by card number", description = "Retrieve a card by its card number with masked card number")
    public ResponseEntity<CardResponseDTO> getCardByCardNumber(
            @Parameter(description = "Card number") @PathVariable String cardNumber) {
        log.info("GET /api/v1/cards/{} - Get card by card number", cardNumber);
        CardResponseDTO card = cardService.getCardByCardNumberMasked(cardNumber);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get cards by status", description = "Retrieve all cards with a specific status (IACT, CACT, DACT) with masked card numbers")
    public ResponseEntity<List<CardResponseDTO>> getCardsByStatus(
            @Parameter(description = "Card status code") @PathVariable String status) {
        log.info("GET /api/v1/cards/status/{} - Get cards by status", status);
        List<CardResponseDTO> cards = cardService.getCardsByStatusMasked(status);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired cards", description = "Retrieve all expired cards with masked card numbers")
    public ResponseEntity<List<CardResponseDTO>> getExpiredCards() {
        log.info("GET /api/v1/cards/expired - Get expired cards");
        List<CardResponseDTO> cards = cardService.getExpiredCardsMasked();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get cards expiring soon", description = "Retrieve cards expiring within specified days with masked card numbers")
    public ResponseEntity<List<CardResponseDTO>> getCardsExpiringSoon(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/v1/cards/expiring?days={} - Get cards expiring in {} days", days, days);
        List<CardResponseDTO> cards = cardService.getCardsExpiringInDaysMasked(days);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @Operation(summary = "Create a new card", description = "Create a new card in the system")
    public ResponseEntity<CardDTO> createCard(
            @Parameter(description = "Card creation details") @Valid @RequestBody CreateCardDTO createCardDTO) {
        log.info("POST /api/v1/cards - Create new card");
        CardDTO createdCard = cardService.createCard(createCardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @PutMapping("/{cardNumber}")
    @Operation(summary = "Update card", description = "Update an existing card")
    public ResponseEntity<CardDTO> updateCard(
            @Parameter(description = "Card number") @PathVariable String cardNumber,
            @Parameter(description = "Card update details") @Valid @RequestBody CreateCardDTO updateCardDTO) {
        log.info("PUT /api/v1/cards/{} - Update card", cardNumber);
        CardDTO updatedCard = cardService.updateCard(cardNumber, updateCardDTO);
        return ResponseEntity.ok(updatedCard);
    }

    @PatchMapping("/{cardNumber}/status")
    @Operation(summary = "Update card status", description = "Update the status of a card (IACT, CACT, DACT)")
    public ResponseEntity<CardDTO> updateCardStatus(
            @Parameter(description = "Card number") @PathVariable String cardNumber,
            @Parameter(description = "New status code") @RequestParam String status) {
        log.info("PATCH /api/v1/cards/{}/status - Update card status to {}", cardNumber, status);
        CardDTO updatedCard = cardService.updateCardStatus(cardNumber, status);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardNumber}")
    @Operation(summary = "Delete card", description = "Delete a card from the system")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Card number") @PathVariable String cardNumber) {
        log.info("DELETE /api/v1/cards/{} - Delete card", cardNumber);
        cardService.deleteCard(cardNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get card count by status", description = "Get the count of cards with a specific status")
    public ResponseEntity<Long> getCardCountByStatus(
            @Parameter(description = "Card status code") @RequestParam String status) {
        log.info("GET /api/v1/cards/count?status={} - Get card count by status", status);
        Long count = cardService.getCardCountByStatus(status);
        return ResponseEntity.ok(count);
    }
}
