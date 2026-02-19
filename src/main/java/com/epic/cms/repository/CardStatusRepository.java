package com.epic.cms.repository;

import com.epic.cms.model.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CardStatusRepository implements ICardStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CardStatus> rowMapper = new CardStatusRowMapper();

    private static class CardStatusRowMapper implements RowMapper<CardStatus> {
        @Override
        public CardStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            return CardStatus.builder()
                    .statusCode(rs.getString("StatusCode"))
                    .description(rs.getString("Description"))
                    .build();
        }
    }

    @Override
    public List<CardStatus> findAll() {
        String sql = "SELECT * FROM CardStatus ORDER BY StatusCode";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<CardStatus> findByStatusCode(String statusCode) {
        String sql = "SELECT * FROM CardStatus WHERE StatusCode = ?";
        List<CardStatus> results = jdbcTemplate.query(sql, rowMapper, statusCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
