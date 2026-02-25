package com.epic.cms.controller;

import com.epic.cms.config.EncryptionConfig;
import com.epic.cms.dto.CardDTO;
import com.epic.cms.dto.CardResponseDTO;
import com.epic.cms.dto.CreateCardDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.epic.cms.dto.MaskedCardIdDTO;
import com.epic.cms.dto.PaginatedResponse;
import com.epic.cms.dto.UpdateCardDTO;
import com.epic.cms.dto.UpdateCardStatusDTO;
import com.epic.cms.service.CardService;
import com.epic.cms.service.impl.CardServiceImpl;
import com.epic.cms.util.CardMaskingUtil;
import com.epic.cms.util.EncryptionUtil;
import com.epic.cms.util.ExpiryDateUtil;
import com.epic.cms.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Card Management", description = "APIs for managing cards")
public class CardController {

    private final CardServiceImpl cardService;
    private final EncryptionConfig encryptionConfig;
    private final ObjectMapper objectMapper;
    private final Validator validator;

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

    @GetMapping("/lookup")
    @Operation(summary = "Get maskedCardId for card number", 
               description = "Retrieve maskedCardId and card details for a given card number (masked or unmasked). " +
                             "Example: /lookup?cardNumber=589925******0233 or /lookup?cardNumber=5899250123450233")
    public ResponseEntity<CardResponseDTO> getMaskedCardIdForCardNumber(
            @Parameter(description = "Card number (masked like 589925******0233 or unmasked)") 
            @RequestParam String cardNumber) {
        log.info("GET /api/v1/cards/lookup?cardNumber={} - Get maskedCardId for card number", cardNumber);
        CardResponseDTO card = cardService.getMaskedCardIdForCardNumber(cardNumber);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/masked-id")
    @Operation(summary = "Get only maskedCardId for card number", 
               description = "Retrieve only maskedCardId for a given card number (masked or unmasked). " +
                             "Returns a lightweight response with just the maskedCardId. " +
                             "Example: /masked-id?cardNumber=589925******0233")
    public ResponseEntity<MaskedCardIdDTO> getMaskedCardIdOnly(
            @Parameter(description = "Card number (masked like 589925******0233 or unmasked)") 
            @RequestParam String cardNumber) {
        log.info("GET /api/v1/cards/masked-id?cardNumber={} - Get maskedCardId only", cardNumber);
        MaskedCardIdDTO maskedCardId = cardService.getMaskedCardIdOnly(cardNumber);
        return ResponseEntity.ok(maskedCardId);
    }

    @PostMapping
    @Operation(summary = "Create a new card", description = "Create a new card in the system (plain text - for internal use)")
    public ResponseEntity<CardDTO> createCard(
            @Parameter(description = "Card creation details") @Valid @RequestBody CreateCardDTO createCardDTO) {
        log.info("POST /api/v1/cards - Create new card");
        CardDTO createdCard = cardService.createCard(createCardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @PostMapping("/encrypted")
    @Operation(summary = "Create a new card (encrypted)", 
               description = "Create a new card using encrypted payload from frontend. " +
                             "The payload is encrypted with AES-256-GCM using the transport key.")
    public ResponseEntity<CardDTO> createCardEncrypted(
            @Parameter(description = "Encrypted card creation payload") 
            @Valid @RequestBody EncryptedPayload encryptedPayload) {
        try {
            LoggerUtil.info(CardController.class, "POST /api/v1/cards/encrypted - Received encrypted card creation request");
            
            // Step 1: Decrypt the payload using transport key
            String transportKey = encryptionConfig.getTransportKey();
            String decryptedJson = EncryptionUtil.decrypt(
                encryptedPayload.getEncryptedData(), 
                transportKey
            );
            
            LoggerUtil.debug(CardController.class, "Successfully decrypted payload");
            LoggerUtil.debug(CardController.class, "Decrypted JSON: {}", decryptedJson);
            
            // Step 2: Parse JSON to CreateCardDTO
            CreateCardDTO cardDTO = objectMapper.readValue(decryptedJson, CreateCardDTO.class);
            
            LoggerUtil.debug(CardController.class, "Successfully parsed CreateCardDTO from decrypted data");
            LoggerUtil.debug(CardController.class, "Card number from frontend: {}", CardMaskingUtil.mask(cardDTO.getCardNumber()));
            
            // Step 3: Validate the decrypted DTO
            Set<ConstraintViolation<CreateCardDTO>> violations = validator.validate(cardDTO);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
                LoggerUtil.error(CardController.class, "Validation failed for decrypted card data: {}", errors);
                LoggerUtil.error(CardController.class, "Decrypted card number: {}", CardMaskingUtil.mask(cardDTO.getCardNumber()));
                throw new IllegalArgumentException("Invalid card data: " + errors);
            }
            
            // Step 3.5: Validate expiry date is in the future
            if (!ExpiryDateUtil.isFutureDate(cardDTO.getExpiryDate())) {
                LoggerUtil.error(CardController.class, "Expiry date validation failed: {} is not in the future", cardDTO.getExpiryDate());
                throw new IllegalArgumentException("Expiry date must be in the future");
            }
            
            // Step 4: Create card (service will encrypt card number with storage key)
            CardDTO createdCard = cardService.createCard(cardDTO);
            
            LoggerUtil.info(CardController.class, "Card created successfully with card number: {}", 
                CardMaskingUtil.mask(createdCard.getCardNumber()));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
            
        } catch (IllegalArgumentException e) {
            LoggerUtil.error(CardController.class, "Invalid encrypted payload or card data: {}", e.getMessage());
            throw e; // Rethrow to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            LoggerUtil.error(CardController.class, "Error creating card from encrypted payload", e);
            throw new RuntimeException("Failed to create card: " + e.getMessage());
        }
    }

    @PutMapping("/{cardNumber}")
    @Operation(summary = "Update card", 
               description = "Update an existing card. Card number can be masked (e.g., 558899******3333) or encrypted. " +
                             "The card number in the path is used to identify the card, not the body.")
    public ResponseEntity<CardResponseDTO> updateCard(
            @Parameter(description = "Card number (masked like 558899******3333 or encrypted)") @PathVariable String cardNumber,
            @Parameter(description = "Card update details") @Valid @RequestBody UpdateCardDTO updateCardDTO) {
        log.info("PUT /api/v1/cards/{} - Update card", cardNumber);
        CardResponseDTO updatedCard = cardService.updateCardMasked(cardNumber, updateCardDTO);
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

    @PostMapping("/activate/encrypted")
    @Operation(summary = "Update card status (encrypted)", 
               description = "Update card status using encrypted payload. " +
                             "The payload contains the card number (can be masked like 453201******0366) and new status. " +
                             "Backend will decrypt, lookup, and update the card status.")
    public ResponseEntity<CardResponseDTO> updateCardStatusEncrypted(
            @Parameter(description = "Encrypted status update payload") 
            @Valid @RequestBody EncryptedPayload encryptedPayload) {
        try {
            LoggerUtil.info(CardController.class, "POST /api/v1/cards/activate/encrypted - Received encrypted status update request");
            
            // Step 1: Decrypt the payload using transport key
            String transportKey = encryptionConfig.getTransportKey();
            String decryptedJson = EncryptionUtil.decrypt(
                encryptedPayload.getEncryptedData(), 
                transportKey
            );
            
            LoggerUtil.debug(CardController.class, "Successfully decrypted status update payload");
            LoggerUtil.debug(CardController.class, "Decrypted JSON: {}", decryptedJson);
            
            // Step 2: Parse JSON to UpdateCardStatusDTO
            UpdateCardStatusDTO statusDTO = objectMapper.readValue(decryptedJson, UpdateCardStatusDTO.class);
            
            LoggerUtil.debug(CardController.class, "Successfully parsed UpdateCardStatusDTO from decrypted data");
            LoggerUtil.debug(CardController.class, "Card number from frontend: {}", CardMaskingUtil.mask(statusDTO.getCardNumber()));
            LoggerUtil.debug(CardController.class, "New status: {}", statusDTO.getNewStatus());
            
            // Step 3: Validate the decrypted DTO
            Set<ConstraintViolation<UpdateCardStatusDTO>> violations = validator.validate(statusDTO);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
                LoggerUtil.error(CardController.class, "Validation failed for status update: {}", errors);
                throw new IllegalArgumentException("Invalid status update data: " + errors);
            }
            
            // Step 4: Update card status (service handles masked/encrypted card number lookup)
            CardResponseDTO updatedCard = cardService.updateCardStatusEncrypted(
                statusDTO.getCardNumber(), 
                statusDTO.getNewStatus()
            );
            
            LoggerUtil.info(CardController.class, "Card status updated successfully to: {}", statusDTO.getNewStatus());
            
            return ResponseEntity.ok(updatedCard);
            
        } catch (IllegalArgumentException e) {
            LoggerUtil.error(CardController.class, "Invalid encrypted payload or status data: {}", e.getMessage());
            throw e; // Rethrow to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            LoggerUtil.error(CardController.class, "Error updating card status from encrypted payload", e);
            throw new RuntimeException("Failed to update card status: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @Operation(summary = "Get card count by status", description = "Get the count of cards with a specific status")
    public ResponseEntity<Long> getCardCountByStatus(
            @Parameter(description = "Card status code") @RequestParam String status) {
        log.info("GET /api/v1/cards/count?status={} - Get card count by status", status);
        Long count = cardService.getCardCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    // ==================== Pagination Endpoints ====================

    @GetMapping("/paginated")
    @Operation(summary = "Get all cards with pagination", 
               description = "Retrieve a paginated list of all cards with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getAllCardsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/cards/paginated?page={}&size={} - Get all cards paginated", page, size);
        PaginatedResponse<CardResponseDTO> response = cardService.getAllCardsPaginated(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}/paginated")
    @Operation(summary = "Get cards by status with pagination", 
               description = "Retrieve paginated cards with a specific status (IACT, CACT, DACT)")
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getCardsByStatusPaginated(
            @Parameter(description = "Card status code") @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/cards/status/{}/paginated?page={}&size={} - Get cards by status paginated", status, page, size);
        PaginatedResponse<CardResponseDTO> response = cardService.getCardsByStatusPaginated(status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired/paginated")
    @Operation(summary = "Get expired cards with pagination", 
               description = "Retrieve paginated list of expired cards")
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getExpiredCardsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/cards/expired/paginated?page={}&size={} - Get expired cards paginated", page, size);
        PaginatedResponse<CardResponseDTO> response = cardService.getExpiredCardsPaginated(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expiring/paginated")
    @Operation(summary = "Get cards expiring soon with pagination", 
               description = "Retrieve paginated cards expiring within specified days")
    public ResponseEntity<PaginatedResponse<CardResponseDTO>> getCardsExpiringSoonPaginated(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/cards/expiring/paginated?days={}&page={}&size={} - Get cards expiring paginated", days, page, size);
        PaginatedResponse<CardResponseDTO> response = cardService.getExpiringCardsPaginated(days, page, size);
        return ResponseEntity.ok(response);
    }
}
