package com.epic.cms.repository;

import com.epic.cms.model.Card;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Card entity operations.
 * This interface defines the contract for data access operations on Card entities.
 */
public interface ICardRepository {
    
    /**
     * Retrieve all cards from the database
     * @return List of all cards
     */
    List<Card> findAll();
    
    /**
     * Find a card by its card number
     * @param cardNumber The card number to search for
     * @return Optional containing the card if found
     */
    Optional<Card> findByCardNumber(String cardNumber);
    
    /**
     * Find a card by its masked card ID.
     * The masked card ID is generated from the card number using a hash.
     * @param maskedCardId The masked card ID to search for (e.g., CRD-M-943E49CC)
     * @return Optional containing the card if found
     */
    Optional<Card> findByMaskedCardId(String maskedCardId);
    
    /**
     * Find a card by its masked card number pattern.
     * Matches cards where the first 6 and last 4 digits match.
     * @param maskedCardNumber The masked card number (e.g., 589925******0233)
     * @return Optional containing the card if found (returns first match if multiple)
     */
    Optional<Card> findByMaskedCardNumber(String maskedCardNumber);
    
    /**
     * Find all cards with a specific status
     * @param cardStatus The status to filter by
     * @return List of cards with the specified status
     */
    List<Card> findByCardStatus(String cardStatus);
    
    /**
     * Find all expired cards
     * @return List of expired cards
     */
    List<Card> findExpiredCards();
    
    /**
     * Find cards expiring between two dates
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of cards expiring in the date range
     */
    List<Card> findExpiringBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Insert a new card into the database
     * @param card The card to insert
     * @return Number of rows affected
     */
    int insert(Card card);
    
    /**
     * Update an existing card
     * @param card The card with updated information
     * @return Number of rows affected
     */
    int update(Card card);
    
    /**
     * Update the status of a card
     * @param cardNumber The card number to update
     * @param newStatus The new status
     * @return Number of rows affected
     */
    int updateCardStatus(String cardNumber, String newStatus);
    
    /**
     * Delete a card by card number
     * @param cardNumber The card number to delete
     * @return Number of rows affected
     */
    int deleteByCardNumber(String cardNumber);
    
    /**
     * Check if a card exists by card number
     * @param cardNumber The card number to check
     * @return true if the card exists, false otherwise
     */
    boolean existsByCardNumber(String cardNumber);
    
    /**
     * Count cards by status
     * @param cardStatus The status to count
     * @return Number of cards with the specified status
     */
    Long countByStatus(String cardStatus);
}
