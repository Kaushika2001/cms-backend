package com.epic.cms.repository;

import com.epic.cms.model.CardRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardRequest entity operations.
 * This interface defines the contract for data access operations on CardRequest entities.
 */
public interface ICardRequestRepository {
    
    /**
     * Retrieve all card requests from the database
     * @return List of all card requests
     */
    List<CardRequest> findAll();
    
    /**
     * Find a card request by its ID
     * @param requestId The request ID to search for
     * @return Optional containing the request if found
     */
    Optional<CardRequest> findById(Integer requestId);
    
    /**
     * Find all requests for a specific card
     * @param cardNumber The card number to search for
     * @return List of requests for the card
     */
    List<CardRequest> findByCardNumber(String cardNumber);
    
    /**
     * Find all requests with a specific status
     * @param statusCode The status code to filter by
     * @return List of requests with the specified status
     */
    List<CardRequest> findByRequestStatusCode(String statusCode);
    
    /**
     * Find all requests with a specific reason code
     * @param reasonCode The reason code to filter by
     * @return List of requests with the specified reason code
     */
    List<CardRequest> findByRequestReasonCode(String reasonCode);
    
    /**
     * Find all pending requests
     * @return List of pending requests
     */
    List<CardRequest> findPendingRequests();
    
    /**
     * Find requests by card number and status
     * @param cardNumber The card number to search for
     * @param statusCode The status code to filter by
     * @return List of matching requests
     */
    List<CardRequest> findByCardNumberAndStatus(String cardNumber, String statusCode);
    
    /**
     * Find requests created between two timestamps
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return List of requests created in the time range
     */
    List<CardRequest> findCreatedBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Insert a new card request
     * @param request The request to insert
     * @return The generated request ID
     */
    Integer insert(CardRequest request);
    
    /**
     * Update an existing card request
     * @param request The request with updated information
     * @return Number of rows affected
     */
    int update(CardRequest request);
    
    /**
     * Update the status of a request
     * @param requestId The request ID to update
     * @param newStatus The new status
     * @param remark Optional remark
     * @return Number of rows affected
     */
    int updateStatus(Integer requestId, String newStatus, String remark);
    
    /**
     * Delete a request by ID
     * @param requestId The request ID to delete
     * @return Number of rows affected
     */
    int deleteById(Integer requestId);
    
    /**
     * Count requests by status
     * @param statusCode The status to count
     * @return Number of requests with the specified status
     */
    Long countByStatus(String statusCode);
}
