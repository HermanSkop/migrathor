package org.migrathor.services.migrationservice;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.database.DatabaseService;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MetaMigrationLayer implements Migration{
    private MigrationService migrationService;
    private DatabaseService databaseService;
    private int currentVersion;
    private static MetaMigrationLayer metaMigrationLayer;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(MetaMigrationLayer.class);


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
        if (metaMigrationLayer == null) {
            metaMigrationLayer = new MetaMigrationLayer();
        }
        return metaMigrationLayer;
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
    public void migrateToVersion(int version) throws IOException, SQLException, MigrationException {
        logger.info("Migrating to version: {}", version);
        if (isVersionMigrated(version)) {
            logger.warn("Version {} already migrated with the same checksum. Skipping the migration", version);
            return;
        }

        migrationService.migrateToVersion(version);
        updateMetadataVersion(version, generateChecksum(migrationService.getScript(version, ScriptType.DO)));

        logger.info("Migration to version {} completed successfully.", version);
    }

    private void updateMetadataVersion(int version, int checksum) throws SQLException {
        databaseService.executeTransactionalQuery("""
                DELETE FROM metadata WHERE version = %d;
                
                INSERT INTO metadata (version, checksum, name)
                VALUES (%d, %d, '%s');
                """.formatted(version, version, checksum, "v" + version + ".sql"));
        updateCurrentVersion();
    }
    private void removeMetadataVersion(int version) throws SQLException {
        databaseService.executeTransactionalQuery("""
                DELETE FROM metadata WHERE version = %d;
                """.formatted(version));
        updateCurrentVersion();
    }

    private void createMetadataTable() throws SQLException {
        databaseService.executeTransactionalQuery("""
                CREATE TABLE IF NOT EXISTS metadata (
                    version INT PRIMARY KEY,
                    checksum INT NOT NULL,
                    name VARCHAR(255) NOT NULL
                );
                """);
    }
    private boolean isVersionMigrated(int version) throws SQLException, IOException {
        return getChecksum(currentVersion) == migrationService.getScript(version, ScriptType.DO).hashCode();
    }
    private int getChecksum(int version) throws SQLException {
        try (ResultSet result = databaseService.executeSelectQuery("""
                SELECT checksum FROM metadata WHERE version = %d;
                """.formatted(version))) {
            boolean exists = result.next();
            if (exists) {
                return result.getInt(1);
            }
        }
        return 1;
    }

    private void updateCurrentVersion() throws SQLException {
        try (ResultSet result = databaseService.executeSelectQuery("SELECT MAX(version) FROM metadata")) {
            if (result.next())
                currentVersion = result.getInt(1);
            else
                throw new SQLException("No version found");
        }
    }

    @Override
    public void undoMigration(int version){
        logger.info("Undoing migration to version: {}", version);
        try {
            removeMetadataVersion(version);
            try {
                migrationService.undoMigration(version);
            } catch (Exception e) {
                try {
                    updateMetadataVersion(version, getChecksum(version));
                } catch (SQLException sqlException) {
                    logger.error("Failed to update metadata version after undoing migration: {} (restoration is needed)", sqlException.getMessage());
                    throw sqlException;
                }
                throw e;
            }

            logger.info("Undoing migration to version {} completed successfully.", version);
        } catch (SQLException e) {
            logger.error("Error undoing migration to version {}: {}", version, e.getMessage());
        }
    }

    private int generateChecksum(String script) {
        return script.hashCode();
    }
}
