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
public class OrdersSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public OrdersSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("Skip orders schema migration because no active database is selected");
            return;
        }
        if (!tableExists(databaseName, "orders")) {
            log.warn("Skip orders schema migration because table orders does not exist");
            return;
        }

        try {
            migrateOrdersTable(databaseName);
            log.info("Orders table schema migration finished");
        } catch (Exception ex) {
            log.error("Orders table schema migration failed", ex);
            throw ex;
        }
    }

    private void migrateOrdersTable(String databaseName) {
        Set<String> columns = loadColumns(databaseName, "orders");

        if (!columns.contains("number")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `number` INT NOT NULL DEFAULT 1 COMMENT 'order quantity' AFTER `price`");
            columns.add("number");
        }

        if (!columns.contains("state")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `state` VARCHAR(10) NOT NULL DEFAULT '1' COMMENT 'order state' AFTER `number`");
            columns.add("state");
        }

        if (!columns.contains("rating")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `rating` INT NULL DEFAULT NULL COMMENT 'order rating' AFTER `state`");
            columns.add("rating");
        }

        if (!columns.contains("review_content")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `review_content` VARCHAR(500) NULL DEFAULT NULL COMMENT 'order review content' AFTER `rating`");
            columns.add("review_content");
        }

        if (!columns.contains("addressid")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `addressid` INT NULL DEFAULT NULL COMMENT 'address id' AFTER `goodsid`");
            columns.add("addressid");
        }

        if (!columns.contains("logistics")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `logistics` VARCHAR(255) NULL DEFAULT NULL COMMENT 'logistics number' AFTER `addressid`");
            columns.add("logistics");
        }

        if (!columns.contains("update_time")) {
            executeSql("ALTER TABLE `orders` ADD COLUMN `update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER `state`");
            columns.add("update_time");
        }

        executeSql("UPDATE `orders` SET `number` = 1 WHERE `number` IS NULL OR `number` <= 0");
        executeSql("UPDATE `orders` SET `state` = '1' WHERE `state` IS NULL OR `state` = ''");

        if (!isAutoIncrement(databaseName, "orders", "id")) {
            executeSql("ALTER TABLE `orders` MODIFY COLUMN `id` INT NOT NULL AUTO_INCREMENT");
            executeSql("ALTER TABLE `orders` AUTO_INCREMENT = " + resolveNextAutoIncrementValue());
        }
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

    private boolean isAutoIncrement(String databaseName, String tableName, String columnName) {
        String extra = jdbcTemplate.queryForObject(
                "SELECT EXTRA FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                String.class,
                databaseName,
                tableName,
                columnName
        );
        return extra != null && extra.toLowerCase(Locale.ROOT).contains("auto_increment");
    }

    private Integer resolveNextAutoIncrementValue() {
        Integer nextValue = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM `orders`", Integer.class);
        return nextValue == null || nextValue <= 0 ? 1 : nextValue;
    }

    private void executeSql(String sql) {
        log.info("Execute orders schema SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
