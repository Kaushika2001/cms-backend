package com.epic.cms.service.impl;

import com.epic.cms.constants.StatusCodes;
import com.epic.cms.dto.ApproveRequestDTO;
import com.epic.cms.dto.CardRequestDTO;
import com.epic.cms.dto.CardRequestResponseDTO;
import com.epic.cms.dto.CreateCardRequestDTO;
import com.epic.cms.exception.InvalidRequestException;
import com.epic.cms.exception.ResourceNotFoundException;
import com.epic.cms.model.CardRequest;
import com.epic.cms.repository.ICardRepository;
import com.epic.cms.repository.ICardRequestRepository;
import com.epic.cms.repository.ICardRequestTypeRepository;
import com.epic.cms.repository.IRequestStatusRepository;
import com.epic.cms.service.ICardRequestService;
import com.epic.cms.util.CardMaskingUtil;
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

        // Verify card exists
        cardRepository.findByCardNumber(createRequestDTO.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber", createRequestDTO.getCardNumber()));

        // Validate request type exists
        cardRequestTypeRepository.findByCode(createRequestDTO.getRequestReasonCode())
                .orElseThrow(() -> new ResourceNotFoundException("CardRequestType", "code", createRequestDTO.getRequestReasonCode()));

        CardRequest request = CardRequest.builder()
                .cardNumber(createRequestDTO.getCardNumber())
                .requestReasonCode(createRequestDTO.getRequestReasonCode())
                .requestStatusCode(StatusCodes.REQUEST_PENDING)
                .remark(createRequestDTO.getRemark())
                .build();

        Integer requestId = cardRequestRepository.insert(request);
        log.info("Card request created successfully with id: {}", requestId);

        return convertToDTO(cardRequestRepository.findById(requestId).get());
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

        request.setRequestReasonCode(updateRequestDTO.getRequestReasonCode());
        request.setRemark(updateRequestDTO.getRemark());

        cardRequestRepository.update(request);
        log.info("Card request updated successfully: {}", requestId);

        return convertToDTO(cardRequestRepository.findById(requestId).get());
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
        CardRequestResponseDTO dto = CardRequestResponseDTO.builder()
                .requestId(request.getRequestId())
                .cardNumber(CardMaskingUtil.mask(request.getCardNumber()))
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
}
