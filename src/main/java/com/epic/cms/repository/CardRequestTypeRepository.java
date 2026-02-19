package com.epic.cms.repository;

import com.epic.cms.model.CardRequestType;
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
public class CardRequestTypeRepository implements ICardRequestTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<CardRequestType> rowMapper = new CardRequestTypeRowMapper();

    private static class CardRequestTypeRowMapper implements RowMapper<CardRequestType> {
        @Override
        public CardRequestType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return CardRequestType.builder()
                    .code(rs.getString("Code"))
                    .description(rs.getString("Description"))
                    .build();
        }
    }

    @Override
    public List<CardRequestType> findAll() {
        String sql = "SELECT * FROM CardRequestType ORDER BY Code";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<CardRequestType> findByCode(String code) {
        String sql = "SELECT * FROM CardRequestType WHERE Code = ?";
        List<CardRequestType> results = jdbcTemplate.query(sql, rowMapper, code);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
