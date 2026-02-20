package com.epic.cms.repository;

import com.epic.cms.model.Card;
import com.epic.cms.util.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CardRepository implements ICardRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Card> cardRowMapper = new CardRowMapper();

    private static class CardRowMapper implements RowMapper<Card> {
        @Override
        public Card mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Card.builder()
                    .cardNumber(rs.getString("CardNumber"))
                    .expiryDate(rs.getDate("ExpiryDate").toLocalDate())
                    .cardStatus(rs.getString("CardStatus"))
                    .creditLimit(rs.getBigDecimal("CreditLimit"))
                    .cashLimit(rs.getBigDecimal("CashLimit"))
                    .availableCreditLimit(rs.getBigDecimal("AvailableCreditLimit"))
                    .availableCashLimit(rs.getBigDecimal("AvailableCashLimit"))
                    .lastUpdateTime(rs.getTimestamp("LastUpdateTime").toLocalDateTime())
                    .build();
        }
    }

    @Override
    public List<Card> findAll() {
        String sql = "SELECT * FROM Card ORDER BY LastUpdateTime DESC";
        return jdbcTemplate.query(sql, cardRowMapper);
    }

    @Override
    public Optional<Card> findByCardNumber(String cardNumber) {
        String sql = "SELECT * FROM Card WHERE CardNumber = ?";
        List<Card> results = jdbcTemplate.query(sql, cardRowMapper, cardNumber);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Card> findByMaskedCardId(String maskedCardId) {
        // Since maskedCardId is generated from cardNumber, we need to find the matching card
        // by checking all cards and comparing their generated maskedCardId
        log.debug("Looking up card by maskedCardId: {}", maskedCardId);
        
        String sql = "SELECT * FROM Card";
        List<Card> allCards = jdbcTemplate.query(sql, cardRowMapper);
        
        // Find the card whose cardNumber generates the matching maskedCardId
        return allCards.stream()
                .filter(card -> maskedCardId.equals(CardMaskingUtil.generateMaskedCardId(card.getCardNumber())))
                .findFirst();
    }

    @Override
    public Optional<Card> findByMaskedCardNumber(String maskedCardNumber) {
        log.info("Looking up card by masked card number: {}", maskedCardNumber);
        
        if (maskedCardNumber == null || maskedCardNumber.isEmpty()) {
            log.warn("Masked card number is null or empty");
            return Optional.empty();
        }
        
        // Remove spaces and dashes
        String cleanMasked = maskedCardNumber.replaceAll("[\\s-]", "");
        
        // Check if it contains asterisks (is actually masked)
        if (!cleanMasked.contains("*")) {
            log.info("Card number is not masked, performing direct lookup");
            return findByCardNumber(cleanMasked);
        }
        
        // Extract the visible parts using utility methods
        String firstPart = CardMaskingUtil.extractFirstPart(maskedCardNumber);
        String lastPart = CardMaskingUtil.extractLastPart(maskedCardNumber);
        
        if (firstPart == null || lastPart == null) {
            log.error("Could not extract first/last parts from masked card number: {}. FirstPart: {}, LastPart: {}", 
                     maskedCardNumber, firstPart, lastPart);
            return Optional.empty();
        }
        
        log.info("Extracted parts - First: {}, Last: {}", firstPart, lastPart);
        
        // Get all cards and filter using the utility method
        String sql = "SELECT * FROM Card";
        List<Card> allCards = jdbcTemplate.query(sql, cardRowMapper);
        
        log.info("Total cards in database: {}", allCards.size());
        
        // Filter cards that match the masked pattern
        Optional<Card> result = allCards.stream()
                .filter(card -> {
                    boolean matches = CardMaskingUtil.matchesMaskedPattern(card.getCardNumber(), maskedCardNumber);
                    if (matches) {
                        log.info("Found matching card: {} matches pattern {}", 
                                CardMaskingUtil.mask(card.getCardNumber()), maskedCardNumber);
                    }
                    return matches;
                })
                .findFirst();
        
        if (result.isEmpty()) {
            log.warn("No card found matching masked pattern: {}", maskedCardNumber);
        }
        
        return result;
    }

    @Override
    public List<Card> findByCardStatus(String cardStatus) {
        String sql = "SELECT * FROM Card WHERE CardStatus = ? ORDER BY LastUpdateTime DESC";
        return jdbcTemplate.query(sql, cardRowMapper, cardStatus);
    }

    @Override
    public List<Card> findExpiredCards() {
        String sql = "SELECT * FROM Card WHERE ExpiryDate < CURRENT_DATE ORDER BY ExpiryDate";
        return jdbcTemplate.query(sql, cardRowMapper);
    }

    @Override
    public List<Card> findExpiringBetween(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM Card WHERE ExpiryDate BETWEEN ? AND ? ORDER BY ExpiryDate";
        return jdbcTemplate.query(sql, cardRowMapper, startDate, endDate);
    }

    @Override
    public int insert(Card card) {
        String sql = "INSERT INTO Card (CardNumber, ExpiryDate, CardStatus, CreditLimit, CashLimit, " +
                     "AvailableCreditLimit, AvailableCashLimit, LastUpdateTime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        return jdbcTemplate.update(sql,
                card.getCardNumber(),
                card.getExpiryDate(),
                card.getCardStatus(),
                card.getCreditLimit(),
                card.getCashLimit(),
                card.getAvailableCreditLimit(),
                card.getAvailableCashLimit());
    }

    @Override
    public int update(Card card) {
        String sql = "UPDATE Card SET ExpiryDate = ?, CardStatus = ?, CreditLimit = ?, CashLimit = ?, " +
                     "AvailableCreditLimit = ?, AvailableCashLimit = ?, LastUpdateTime = CURRENT_TIMESTAMP " +
                     "WHERE CardNumber = ?";
        return jdbcTemplate.update(sql,
                card.getExpiryDate(),
                card.getCardStatus(),
                card.getCreditLimit(),
                card.getCashLimit(),
                card.getAvailableCreditLimit(),
                card.getAvailableCashLimit(),
                card.getCardNumber());
    }

    @Override
    public int updateCardStatus(String cardNumber, String newStatus) {
        String sql = "UPDATE Card SET CardStatus = ?, LastUpdateTime = CURRENT_TIMESTAMP WHERE CardNumber = ?";
        return jdbcTemplate.update(sql, newStatus, cardNumber);
    }

    @Override
    public int deleteByCardNumber(String cardNumber) {
        String sql = "DELETE FROM Card WHERE CardNumber = ?";
        return jdbcTemplate.update(sql, cardNumber);
    }

    @Override
    public boolean existsByCardNumber(String cardNumber) {
        String sql = "SELECT COUNT(*) FROM Card WHERE CardNumber = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cardNumber);
        return count != null && count > 0;
    }

    @Override
    public Long countByStatus(String cardStatus) {
        String sql = "SELECT COUNT(*) FROM Card WHERE CardStatus = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, cardStatus);
    }
}
