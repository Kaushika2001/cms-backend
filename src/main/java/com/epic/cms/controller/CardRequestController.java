package com.epic.cms.controller;

import com.epic.cms.dto.ApproveRequestDTO;
import com.epic.cms.dto.CardRequestDTO;
import com.epic.cms.dto.CardRequestResponseDTO;
import com.epic.cms.dto.CreateCardRequestDTO;
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
    @Operation(summary = "Create a new request", description = "Create a new card request")
    public ResponseEntity<CardRequestDTO> createRequest(
            @Parameter(description = "Request creation details") @Valid @RequestBody CreateCardRequestDTO createRequestDTO) {
        log.info("POST /api/v1/requests - Create new request");
        CardRequestDTO createdRequest = cardRequestService.createRequest(createRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PutMapping("/{requestId}")
    @Operation(summary = "Update request", description = "Update an existing card request (only pending requests)")
    public ResponseEntity<CardRequestDTO> updateRequest(
            @Parameter(description = "Request ID") @PathVariable Integer requestId,
            @Parameter(description = "Request update details") @Valid @RequestBody CreateCardRequestDTO updateRequestDTO) {
        log.info("PUT /api/v1/requests/{} - Update request", requestId);
        CardRequestDTO updatedRequest = cardRequestService.updateRequest(requestId, updateRequestDTO);
        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{requestId}/process")
    @Operation(summary = "Approve/Reject request", description = "Approve or reject a card request")
    public ResponseEntity<CardRequestDTO> processRequest(
            @Parameter(description = "Request ID") @PathVariable Integer requestId,
            @Parameter(description = "Approval/Rejection details") @Valid @RequestBody ApproveRequestDTO approveRequestDTO) {
        log.info("POST /api/v1/requests/{}/process - Process request", requestId);
        CardRequestDTO updatedRequest = cardRequestService.approveOrRejectRequest(requestId, approveRequestDTO);
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
}
