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
public class ManagerSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ManagerSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("跳过管理员表结构迁移，因为未选择活动数据库");
            return;
        }
        if (!tableExists(databaseName, "manager")) {
            log.warn("跳过管理员表结构迁移，因为管理员表不存在");
            return;
        }

        try {
            migrateManagerTable(databaseName);
            log.info("管理员表结构迁移完成");
        } catch (Exception ex) {
            log.error("管理员表结构迁移失败", ex);
            throw ex;
        }
    }

    private void migrateManagerTable(String databaseName) {
        Set<String> columns = loadColumns(databaseName, "manager");

        if (!columns.contains("avatar")) {
            executeSql("ALTER TABLE `manager` ADD COLUMN `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT 'manager avatar' AFTER `password`");
            columns.add("avatar");
        } else {
            Long avatarLength = getColumnLength(databaseName, "manager", "avatar");
            if (avatarLength != null && avatarLength > 0 && avatarLength < 500) {
                executeSql("ALTER TABLE `manager` MODIFY COLUMN `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT 'manager avatar'");
            }
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

    private void executeSql(String sql) {
        log.info("执行管理员表结构SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
