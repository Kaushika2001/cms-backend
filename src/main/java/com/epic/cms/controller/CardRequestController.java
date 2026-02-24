package com.epic.cms.controller;

import com.epic.cms.dto.ApproveRequestDTO;
import com.epic.cms.dto.CardRequestDTO;
import com.epic.cms.dto.CardRequestResponseDTO;
import com.epic.cms.dto.CreateCardRequestDTO;
import com.epic.cms.dto.EncryptedPayload;
import com.epic.cms.dto.PaginatedResponse;
import com.epic.cms.service.ICardRequestService;
import com.epic.cms.service.impl.CardRequestServiceImpl;
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
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Card Request Management", description = "APIs for managing card requests")
public class CardRequestController {

    private final CardRequestServiceImpl cardRequestService;

    @GetMapping
    @Operation(summary = "Get all requests", description = "Retrieve a list of all card requests with masked card numbers")
    public ResponseEntity<List<CardRequestResponseDTO>> getAllRequests() {
        log.info("GET /api/v1/requests - Get all requests");
        List<CardRequestResponseDTO> requests = cardRequestService.getAllRequestsMasked();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get request by ID", description = "Retrieve a card request by its ID with masked card number")
    public ResponseEntity<CardRequestResponseDTO> getRequestById(
            @Parameter(description = "Request ID") @PathVariable Integer requestId) {
        log.info("GET /api/v1/requests/{} - Get request by ID", requestId);
        CardRequestResponseDTO request = cardRequestService.getRequestByIdMasked(requestId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/card/{cardNumber}")
    @Operation(summary = "Get requests by card number", description = "Retrieve all requests for a specific card with masked card numbers")
    public ResponseEntity<List<CardRequestResponseDTO>> getRequestsByCardNumber(
            @Parameter(description = "Card number") @PathVariable String cardNumber) {
        log.info("GET /api/v1/requests/card/{} - Get requests by card number", cardNumber);
        List<CardRequestResponseDTO> requests = cardRequestService.getRequestsByCardNumberMasked(cardNumber);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/masked-card/{maskedCardId}")
    @Operation(summary = "Get requests by masked card ID", description = "Retrieve all requests for a specific card using its masked card ID")
    public ResponseEntity<List<CardRequestResponseDTO>> getRequestsByMaskedCardId(
            @Parameter(description = "Masked card ID (e.g., CRD-M-256AB400)") @PathVariable String maskedCardId) {
        log.info("GET /api/v1/requests/masked-card/{} - Get requests by masked card ID", maskedCardId);
        List<CardRequestResponseDTO> requests = cardRequestService.getRequestsByMaskedCardId(maskedCardId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get requests by status", description = "Retrieve all requests with a specific status (PEND, APPR, RJCT) with masked card numbers")
    public ResponseEntity<List<CardRequestResponseDTO>> getRequestsByStatus(
            @Parameter(description = "Request status code") @PathVariable String status) {
        log.info("GET /api/v1/requests/status/{} - Get requests by status", status);
        List<CardRequestResponseDTO> requests = cardRequestService.getRequestsByStatusMasked(status);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get requests by type", description = "Retrieve all requests of a specific type (ACTI, CDCL) with masked card numbers")
    public ResponseEntity<List<CardRequestResponseDTO>> getRequestsByType(
            @Parameter(description = "Request type code") @PathVariable String type) {
        log.info("GET /api/v1/requests/type/{} - Get requests by type", type);
        List<CardRequestResponseDTO> requests = cardRequestService.getRequestsByTypeMasked(type);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending requests", description = "Retrieve all pending requests with masked card numbers")
    public ResponseEntity<List<CardRequestResponseDTO>> getPendingRequests() {
        log.info("GET /api/v1/requests/pending - Get pending requests");
        List<CardRequestResponseDTO> requests = cardRequestService.getPendingRequestsMasked();
        return ResponseEntity.ok(requests);
    }

    @PostMapping
    @Operation(summary = "Create a new request", description = "Create a new card request with masked card number in response")
    public ResponseEntity<CardRequestResponseDTO> createRequest(
            @Parameter(description = "Request creation details") @Valid @RequestBody CreateCardRequestDTO createRequestDTO) {
        log.info("POST /api/v1/requests - Create new request");
        CardRequestResponseDTO createdRequest = cardRequestService.createRequestMasked(createRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PostMapping("/encrypted")
    @Operation(summary = "Create a new request (encrypted)", 
               description = "Create a new card request with encrypted payload. " +
                           "Encrypts CreateCardRequestDTO with transport key. " +
                           "Supports card number lookup in encrypted storage. Returns masked card number.")
    public ResponseEntity<CardRequestResponseDTO> createRequestEncrypted(
            @Parameter(description = "Encrypted request creation details") 
            @Valid @RequestBody EncryptedPayload encryptedPayload) {
        log.info("POST /api/v1/requests/encrypted - Create new request with encrypted payload");
        CardRequestResponseDTO createdRequest = cardRequestService.createCardRequestEncryptedMasked(encryptedPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }


    @PutMapping("/{requestId}")
    @Operation(summary = "Update request", description = "Update an existing card request (only pending requests) with masked card number in response")
    public ResponseEntity<CardRequestResponseDTO> updateRequest(
            @Parameter(description = "Request ID") @PathVariable Integer requestId,
            @Parameter(description = "Request update details") @Valid @RequestBody CreateCardRequestDTO updateRequestDTO) {
        log.info("PUT /api/v1/requests/{} - Update request", requestId);
        CardRequestResponseDTO updatedRequest = cardRequestService.updateRequestMasked(requestId, updateRequestDTO);
        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{requestId}/process")
    @Operation(summary = "Approve/Reject request", description = "Approve or reject a card request with masked card number in response")
    public ResponseEntity<CardRequestResponseDTO> processRequest(
            @Parameter(description = "Request ID") @PathVariable Integer requestId,
            @Parameter(description = "Approval/Rejection details") @Valid @RequestBody ApproveRequestDTO approveRequestDTO) {
        log.info("POST /api/v1/requests/{}/process - Process request", requestId);
        CardRequestResponseDTO updatedRequest = cardRequestService.approveOrRejectRequestMasked(requestId, approveRequestDTO);
        return ResponseEntity.ok(updatedRequest);
    }

    @DeleteMapping("/{requestId}")
    @Operation(summary = "Delete request", description = "Delete a card request (only pending/rejected requests)")
    public ResponseEntity<Void> deleteRequest(
            @Parameter(description = "Request ID") @PathVariable Integer requestId) {
        log.info("DELETE /api/v1/requests/{} - Delete request", requestId);
        cardRequestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get request count by status", description = "Get the count of requests with a specific status")
    public ResponseEntity<Long> getRequestCountByStatus(
            @Parameter(description = "Request status code") @RequestParam String status) {
        log.info("GET /api/v1/requests/count?status={} - Get request count by status", status);
        Long count = cardRequestService.getRequestCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    // ==================== Pagination Endpoints ====================

    @GetMapping("/paginated")
    @Operation(summary = "Get all requests with pagination", 
               description = "Retrieve a paginated list of all card requests with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardRequestResponseDTO>> getAllRequestsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/requests/paginated?page={}&size={} - Get all requests paginated", page, size);
        PaginatedResponse<CardRequestResponseDTO> response = cardRequestService.getAllRequestsPaginatedMasked(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/card/{cardNumber}/paginated")
    @Operation(summary = "Get requests by card number with pagination", 
               description = "Retrieve paginated requests for a specific card with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardRequestResponseDTO>> getRequestsByCardNumberPaginated(
            @Parameter(description = "Card number") @PathVariable String cardNumber,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/requests/card/{}/paginated?page={}&size={} - Get requests by card paginated", cardNumber, page, size);
        PaginatedResponse<CardRequestResponseDTO> response = cardRequestService.getRequestsByCardNumberPaginatedMasked(cardNumber, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}/paginated")
    @Operation(summary = "Get requests by status with pagination", 
               description = "Retrieve paginated requests with a specific status (PEND, APPR, RJCT) with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardRequestResponseDTO>> getRequestsByStatusPaginated(
            @Parameter(description = "Request status code") @PathVariable String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/requests/status/{}/paginated?page={}&size={} - Get requests by status paginated", status, page, size);
        PaginatedResponse<CardRequestResponseDTO> response = cardRequestService.getRequestsByStatusPaginatedMasked(status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}/paginated")
    @Operation(summary = "Get requests by type with pagination", 
               description = "Retrieve paginated requests of a specific type (ACTI, CDCL) with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardRequestResponseDTO>> getRequestsByTypePaginated(
            @Parameter(description = "Request type code") @PathVariable String type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/requests/type/{}/paginated?page={}&size={} - Get requests by type paginated", type, page, size);
        PaginatedResponse<CardRequestResponseDTO> response = cardRequestService.getRequestsByTypePaginatedMasked(type, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/paginated")
    @Operation(summary = "Get pending requests with pagination", 
               description = "Retrieve paginated list of pending requests with masked card numbers")
    public ResponseEntity<PaginatedResponse<CardRequestResponseDTO>> getPendingRequestsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/requests/pending/paginated?page={}&size={} - Get pending requests paginated", page, size);
        PaginatedResponse<CardRequestResponseDTO> response = cardRequestService.getPendingRequestsPaginatedMasked(page, size);
        return ResponseEntity.ok(response);
    }
}
