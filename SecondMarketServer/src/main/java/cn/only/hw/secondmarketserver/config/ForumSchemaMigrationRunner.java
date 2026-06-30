package cn.only.hw.secondmarketserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 论坛表结构迁移运行器
 * 用于在应用启动时自动检查和更新forum表的结构，确保数据库表结构与代码期望一致
 */
@Component
@Slf4j
public class ForumSchemaMigrationRunner implements CommandLineRunner {

    /** 默认论坛类型 */
    private static final String DEFAULT_FORUM_TYPE = "校园日常";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数注入JdbcTemplate
     *
     * @param jdbcTemplate JDBC模板对象，用于执行SQL语句
     */
    public ForumSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 应用启动时执行的迁移逻辑
     * 检查当前数据库是否存在，如果存在则执行forum表的迁移操作
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) {
        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.trim().isEmpty()) {
            log.warn("Skip forum schema migration because no active database is selected");
            return;
        }
        if (!tableExists(databaseName, "forum")) {
            log.warn("Skip forum schema migration because table forum does not exist");
            return;
        }

        try {
            migrateForumTable(databaseName);
            log.info("Forum table schema migration finished");
        } catch (Exception ex) {
            log.error("Forum table schema migration failed", ex);
            throw ex;
        }
    }

    /**
     * 迁移forum表结构
     * 检查并添加缺失的字段（imgs、icon、type、manage、update_time）
     *
     * @param databaseName 数据库名称
     */
    private void migrateForumTable(String databaseName) {
        Set<String> columns = loadColumns(databaseName, "forum");

        if (!columns.contains("imgs")) {
            if (columns.contains("img")) {
                executeSql("ALTER TABLE forum CHANGE COLUMN img imgs TEXT NULL COMMENT 'forum images'");
                columns.remove("img");
            } else {
                executeSql("ALTER TABLE forum ADD COLUMN imgs TEXT NULL COMMENT 'forum images' AFTER content");
            }
            columns.add("imgs");
        }

        if (!columns.contains("icon")) {
            executeSql("ALTER TABLE forum ADD COLUMN icon VARCHAR(500) NULL DEFAULT NULL COMMENT 'forum cover' AFTER pass_time");
            columns.add("icon");
        }

        if (!columns.contains("type")) {
            executeSql("ALTER TABLE forum ADD COLUMN type VARCHAR(255) NULL DEFAULT '" + DEFAULT_FORUM_TYPE + "' COMMENT 'forum type' AFTER icon");
            columns.add("type");
        }

        if (!columns.contains("manage")) {
            executeSql("ALTER TABLE forum ADD COLUMN manage VARCHAR(10) NOT NULL DEFAULT '1' COMMENT 'forum status' AFTER type");
            columns.add("manage");
        }

        if (!columns.contains("update_time")) {
            executeSql("ALTER TABLE forum ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER manage");
            columns.add("update_time");
        }

        executeSql("UPDATE forum SET icon = SUBSTRING_INDEX(imgs, ',', 1) WHERE (icon IS NULL OR icon = '') AND imgs IS NOT NULL AND imgs <> ''");
        executeSql("UPDATE forum SET type = '" + DEFAULT_FORUM_TYPE + "' WHERE type IS NULL OR type = ''");
        executeSql("UPDATE forum SET manage = '1' WHERE manage IS NULL OR manage = ''");
    }

    /**
     * 检查指定数据库中是否存在指定的表
     *
     * @param databaseName 数据库名称
     * @param tableName 表名
     * @return 表是否存在
     */
    private boolean tableExists(String databaseName, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?",
                Integer.class,
                databaseName,
                tableName
        );
        return count != null && count > 0;
    }

    /**
     * 加载指定表的所有列名
     *
     * @param databaseName 数据库名称
     * @param tableName 表名
     * @return 列名集合（小写）
     */
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

    /**
     * 执行SQL语句
     *
     * @param sql 要执行的SQL语句
     */
    private void executeSql(String sql) {
        log.info("Execute forum schema SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
}
