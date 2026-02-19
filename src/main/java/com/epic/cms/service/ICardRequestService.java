package com.epic.cms.service;

import com.epic.cms.dto.ApproveRequestDTO;
import com.epic.cms.dto.CardRequestDTO;
import com.epic.cms.dto.CreateCardRequestDTO;

import java.util.List;

/**
 * Service interface for Card Request operations.
 * This interface defines the contract for business logic related to card requests.
 */
public interface ICardRequestService {
    
    /**
     * Get all card requests
     * @return List of all card requests
     */
    List<CardRequestDTO> getAllRequests();
    
    /**
     * Get a card request by ID
     * @param requestId The request ID
     * @return The card request DTO
     */
    CardRequestDTO getRequestById(Integer requestId);
    
    /**
     * Get all requests for a specific card
     * @param cardNumber The card number
     * @return List of requests for the card
     */
    List<CardRequestDTO> getRequestsByCardNumber(String cardNumber);
    
    /**
     * Get all requests with a specific status
     * @param statusCode The status code
     * @return List of requests with the status
     */
    List<CardRequestDTO> getRequestsByStatus(String statusCode);
    
    /**
     * Get all requests of a specific type
     * @param requestType The request type
     * @return List of requests with the type
     */
    List<CardRequestDTO> getRequestsByType(String requestType);
    
    /**
     * Get all pending requests
     * @return List of pending requests
     */
    List<CardRequestDTO> getPendingRequests();
    
    /**
     * Create a new card request
     * @param createRequestDTO The request creation data
     * @return The created card request
     */
    CardRequestDTO createRequest(CreateCardRequestDTO createRequestDTO);
    
    /**
     * Update an existing card request
     * @param requestId The request ID
     * @param updateRequestDTO The update data
     * @return The updated card request
     */
    CardRequestDTO updateRequest(Integer requestId, CreateCardRequestDTO updateRequestDTO);
    
    /**
     * Approve or reject a card request
     * @param requestId The request ID
     * @param approveRequestDTO The approval/rejection data
     * @return The updated card request
     */
    CardRequestDTO approveOrRejectRequest(Integer requestId, ApproveRequestDTO approveRequestDTO);
    
    /**
     * Delete a card request
     * @param requestId The request ID
     */
    void deleteRequest(Integer requestId);
    
    /**
     * Get count of requests by status
     * @param statusCode The status code
     * @return Number of requests with the status
     */
    Long getRequestCountByStatus(String statusCode);
}
