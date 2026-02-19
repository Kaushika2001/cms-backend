package com.epic.cms.repository;

import com.epic.cms.model.CardStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardStatus lookup table operations.
 * This interface defines the contract for data access operations on CardStatus entities.
 */
public interface ICardStatusRepository {
    
    /**
     * Retrieve all card status codes
     * @return List of all card statuses
     */
    List<CardStatus> findAll();
    
    /**
     * Find a card status by its status code
     * @param statusCode The status code to search for
     * @return Optional containing the card status if found
     */
    Optional<CardStatus> findByStatusCode(String statusCode);
}
