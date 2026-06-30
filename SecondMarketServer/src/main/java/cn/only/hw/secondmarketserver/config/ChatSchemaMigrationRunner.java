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
public class ChatSchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ChatSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("Skip chat schema migration because no active database is selected");
            return;
        }

        try {
            ensureChatSessionTable(databaseName);
            ensureChatMessageTable(databaseName);
            log.info("Chat schema migration finished");
        } catch (Exception ex) {
            log.error("Chat schema migration failed", ex);
            throw ex;
        }
    }

    private void ensureChatSessionTable(String databaseName) {
        executeSql("CREATE TABLE IF NOT EXISTS `chat_session` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'chat session id',"
                + "`user_id` INT NOT NULL COMMENT 'initiator user id',"
                + "`target_user_id` INT NOT NULL COMMENT 'target user id',"
                + "`goods_id` INT DEFAULT NULL COMMENT 'related goods id',"
                + "`last_message` VARCHAR(500) DEFAULT NULL COMMENT 'last message',"
                + "`last_message_time` DATETIME DEFAULT NULL COMMENT 'last message time',"
                + "`unread_count_user` INT DEFAULT 0 COMMENT 'unread count for initiator',"
                + "`unread_count_target` INT DEFAULT 0 COMMENT 'unread count for target user',"
                + "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',"
                + "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',"
                + "PRIMARY KEY (`id`),"
                + "UNIQUE KEY `uk_user_target_goods` (`user_id`,`target_user_id`,`goods_id`),"
                + "KEY `idx_user_id` (`user_id`),"
                + "KEY `idx_target_user_id` (`target_user_id`),"
                + "KEY `idx_goods_id` (`goods_id`),"
                + "KEY `idx_last_message_time` (`last_message_time`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='chat session table'");

        Set<String> columns = loadColumns(databaseName, "chat_session");
        if (!columns.contains("goods_id")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN goods_id INT DEFAULT NULL COMMENT 'related goods id' AFTER target_user_id");
        }
        if (!columns.contains("last_message")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN last_message VARCHAR(500) DEFAULT NULL COMMENT 'last message' AFTER goods_id");
        }
        if (!columns.contains("last_message_time")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN last_message_time DATETIME DEFAULT NULL COMMENT 'last message time' AFTER last_message");
        }
        if (!columns.contains("unread_count_user")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN unread_count_user INT DEFAULT 0 COMMENT 'unread count for initiator' AFTER last_message_time");
        }
        if (!columns.contains("unread_count_target")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN unread_count_target INT DEFAULT 0 COMMENT 'unread count for target user' AFTER unread_count_user");
        }
        if (!columns.contains("create_time")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time' AFTER unread_count_target");
        }
        if (!columns.contains("update_time")) {
            executeSql("ALTER TABLE chat_session ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER create_time");
        }

        ensureIndexExists(databaseName, "chat_session", "idx_user_id",
                "ALTER TABLE chat_session ADD INDEX idx_user_id (user_id)");
        ensureIndexExists(databaseName, "chat_session", "idx_target_user_id",
                "ALTER TABLE chat_session ADD INDEX idx_target_user_id (target_user_id)");
        ensureIndexExists(databaseName, "chat_session", "idx_goods_id",
                "ALTER TABLE chat_session ADD INDEX idx_goods_id (goods_id)");
        ensureIndexExists(databaseName, "chat_session", "idx_last_message_time",
                "ALTER TABLE chat_session ADD INDEX idx_last_message_time (last_message_time)");

        executeSql("UPDATE chat_session SET unread_count_user = 0 WHERE unread_count_user IS NULL OR unread_count_user < 0");
        executeSql("UPDATE chat_session SET unread_count_target = 0 WHERE unread_count_target IS NULL OR unread_count_target < 0");
    }

    private void ensureChatMessageTable(String databaseName) {
        executeSql("CREATE TABLE IF NOT EXISTS `chat_message` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'chat message id',"
                + "`session_id` BIGINT NOT NULL COMMENT 'session id',"
                + "`sender_id` INT NOT NULL COMMENT 'sender user id',"
                + "`receiver_id` INT NOT NULL COMMENT 'receiver user id',"
                + "`message_type` VARCHAR(20) DEFAULT 'text' COMMENT 'message type',"
                + "`content` TEXT NOT NULL COMMENT 'message content',"
                + "`is_read` TINYINT DEFAULT 0 COMMENT 'read status',"
                + "`read_time` DATETIME DEFAULT NULL COMMENT 'read time',"
                + "`send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'send time',"
                + "PRIMARY KEY (`id`),"
                + "KEY `idx_session_id` (`session_id`),"
                + "KEY `idx_sender_id` (`sender_id`),"
                + "KEY `idx_receiver_id` (`receiver_id`),"
                + "KEY `idx_send_time` (`send_time`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='chat message table'");

        Set<String> columns = loadColumns(databaseName, "chat_message");
        if (!columns.contains("message_type")) {
            executeSql("ALTER TABLE chat_message ADD COLUMN message_type VARCHAR(20) DEFAULT 'text' COMMENT 'message type' AFTER receiver_id");
        }
        if (!columns.contains("is_read")) {
            executeSql("ALTER TABLE chat_message ADD COLUMN is_read TINYINT DEFAULT 0 COMMENT 'read status' AFTER content");
        }
        if (!columns.contains("read_time")) {
            executeSql("ALTER TABLE chat_message ADD COLUMN read_time DATETIME DEFAULT NULL COMMENT 'read time' AFTER is_read");
        }
        if (!columns.contains("send_time")) {
            executeSql("ALTER TABLE chat_message ADD COLUMN send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'send time' AFTER read_time");
        }

        ensureIndexExists(databaseName, "chat_message", "idx_session_id",
                "ALTER TABLE chat_message ADD INDEX idx_session_id (session_id)");
        ensureIndexExists(databaseName, "chat_message", "idx_sender_id",
                "ALTER TABLE chat_message ADD INDEX idx_sender_id (sender_id)");
        ensureIndexExists(databaseName, "chat_message", "idx_receiver_id",
                "ALTER TABLE chat_message ADD INDEX idx_receiver_id (receiver_id)");
        ensureIndexExists(databaseName, "chat_message", "idx_send_time",
                "ALTER TABLE chat_message ADD INDEX idx_send_time (send_time)");

        executeSql("UPDATE chat_message SET message_type = 'text' WHERE message_type IS NULL OR message_type = ''");
        executeSql("UPDATE chat_message SET is_read = 0 WHERE is_read IS NULL OR is_read NOT IN (0, 1)");
    }

    private Set<String> loadColumns(String databaseName, String tableName) {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                String.class,
                databaseName,
                tableName
        );
        return columns.stream()
                .map(item -> item.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private void ensureIndexExists(String databaseName, String tableName, String indexName, String createSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                databaseName,
                tableName,
                indexName
        );
        if (count == null || count == 0) {
            executeSql(createSql);
        }
    }

    private void executeSql(String sql) {
        log.info("Execute chat schema SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
