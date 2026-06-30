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
public class UserSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("Skip user schema migration because no active database is selected");
            return;
        }
        if (!tableExists(databaseName, "user")) {
            log.warn("Skip user schema migration because table user does not exist");
            return;
        }

        try {
            migrateUserTable(databaseName);
            log.info("User table schema migration finished");
        } catch (Exception ex) {
            log.error("User table schema migration failed", ex);
            throw ex;
        }
    }

    private void migrateUserTable(String databaseName) {
        Set<String> columns = loadColumns(databaseName, "user");

        ensureMinVarcharLength(databaseName, columns, "user", "tel", 128, "encrypted phone");
        ensureMinVarcharLength(databaseName, columns, "user", "idcard", 128, "encrypted id card");
        ensureMinVarcharLength(databaseName, columns, "user", "password", 255, "hashed password");

        if (!columns.contains("icon")) {
            executeSql("ALTER TABLE `user` ADD COLUMN `icon` VARCHAR(500) NULL DEFAULT NULL COMMENT 'user avatar' AFTER `roomnumb`");
            columns.add("icon");
        } else {
            Long iconLength = getColumnLength(databaseName, "user", "icon");
            if (iconLength != null && iconLength > 0 && iconLength < 500) {
                executeSql("ALTER TABLE `user` MODIFY COLUMN `icon` VARCHAR(500) NULL DEFAULT NULL COMMENT 'user avatar'");
            }
        }

        if (!columns.contains("balance")) {
            executeSql("ALTER TABLE `user` ADD COLUMN `balance` DOUBLE NOT NULL DEFAULT 0 COMMENT 'user balance' AFTER `idcard`");
            columns.add("balance");
        }

        executeSql("UPDATE `user` SET `balance` = 0 WHERE `balance` IS NULL OR `balance` < 0");

        if (!isAutoIncrement(databaseName, "user", "id")) {
            executeSql("ALTER TABLE `user` MODIFY COLUMN `id` INT NOT NULL AUTO_INCREMENT");
            executeSql("ALTER TABLE `user` AUTO_INCREMENT = " + resolveNextAutoIncrementValue());
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

    private Long getColumnLength(String databaseName, String tableName, String columnName) {
        return jdbcTemplate.queryForObject(
                "SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Long.class,
                databaseName,
                tableName,
                columnName
        );
    }

    private void ensureMinVarcharLength(
            String databaseName,
            Set<String> columns,
            String tableName,
            String columnName,
            long requiredLength,
            String comment) {
        if (!columns.contains(columnName)) {
            log.warn("Skip schema migration for {}.{} because the column does not exist", tableName, columnName);
            return;
        }

        Long currentLength = getColumnLength(databaseName, tableName, columnName);
        if (currentLength != null && currentLength >= requiredLength) {
            return;
        }

        executeSql(String.format(
                "ALTER TABLE `%s` MODIFY COLUMN `%s` VARCHAR(%d) NULL DEFAULT NULL COMMENT '%s'",
                tableName,
                columnName,
                requiredLength,
                comment
        ));
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
        Integer nextValue = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM `user`", Integer.class);
        return nextValue == null || nextValue <= 0 ? 1 : nextValue;
    }

    private void executeSql(String sql) {
        log.info("Execute user schema SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
