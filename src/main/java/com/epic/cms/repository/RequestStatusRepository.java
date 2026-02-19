package com.epic.cms.repository;

import com.epic.cms.model.RequestStatus;
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
public class RequestStatusRepository implements IRequestStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RequestStatus> rowMapper = new RequestStatusRowMapper();

    private static class RequestStatusRowMapper implements RowMapper<RequestStatus> {
        @Override
        public RequestStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            return RequestStatus.builder()
                    .statusCode(rs.getString("StatusCode"))
                    .description(rs.getString("Description"))
                    .build();
        }
    }

    @Override
    public List<RequestStatus> findAll() {
        String sql = "SELECT * FROM RequestStatus ORDER BY StatusCode";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<RequestStatus> findByStatusCode(String statusCode) {
        String sql = "SELECT * FROM RequestStatus WHERE StatusCode = ?";
        List<RequestStatus> results = jdbcTemplate.query(sql, rowMapper, statusCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
