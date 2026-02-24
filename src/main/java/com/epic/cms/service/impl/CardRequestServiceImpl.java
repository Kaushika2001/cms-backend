package com.epic.cms.service.impl;

import com.epic.cms.config.EncryptionConfig;
import com.epic.cms.constants.StatusCodes;
import com.epic.cms.dto.ApproveRequestDTO;
import com.epic.cms.dto.CardRequestDTO;
import com.epic.cms.dto.CardRequestResponseDTO;
import com.epic.cms.dto.CreateCardRequestDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.epic.cms.dto.PaginatedResponse;
import com.epic.cms.exception.InvalidRequestException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.Card;
import com.epic.cms.model.CardRequest;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.repository.ICardRequestRepository;
import com.epic.cms.repository.ICardRequestTypeRepository;
import com.epic.cms.repository.IRequestStatusRepository;
import com.epic.cms.service.ICardRequestService;
import com.epic.cms.util.CardMaskingUtil;
import com.epic.cms.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardRequestServiceImpl implements ICardRequestService {

    private final ICardRequestRepository cardRequestRepository;
    private final ICardRepository cardRepository;
    private final ICardRequestTypeRepository cardRequestTypeRepository;
    private final IRequestStatusRepository requestStatusRepository;
    private final EncryptionConfig encryptionConfig;
    private final ObjectMapper objectMapper;

    @Override
    public List<CardRequestDTO> getAllRequests() {
        log.info("Fetching all card requests");
        return cardRequestRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getAllRequestsMasked() {
        log.info("Fetching all card requests (masked)");
        return cardRequestRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CardRequestDTO getRequestById(Integer requestId) {
        log.info("Fetching card request with id: {}", requestId);
        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));
        return convertToDTO(request);
    }

    public CardRequestResponseDTO getRequestByIdMasked(Integer requestId) {
        log.info("Fetching card request with id (masked): {}", requestId);
        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));
        return convertToResponseDTO(request);
    }

    @Override
    public List<CardRequestDTO> getRequestsByCardNumber(String cardNumber) {
        log.info("Fetching requests for card");
        return cardRequestRepository.findByCardNumber(cardNumber)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getRequestsByCardNumberMasked(String cardNumber) {
        log.info("Fetching requests for card (masked)");
        return cardRequestRepository.findByCardNumber(cardNumber)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getRequestsByMaskedCardId(String maskedCardId) {
        log.info("Fetching requests for masked card ID: {}", maskedCardId);
        
        // First, find the card by masked card ID
        Card card = cardRepository.findByMaskedCardId(maskedCardId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Card", 
                    "maskedCardId", 
                    maskedCardId
                ));
        
        // Then, find all requests for that card using the encrypted card number
        return cardRequestRepository.findByCardNumber(card.getCardNumber())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardRequestDTO> getRequestsByStatus(String statusCode) {
        log.info("Fetching requests with status: {}", statusCode);
        return cardRequestRepository.findByRequestStatusCode(statusCode)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getRequestsByStatusMasked(String statusCode) {
        log.info("Fetching requests with status (masked): {}", statusCode);
        return cardRequestRepository.findByRequestStatusCode(statusCode)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardRequestDTO> getRequestsByType(String requestType) {
        log.info("Fetching requests with type: {}", requestType);
        return cardRequestRepository.findByRequestReasonCode(requestType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getRequestsByTypeMasked(String requestType) {
        log.info("Fetching requests with type (masked): {}", requestType);
        return cardRequestRepository.findByRequestReasonCode(requestType)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardRequestDTO> getPendingRequests() {
        log.info("Fetching pending requests");
        return cardRequestRepository.findPendingRequests()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CardRequestResponseDTO> getPendingRequestsMasked() {
        log.info("Fetching pending requests (masked)");
        return cardRequestRepository.findPendingRequests()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CardRequestDTO createRequest(CreateCardRequestDTO createRequestDTO) {
        log.info("Creating new card request");

        // Find the card by either maskedCardId (preferred) or cardNumber (legacy)
        Card card;
        if (createRequestDTO.getMaskedCardId() != null && !createRequestDTO.getMaskedCardId().isBlank()) {
            log.info("Looking up card by maskedCardId: {}", createRequestDTO.getMaskedCardId());
            card = cardRepository.findByMaskedCardId(createRequestDTO.getMaskedCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardId", createRequestDTO.getMaskedCardId()));
        } else if (createRequestDTO.getCardNumber() != null && !createRequestDTO.getCardNumber().isBlank()) {
            log.info("Looking up card by cardNumber (legacy)");
            card = cardRepository.findByCardNumber(createRequestDTO.getCardNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", createRequestDTO.getCardNumber()));
        } else {
            throw new InvalidRequestException("Either maskedCardId or cardNumber must be provided");
        }

        // Validate request type exists
        cardRequestTypeRepository.findByCode(createRequestDTO.getRequestReasonCode())
                .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", createRequestDTO.getRequestReasonCode()));

        CardRequest request = CardRequest.builder()
                .cardNumber(card.getCardNumber()) // Use the actual card number from the database
                .requestReasonCode(createRequestDTO.getRequestReasonCode())
                .requestStatusCode(StatusCodes.REQUEST_PENDING)
                .remark(createRequestDTO.getRemark())
                .build();

        Integer requestId = cardRequestRepository.insert(request);
        log.info("Card request created successfully with id: {}", requestId);

        return convertToDTO(cardRequestRepository.findById(requestId).get());
    }

    public CardRequestResponseDTO createRequestMasked(CreateCardRequestDTO createRequestDTO) {
        log.info("Creating new card request (masked response)");

        // Find the card by either maskedCardId (preferred) or cardNumber (legacy)
        Card card;
        if (createRequestDTO.getMaskedCardId() != null && !createRequestDTO.getMaskedCardId().isBlank()) {
            log.info("Looking up card by maskedCardId: {}", createRequestDTO.getMaskedCardId());
            card = cardRepository.findByMaskedCardId(createRequestDTO.getMaskedCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "maskedCardId", createRequestDTO.getMaskedCardId()));
        } else if (createRequestDTO.getCardNumber() != null && !createRequestDTO.getCardNumber().isBlank()) {
            log.info("Looking up card by cardNumber (legacy)");
            card = cardRepository.findByCardNumber(createRequestDTO.getCardNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", createRequestDTO.getCardNumber()));
        } else {
            throw new InvalidRequestException("Either maskedCardId or cardNumber must be provided");
        }

        // Validate request type exists
        cardRequestTypeRepository.findByCode(createRequestDTO.getRequestReasonCode())
                .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", createRequestDTO.getRequestReasonCode()));

        CardRequest request = CardRequest.builder()
                .cardNumber(card.getCardNumber()) // Use the actual card number from the database
                .requestReasonCode(createRequestDTO.getRequestReasonCode())
                .requestStatusCode(StatusCodes.REQUEST_PENDING)
                .remark(createRequestDTO.getRemark())
                .build();

        Integer requestId = cardRequestRepository.insert(request);
        log.info("Card request created successfully with id: {}", requestId);

        return convertToResponseDTO(cardRequestRepository.findById(requestId).get());
    }

    @Override
    public CardRequestDTO createCardRequestEncrypted(EncryptedPayload encryptedPayload) {
        log.info("Creating new card request with encrypted payload");

        try {
            // Step 1: Decrypt the payload with transport key
            String transportKey = encryptionConfig.getTransportKey();
            String decryptedJson = EncryptionUtil.decrypt(
                    encryptedPayload.getEncryptedData(),
                    transportKey
            );
            log.debug("Decrypted card request payload");

            // Step 2: Parse the decrypted JSON to CreateCardRequestDTO
            CreateCardRequestDTO createRequestDTO = objectMapper.readValue(decryptedJson, CreateCardRequestDTO.class);
            log.debug("Parsed CreateCardRequestDTO from decrypted payload");

            // Step 3: Find the card by card number (need to decrypt stored card numbers)
            Card card;
            if (createRequestDTO.getCardNumber() != null && !createRequestDTO.getCardNumber().isBlank()) {
                log.info("Looking up card by decrypting stored card numbers");
                card = findCardByUnencryptedNumber(createRequestDTO.getCardNumber());
            } else {
                throw new InvalidRequestException("Card number must be provided in encrypted request");
            }

            // Step 4: Validate request type exists
            cardRequestTypeRepository.findByCode(createRequestDTO.getRequestReasonCode())
                    .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", createRequestDTO.getRequestReasonCode()));

            // Step 5: Create the card request
            CardRequest request = CardRequest.builder()
                    .cardNumber(card.getCardNumber()) // Use the encrypted card number from the database
                    .requestReasonCode(createRequestDTO.getRequestReasonCode())
                    .requestStatusCode(StatusCodes.REQUEST_PENDING)
                    .remark(createRequestDTO.getRemark())
                    .build();

            Integer requestId = cardRequestRepository.insert(request);
            log.info("Card request created successfully with id: {}", requestId);

            return convertToDTO(cardRequestRepository.findById(requestId).get());

        } catch (Exception e) {
            log.error("Error creating encrypted card request: {}", e.getMessage());
            throw new InvalidRequestException("Failed to create encrypted card request: " + e.getMessage());
        }
    }

    public CardRequestResponseDTO createCardRequestEncryptedMasked(EncryptedPayload encryptedPayload) {
        log.info("Creating new card request with encrypted payload (masked response)");

        try {
            // Step 1: Decrypt the payload with transport key
            String transportKey = encryptionConfig.getTransportKey();
            String decryptedJson = EncryptionUtil.decrypt(
                    encryptedPayload.getEncryptedData(),
                    transportKey
            );
            log.debug("Decrypted card request payload");

            // Step 2: Parse the decrypted JSON to CreateCardRequestDTO
            CreateCardRequestDTO createRequestDTO = objectMapper.readValue(decryptedJson, CreateCardRequestDTO.class);
            log.debug("Parsed CreateCardRequestDTO from decrypted payload");

            // Step 3: Find the card by card number (need to decrypt stored card numbers)
            Card card;
            if (createRequestDTO.getCardNumber() != null && !createRequestDTO.getCardNumber().isBlank()) {
                log.info("Looking up card by decrypting stored card numbers");
                card = findCardByUnencryptedNumber(createRequestDTO.getCardNumber());
            } else {
                throw new InvalidRequestException("Card number must be provided in encrypted request");
            }

            // Step 4: Validate request type exists
            cardRequestTypeRepository.findByCode(createRequestDTO.getRequestReasonCode())
                    .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", createRequestDTO.getRequestReasonCode()));

            // Step 5: Create the card request
            CardRequest request = CardRequest.builder()
                    .cardNumber(card.getCardNumber()) // Use the encrypted card number from the database
                    .requestReasonCode(createRequestDTO.getRequestReasonCode())
                    .requestStatusCode(StatusCodes.REQUEST_PENDING)
                    .remark(createRequestDTO.getRemark())
                    .build();

            Integer requestId = cardRequestRepository.insert(request);
            log.info("Card request created successfully with id: {}", requestId);

            return convertToResponseDTO(cardRequestRepository.findById(requestId).get());

        } catch (Exception e) {
            log.error("Error creating encrypted card request: {}", e.getMessage());
            throw new InvalidRequestException("Failed to create encrypted card request: " + e.getMessage());
        }
    }

    /**
     * Helper method to find a card by decrypting all stored card numbers.
     * This is needed when cards are stored with encrypted card numbers.
     */
    private Card findCardByUnencryptedNumber(String unencryptedCardNumber) {
        log.info("Searching for card by decrypting stored card numbers");

        // Get all cards
        List<Card> allCards = cardRepository.findAll();
        log.debug("Total cards to check: {}", allCards.size());

        String storageKey = encryptionConfig.getStorageKey();

        // Find the card whose decrypted card number matches
        for (Card card : allCards) {
            try {
                // Check if card number is encrypted (contains ".")
                if (card.getCardNumber().contains(".")) {
                    String decryptedCardNumber = EncryptionUtil.decrypt(card.getCardNumber(), storageKey);
                    if (unencryptedCardNumber.equals(decryptedCardNumber)) {
                        log.info("Found matching card by decryption");
                        return card;
                    }
                } else {
                    // Card number might not be encrypted (legacy data), try direct comparison
                    if (unencryptedCardNumber.equals(card.getCardNumber())) {
                        log.info("Found matching card (unencrypted legacy data)");
                        return card;
                    }
                }
            } catch (Exception e) {
                log.debug("Error decrypting card number, might be legacy format: {}", e.getMessage());
            }
        }

        throw new ResourceNotFoundException("Card", "cardNumber", "****");
    }

    @Override
    public CardRequestDTO updateRequest(Integer requestId, CreateCardRequestDTO updateRequestDTO) {
        log.info("Updating card request: {}", requestId);

        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));

        // Only allow updates to pending requests
        if (!StatusCodes.REQUEST_PENDING.equals(request.getRequestStatusCode())) {
            throw new InvalidRequestException("Cannot update request with status: " + request.getRequestStatusCode());
        }

        // Validate request type exists
        cardRequestTypeRepository.findByCode(updateRequestDTO.getRequestReasonCode())
                .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", updateRequestDTO.getRequestReasonCode()));

        // Note: We don't update the card number for existing requests
        // Only the request reason and remark can be updated
        request.setRequestReasonCode(updateRequestDTO.getRequestReasonCode());
        request.setRemark(updateRequestDTO.getRemark());

        cardRequestRepository.update(request);
        log.info("Card request updated successfully: {}", requestId);

        return convertToDTO(cardRequestRepository.findById(requestId).get());
    }

    public CardRequestResponseDTO updateRequestMasked(Integer requestId, CreateCardRequestDTO updateRequestDTO) {
        log.info("Updating card request: {} (masked response)", requestId);

        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));

        // Only allow updates to pending requests
        if (!StatusCodes.REQUEST_PENDING.equals(request.getRequestStatusCode())) {
            throw new InvalidRequestException("Cannot update request with status: " + request.getRequestStatusCode());
        }

        // Validate request type exists
        cardRequestTypeRepository.findByCode(updateRequestDTO.getRequestReasonCode())
                .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", updateRequestDTO.getRequestReasonCode()));

        // Note: We don't update the card number for existing requests
        // Only the request reason and remark can be updated
        request.setRequestReasonCode(updateRequestDTO.getRequestReasonCode());
        request.setRemark(updateRequestDTO.getRemark());

        cardRequestRepository.update(request);
        log.info("Card request updated successfully: {}", requestId);

        return convertToResponseDTO(cardRequestRepository.findById(requestId).get());
    }

    @Override
    public CardRequestDTO approveOrRejectRequest(Integer requestId, ApproveRequestDTO approveRequestDTO) {
        log.info("Processing approval for request id: {}", requestId);

        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));

        // Only allow approval/rejection of pending requests
        if (!StatusCodes.REQUEST_PENDING.equals(request.getRequestStatusCode())) {
            throw new InvalidRequestException("Cannot approve/reject request with status: " + request.getRequestStatusCode());
        }

        // Validate new status exists
        requestStatusRepository.findByStatusCode(approveRequestDTO.getRequestStatusCode())
                .orElseThrow(() -> new ResourceNotFoundException("RequestStatus", "statusCode", approveRequestDTO.getRequestStatusCode()));

        cardRequestRepository.updateStatus(requestId, approveRequestDTO.getRequestStatusCode(), approveRequestDTO.getRemark());
        
        if (StatusCodes.REQUEST_APPROVED.equals(approveRequestDTO.getRequestStatusCode())) {
            log.info("Request approved: {}", requestId);
            
            // Update card status based on request type
            updateCardStatusForApprovedRequest(request);
        } else {
            log.info("Request rejected: {}", requestId);
        }

        return convertToDTO(cardRequestRepository.findById(requestId).get());
    }

    public CardRequestResponseDTO approveOrRejectRequestMasked(Integer requestId, ApproveRequestDTO approveRequestDTO) {
        log.info("Processing approval for request id: {} (masked response)", requestId);

        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));

        // Only allow approval/rejection of pending requests
        if (!StatusCodes.REQUEST_PENDING.equals(request.getRequestStatusCode())) {
            throw new InvalidRequestException("Cannot approve/reject request with status: " + request.getRequestStatusCode());
        }

        // Validate new status exists
        requestStatusRepository.findByStatusCode(approveRequestDTO.getRequestStatusCode())
                .orElseThrow(() -> new ResourceNotFoundException("RequestStatus", "statusCode", approveRequestDTO.getRequestStatusCode()));

        cardRequestRepository.updateStatus(requestId, approveRequestDTO.getRequestStatusCode(), approveRequestDTO.getRemark());
        
        if (StatusCodes.REQUEST_APPROVED.equals(approveRequestDTO.getRequestStatusCode())) {
            log.info("Request approved: {}", requestId);
            
            // Update card status based on request type
            updateCardStatusForApprovedRequest(request);
        } else {
            log.info("Request rejected: {}", requestId);
        }

        return convertToResponseDTO(cardRequestRepository.findById(requestId).get());
    }
    
    private void updateCardStatusForApprovedRequest(CardRequest request) {
        String newCardStatus = null;
        
        // Determine new card status based on request type
        switch (request.getRequestReasonCode()) {
            case "ACTI": // Card Activation Request
                newCardStatus = StatusCodes.CARD_ACTIVE;
                log.info("Activating card due to approved activation request");
                break;
                
            case "CDCL": // Card Closure Request
                newCardStatus = StatusCodes.CARD_DEACTIVATED;
                log.info("Deactivating card due to approved closure request");
                break;
                
            default:
                log.warn("Unknown request type: {}. Card status not updated.", request.getRequestReasonCode());
                return;
        }
        
        // Update the card status
        if (newCardStatus != null) {
            int rowsUpdated = cardRepository.updateCardStatus(request.getCardNumber(), newCardStatus);
            if (rowsUpdated > 0) {
                log.info("Card status updated successfully");
            } else {
                log.error("Failed to update card status");
            }
        }
    }

    @Override
    public void deleteRequest(Integer requestId) {
        log.info("Deleting card request: {}", requestId);

        CardRequest request = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CardRequest", "requestId", requestId));

        // Only allow deletion of pending or rejected requests
        if (StatusCodes.REQUEST_APPROVED.equals(request.getRequestStatusCode())) {
            throw new InvalidRequestException("Cannot delete approved request");
        }

        cardRequestRepository.deleteById(requestId);
        log.info("Card request deleted successfully: {}", requestId);
    }

    @Override
    public Long getRequestCountByStatus(String statusCode) {
        log.info("Getting request count for status: {}", statusCode);
        return cardRequestRepository.countByStatus(statusCode);
    }

    private CardRequestDTO convertToDTO(CardRequest request) {
        CardRequestDTO dto = CardRequestDTO.builder()
                .requestId(request.getRequestId())
                .cardNumber(request.getCardNumber())
                .requestReasonCode(request.getRequestReasonCode())
                .requestStatusCode(request.getRequestStatusCode())
                .remark(request.getRemark())
                .createdTime(request.getCreatedTime())
                .build();

        // Fetch request type description
        cardRequestTypeRepository.findByCode(request.getRequestReasonCode())
                .ifPresent(type -> dto.setRequestReasonDescription(type.getDescription()));

        // Fetch status description
        if (request.getRequestStatusCode() != null) {
            requestStatusRepository.findByStatusCode(request.getRequestStatusCode())
                    .ifPresent(status -> dto.setRequestStatusDescription(status.getDescription()));
        }

        return dto;
    }

    private CardRequestResponseDTO convertToResponseDTO(CardRequest request) {
        // Get the encrypted card number from the request
        String encryptedCardNumber = request.getCardNumber();
        String decryptedCardNumber = encryptedCardNumber;
        
        log.debug("Converting to ResponseDTO - encrypted card number: {}", encryptedCardNumber);
        
        // Decrypt if the card number is in encrypted format
        if (encryptedCardNumber != null && encryptedCardNumber.contains(".")) {
            try {
                String storageKey = encryptionConfig.getStorageKey();
                decryptedCardNumber = EncryptionUtil.decrypt(encryptedCardNumber, storageKey);
                log.debug("Successfully decrypted card number");
            } catch (Exception e) {
                log.error("Error decrypting card number for response: {}", e.getMessage(), e);
                log.error("Encrypted value that failed to decrypt: {}", encryptedCardNumber);
                // If decryption fails, try to mask the encrypted value directly (fallback)
                decryptedCardNumber = encryptedCardNumber;
            }
        } else {
            log.debug("Card number is not encrypted (no dot separator found)");
        }
        
        String maskedCardId;
        String maskedCardNumber;
        
        try {
            maskedCardId = CardMaskingUtil.generateMaskedCardId(decryptedCardNumber);
            maskedCardNumber = CardMaskingUtil.mask(decryptedCardNumber);
            log.debug("Generated maskedCardId: {}, maskedCardNumber: {}", maskedCardId, maskedCardNumber);
        } catch (Exception e) {
            log.error("Error generating masked values: {}", e.getMessage(), e);
            // Fallback values
            maskedCardId = "CRD-M-ERROR";
            maskedCardNumber = "****ERROR****";
        }
        
        CardRequestResponseDTO dto = CardRequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .maskedCardId(maskedCardId)
                .cardNumber(maskedCardNumber)
                .requestReasonCode(request.getRequestReasonCode())
                .requestStatusCode(request.getRequestStatusCode())
                .remark(request.getRemark())
                .createdTime(request.getCreatedTime())
                .build();

        // Fetch request type description
        cardRequestTypeRepository.findByCode(request.getRequestReasonCode())
                .ifPresent(type -> dto.setRequestReasonDescription(type.getDescription()));

        // Fetch status description
        if (request.getRequestStatusCode() != null) {
            requestStatusRepository.findByStatusCode(request.getRequestStatusCode())
                    .ifPresent(status -> dto.setRequestStatusDescription(status.getDescription()));
        }

        return dto;
    }

    @Override
    public PaginatedResponse<CardRequestDTO> getAllRequestsPaginated(int page, int size) {
        log.info("Fetching all card requests with pagination - page: {}, size: {}", page, size);
        
        List<CardRequestDTO> content = cardRequestRepository.findAll(page, size)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.count();
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<CardRequestDTO> getRequestsByCardNumberPaginated(String cardNumber, int page, int size) {
        log.info("Fetching requests for card (paginated) - page: {}, size: {}", page, size);
        
        List<CardRequestDTO> content = cardRequestRepository.findByCardNumber(cardNumber, page, size)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByCardNumber(cardNumber);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<CardRequestDTO> getRequestsByStatusPaginated(String statusCode, int page, int size) {
        log.info("Fetching requests with status {} (paginated) - page: {}, size: {}", statusCode, page, size);
        
        List<CardRequestDTO> content = cardRequestRepository.findByRequestStatusCode(statusCode, page, size)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByStatus(statusCode);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<CardRequestDTO> getRequestsByTypePaginated(String requestType, int page, int size) {
        log.info("Fetching requests of type {} (paginated) - page: {}, size: {}", requestType, page, size);
        
        List<CardRequestDTO> content = cardRequestRepository.findByRequestReasonCode(requestType, page, size)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByReasonCode(requestType);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    @Override
    public PaginatedResponse<CardRequestDTO> getPendingRequestsPaginated(int page, int size) {
        log.info("Fetching pending requests (paginated) - page: {}, size: {}", page, size);
        
        List<CardRequestDTO> content = cardRequestRepository.findPendingRequests(page, size)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countPendingRequests();
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    // ==================== Masked Paginated Methods ====================

    /**
     * Get all card requests with pagination and masked card numbers
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated response containing card requests with masked card numbers
     */
    public PaginatedResponse<CardRequestResponseDTO> getAllRequestsPaginatedMasked(int page, int size) {
        log.info("Fetching all card requests with pagination (masked) - page: {}, size: {}", page, size);
        
        List<CardRequestResponseDTO> content = cardRequestRepository.findAll(page, size)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.count();
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    /**
     * Get requests by card number with pagination and masked card numbers
     * @param cardNumber The card number
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated response containing card requests with masked card numbers
     */
    public PaginatedResponse<CardRequestResponseDTO> getRequestsByCardNumberPaginatedMasked(String cardNumber, int page, int size) {
        log.info("Fetching requests for card (paginated, masked) - page: {}, size: {}", page, size);
        
        List<CardRequestResponseDTO> content = cardRequestRepository.findByCardNumber(cardNumber, page, size)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByCardNumber(cardNumber);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    /**
     * Get requests by status with pagination and masked card numbers
     * @param statusCode The status code
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated response containing card requests with masked card numbers
     */
    public PaginatedResponse<CardRequestResponseDTO> getRequestsByStatusPaginatedMasked(String statusCode, int page, int size) {
        log.info("Fetching requests with status {} (paginated, masked) - page: {}, size: {}", statusCode, page, size);
        
        List<CardRequestResponseDTO> content = cardRequestRepository.findByRequestStatusCode(statusCode, page, size)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByStatus(statusCode);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    /**
     * Get requests by type with pagination and masked card numbers
     * @param requestType The request type
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated response containing card requests with masked card numbers
     */
    public PaginatedResponse<CardRequestResponseDTO> getRequestsByTypePaginatedMasked(String requestType, int page, int size) {
        log.info("Fetching requests of type {} (paginated, masked) - page: {}, size: {}", requestType, page, size);
        
        List<CardRequestResponseDTO> content = cardRequestRepository.findByRequestReasonCode(requestType, page, size)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countByReasonCode(requestType);
        return new PaginatedResponse<>(content, page, size, totalElements);
    }

    /**
     * Get pending requests with pagination and masked card numbers
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return Paginated response containing pending card requests with masked card numbers
     */
    public PaginatedResponse<CardRequestResponseDTO> getPendingRequestsPaginatedMasked(int page, int size) {
        log.info("Fetching pending requests (paginated, masked) - page: {}, size: {}", page, size);
        
        List<CardRequestResponseDTO> content = cardRequestRepository.findPendingRequests(page, size)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        long totalElements = cardRequestRepository.countPendingRequests();
        return new PaginatedResponse<>(content, page, size, totalElements);
    }
}
