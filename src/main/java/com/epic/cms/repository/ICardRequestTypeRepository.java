package com.epic.cms.repository;

import com.epic.cms.model.CardRequestType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardRequestType lookup table operations.
 * This interface defines the contract for data access operations on CardRequestType entities.
 */
public interface ICardRequestTypeRepository {
    
    /**
     * Retrieve all card request types
     * @return List of all card request types
     */
    List<CardRequestType> findAll();
    
    /**
     * Find a card request type by its code
     * @param code The code to search for
     * @return Optional containing the card request type if found
     */
    Optional<CardRequestType> findByCode(String code);
}
