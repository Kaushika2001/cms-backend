package com.epic.cms.repository;

import com.epic.cms.model.RequestStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RequestStatus lookup table operations.
 * This interface defines the contract for data access operations on RequestStatus entities.
 */
public interface IRequestStatusRepository {
    
    /**
     * Retrieve all request status codes
     * @return List of all request statuses
     */
    List<RequestStatus> findAll();
    
    /**
     * Find a request status by its status code
     * @param statusCode The status code to search for
     * @return Optional containing the request status if found
     */
    Optional<RequestStatus> findByStatusCode(String statusCode);
}
