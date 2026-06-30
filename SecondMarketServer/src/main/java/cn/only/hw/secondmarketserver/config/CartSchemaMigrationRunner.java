package cn.only.hw.secondmarketserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CartSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public CartSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("跳过购物车表结构迁移，因为未选择活动数据库");
            return;
        }
        if (!tableExists(databaseName, "cart")) {
            log.warn("跳过购物车表结构迁移，因为购物车表不存在");
            return;
        }

        try {
            migrateCartTable(databaseName);
            log.info("购物车表结构迁移完成");
        } catch (Exception ex) {
            log.error("购物车表结构迁移失败", ex);
            throw ex;
        }
    }

    private void migrateCartTable(String databaseName) {
        Set<String> columns = loadColumns(databaseName, "cart");

        if (!columns.contains("number")) {
            executeSql("ALTER TABLE cart ADD COLUMN number INT NOT NULL DEFAULT 1 COMMENT 'cart quantity' AFTER price");
            columns.add("number");
        }

        executeSql("UPDATE cart SET number = 1 WHERE number IS NULL OR number <= 0");
    }

    private boolean tableExists(String databaseName, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                Integer.class,
                databaseName,
                tableName
        );
        return count != null && count > 0;
    }

    private Set<String> loadColumns(String databaseName, String tableName) {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                String.class,
                databaseName,
                tableName
        );
        return columns.stream()
                .map(column -> column.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private void executeSql(String sql) {
        log.info("执行购物车表结构SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
