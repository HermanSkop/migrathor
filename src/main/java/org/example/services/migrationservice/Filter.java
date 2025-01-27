package org.example.services.migrationservice;

import ch.qos.logback.classic.Logger;
import org.example.services.database.DatabaseService;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Filter implements Migration{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Filter.class);
    private MigrationService migrationService;
    private DatabaseService databaseService;
    private int currentVersion;
    private static Filter filter;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Filter.class);

    public void init(String configPath) throws SQLException, IOException, IllegalArgumentException {
        logger.info("Initializing migration with the following configuration: {}", configPath);
        migrationService = MigrationService.getInstance();
        migrationService.init(configPath);
        databaseService = DatabaseService.getDatabaseService();
        createMetadataTable();
        updateCurrentVersion();
        logger.info("Initialized migration with the following configuration: {}", this);
    }

    public static Migration getInstance() {
        if (filter == null) {
            filter = new Filter();
        }
        return filter;
    }

    @Override
    public String toString() {
        return migrationService.toString() + """
                Filter{
                    currentVersion: %d
                }
                """.formatted(currentVersion);
    }

    @Override
    public void migrateToVersion(int version) throws IOException, SQLException {
        logger.info("Migrating to version: {}", version);
        if (isVersionMigrated(version)) {
            logger.warn("Version {} already migrated. Skipping the migration", version);
            return;
        }

        migrationService.migrateToVersion(version);
        updateMetadata(version, getChecksum(version));

        logger.info("Migration to version {} completed successfully.", version);
    }

    private void updateMetadata(int version, int checksum) throws SQLException {
        databaseService.executeTransactionalQuery("""
                DELETE FROM metadata WHERE version = %d;
                
                INSERT INTO metadata (version, checksum, name)
                VALUES (%d, %d, '%s');
                """.formatted(version, version, checksum, "v" + version + ".sql"));
        updateCurrentVersion();
    }
    private void createMetadataTable() throws SQLException {
        databaseService.executeQuery("""
                CREATE TABLE IF NOT EXISTS metadata (
                    version INT PRIMARY KEY,
                    checksum INT NOT NULL,
                    name VARCHAR(255) NOT NULL
                );
                """);
    }
    private boolean isVersionMigrated(int version) throws SQLException, IOException {
        return getChecksum(currentVersion) == migrationService.getScript(version).hashCode();
    }

    private int getChecksum(int version) throws SQLException {
        try (ResultSet result = databaseService.executeSelectQuery("""
                SELECT checksum FROM metadata WHERE version = %d;
                """.formatted(version))) {
            if (result.next()) {
                return result.getInt(1);
            }
        }
        return 1;
    }

    private int getCurrentVersion() {
        return currentVersion;
    }

    private void updateCurrentVersion() throws SQLException {
        try (ResultSet result = databaseService.executeSelectQuery("SELECT MAX(version) FROM metadata")) {
            if (result.next())
                currentVersion = result.getInt(1);
            else
                throw new SQLException("No version found");
        }
    }
}
