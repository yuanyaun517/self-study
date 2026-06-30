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
public class ForumCommentSchemaMigrationRunner implements CommandLineRunner {

    private static final String TABLE_NAME = "forum_comment";

    private final JdbcTemplate jdbcTemplate;

    public ForumCommentSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("Skip forum comment schema migration because no active database is selected");
            return;
        }

        try {
            migrateForumCommentTable(databaseName);
            log.info("Forum comment table schema migration finished");
        } catch (Exception ex) {
            log.error("Forum comment table schema migration failed", ex);
            throw ex;
        }
    }

    private void migrateForumCommentTable(String databaseName) {
        if (!tableExists(databaseName, TABLE_NAME)) {
            executeSql("CREATE TABLE `forum_comment` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'comment content',"
                    + "`forum_id` int(11) NOT NULL COMMENT 'forum post id',"
                    + "`user_id` int(11) NOT NULL COMMENT 'comment user id',"
                    + "`parent_id` int(11) DEFAULT NULL COMMENT 'parent comment id',"
                    + "`reply_to_user_id` int(11) DEFAULT NULL COMMENT 'reply to user id',"
                    + "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',"
                    + "`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',"
                    + "`status` tinyint(4) DEFAULT '1' COMMENT 'status: 1-normal, 0-deleted',"
                    + "PRIMARY KEY (`id`) USING BTREE,"
                    + "KEY `idx_forum_id` (`forum_id`) USING BTREE,"
                    + "KEY `idx_user_id` (`user_id`) USING BTREE,"
                    + "KEY `idx_parent_id` (`parent_id`) USING BTREE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC");
            return;
        }

        Set<String> columns = loadColumns(databaseName, TABLE_NAME);
        if (!columns.contains("content")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN content VARCHAR(500) NOT NULL COMMENT 'comment content' AFTER id");
        }
        if (!columns.contains("forum_id")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN forum_id INT(11) NOT NULL COMMENT 'forum post id' AFTER content");
        }
        if (!columns.contains("user_id")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN user_id INT(11) NOT NULL COMMENT 'comment user id' AFTER forum_id");
        }
        if (!columns.contains("parent_id")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN parent_id INT(11) NULL DEFAULT NULL COMMENT 'parent comment id' AFTER user_id");
        }
        if (!columns.contains("reply_to_user_id")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN reply_to_user_id INT(11) NULL DEFAULT NULL COMMENT 'reply to user id' AFTER parent_id");
        }
        if (!columns.contains("create_time")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER reply_to_user_id");
        }
        if (!columns.contains("update_time")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER create_time");
        }
        if (!columns.contains("status")) {
            executeSql("ALTER TABLE forum_comment ADD COLUMN status TINYINT(4) NULL DEFAULT '1' COMMENT 'status: 1-normal, 0-deleted' AFTER update_time");
        }

        createIndexIfMissing(databaseName, "idx_forum_id", "ALTER TABLE forum_comment ADD INDEX idx_forum_id (forum_id)");
        createIndexIfMissing(databaseName, "idx_user_id", "ALTER TABLE forum_comment ADD INDEX idx_user_id (user_id)");
        createIndexIfMissing(databaseName, "idx_parent_id", "ALTER TABLE forum_comment ADD INDEX idx_parent_id (parent_id)");
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

    private void createIndexIfMissing(String databaseName, String indexName, String createIndexSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                databaseName,
                TABLE_NAME,
                indexName
        );
        if (count != null && count > 0) {
            return;
        }
        executeSql(createIndexSql);
    }

    private void executeSql(String sql) {
        log.info("Execute forum comment schema SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
