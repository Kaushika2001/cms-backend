package com.epic.cms.repository;

import com.epic.cms.model.Card;
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
