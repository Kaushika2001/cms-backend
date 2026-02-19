package com.epic.cms.repository;

import com.epic.cms.model.CardRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CardRequestRepository implements ICardRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CardRequest> requestRowMapper = new CardRequestRowMapper();

    private static class CardRequestRowMapper implements RowMapper<CardRequest> {
        @Override
        public CardRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return CardRequest.builder()
                    .requestId(rs.getInt("RequestId"))
                    .cardNumber(rs.getString("CardNumber"))
                    .requestReasonCode(rs.getString("RequestReasonCode"))
                    .requestStatusCode(rs.getString("RequestStatusCode"))
                    .remark(rs.getString("Remark"))
                    .createdTime(rs.getTimestamp("CreatedTime").toLocalDateTime())
                    .build();
        }
    }

    @Override
    public List<CardRequest> findAll() {
        String sql = "SELECT * FROM CardRequest ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper);
    }

    @Override
    public Optional<CardRequest> findById(Integer requestId) {
        String sql = "SELECT * FROM CardRequest WHERE RequestId = ?";
        List<CardRequest> results = jdbcTemplate.query(sql, requestRowMapper, requestId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<CardRequest> findByCardNumber(String cardNumber) {
        String sql = "SELECT * FROM CardRequest WHERE CardNumber = ? ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper, cardNumber);
    }

    @Override
    public List<CardRequest> findByRequestStatusCode(String statusCode) {
        String sql = "SELECT * FROM CardRequest WHERE RequestStatusCode = ? ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper, statusCode);
    }

    @Override
    public List<CardRequest> findByRequestReasonCode(String reasonCode) {
        String sql = "SELECT * FROM CardRequest WHERE RequestReasonCode = ? ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper, reasonCode);
    }

    @Override
    public List<CardRequest> findPendingRequests() {
        String sql = "SELECT * FROM CardRequest WHERE RequestStatusCode = 'PEND' ORDER BY CreatedTime ASC";
        return jdbcTemplate.query(sql, requestRowMapper);
    }

    @Override
    public List<CardRequest> findByCardNumberAndStatus(String cardNumber, String statusCode) {
        String sql = "SELECT * FROM CardRequest WHERE CardNumber = ? AND RequestStatusCode = ? ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper, cardNumber, statusCode);
    }

    @Override
    public List<CardRequest> findCreatedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM CardRequest WHERE CreatedTime BETWEEN ? AND ? ORDER BY CreatedTime DESC";
        return jdbcTemplate.query(sql, requestRowMapper, startTime, endTime);
    }

    @Override
    public Integer insert(CardRequest request) {
        String sql = "INSERT INTO CardRequest (CardNumber, RequestReasonCode, RequestStatusCode, Remark, CreatedTime) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING RequestId";
        
        return jdbcTemplate.queryForObject(sql, Integer.class,
                request.getCardNumber(),
                request.getRequestReasonCode(),
                request.getRequestStatusCode(),
                request.getRemark());
    }

    @Override
    public int update(CardRequest request) {
        String sql = "UPDATE CardRequest SET RequestReasonCode = ?, RequestStatusCode = ?, Remark = ? " +
                     "WHERE RequestId = ?";
        return jdbcTemplate.update(sql,
                request.getRequestReasonCode(),
                request.getRequestStatusCode(),
                request.getRemark(),
                request.getRequestId());
    }

    @Override
    public int updateStatus(Integer requestId, String newStatus, String remark) {
        String sql = "UPDATE CardRequest SET RequestStatusCode = ?, Remark = ? WHERE RequestId = ?";
        return jdbcTemplate.update(sql, newStatus, remark, requestId);
    }

    @Override
    public int deleteById(Integer requestId) {
        String sql = "DELETE FROM CardRequest WHERE RequestId = ?";
        return jdbcTemplate.update(sql, requestId);
    }

    @Override
    public Long countByStatus(String statusCode) {
        String sql = "SELECT COUNT(*) FROM CardRequest WHERE RequestStatusCode = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, statusCode);
    }
}
