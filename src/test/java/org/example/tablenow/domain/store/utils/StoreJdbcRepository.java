package org.example.tablenow.domain.store.utils;

import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

@Repository
public class StoreJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 5000;

    public StoreJdbcRepository(@Qualifier("dataDBSource") DataSource dataDBSource) {
        this.jdbcTemplate = new JdbcTemplate(dataDBSource);
    }

    public void insertBatch(List<StoreCreateRequestDto> dto) {
        String sql = """
                INSERT INTO STORE (name, description, address, user_id, category_id, capacity, deposit,
                                    start_time, end_time, created_at, updated_at, rating, rating_count)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);
                """;

        for (int i = 0; i < dto.size(); i += BATCH_SIZE) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis() - new Random().nextLong(Integer.MAX_VALUE));
            List<StoreCreateRequestDto> storeCreateRequestDtos = dto.subList(i, i + BATCH_SIZE);
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, storeCreateRequestDtos.get(i).getName());
                    ps.setString(2, storeCreateRequestDtos.get(i).getDescription());
                    ps.setString(3, storeCreateRequestDtos.get(i).getAddress());
                    ps.setLong(4, 2L);
                    ps.setLong(5, storeCreateRequestDtos.get(i).getCategoryId());
                    ps.setInt(6, storeCreateRequestDtos.get(i).getCapacity());
                    ps.setInt(7, storeCreateRequestDtos.get(i).getDeposit());
                    ps.setObject(8, storeCreateRequestDtos.get(i).getStartTime());
                    ps.setObject(9, storeCreateRequestDtos.get(i).getEndTime());
                    ps.setTimestamp(10, timestamp);
                    ps.setTimestamp(11, timestamp);
                    ps.setDouble(12, Math.round(new Random().nextDouble(0.0, 5.0) * 10) / 10.0);
                    ps.setInt(13, new Random().nextInt(1, 999));
                }

                @Override
                public int getBatchSize() {
                    return storeCreateRequestDtos.size();
                }
            });
        }
    }
}
