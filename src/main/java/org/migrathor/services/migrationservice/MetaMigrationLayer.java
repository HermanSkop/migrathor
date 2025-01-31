package org.migrathor.services.migrationservice;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.database.DatabaseService;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class MetaMigrationLayer implements Migration {
    private MigrationService migrationService;
    private DatabaseService databaseService;
    private int currentVersion;
    private static MetaMigrationLayer metaMigration;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(MetaMigrationLayer.class);

    private MetaMigrationLayer() {
    }

    @Override
    public void init(String configPath) throws MigrationException {
        logger.info("Initializing migration with the following configuration: {}", configPath);
        try {
            migrationService = MigrationService.getInstance();
            migrationService.init(configPath);
            databaseService = DatabaseService.getDatabaseService();
            createMetadataTable();
            updateCurrentVersion();
        } catch (MigrationException e) {
            destroy();
            throw e;
        }
        logger.info("Initialized migration with the following configuration: {}", this);
    }

    public static void destroy() {
        if (metaMigration != null) {
            DatabaseService.destroy();
            metaMigration = null;
        }
    }

    public static MetaMigrationLayer getInstance() {
        if (metaMigration == null) {
            metaMigration = new MetaMigrationLayer();
        }
        return metaMigration;
    }

    @Override
    public String toString() {
        updateCurrentVersion();
        return migrationService.toString() +
                """
                        Filter{
                            currentVersion: %d,
                            versions: %s
                        }
                        """.formatted(currentVersion, getAllVersions());
    }

    private List<Integer> getAllVersions() {
        try (ResultSet result = databaseService.executeSelectQuery("SELECT version FROM metadata ORDER BY version")) {
            List<Integer> versions = new ArrayList<>();
            while (result.next()) {
                versions.add(result.getInt("version"));
            }
            return versions;
        } catch (SQLException e) {
            if (e.getMessage().contains("relation \"metadata\" does not exist")) {
                createMetadataTable();
                return getAllVersions();
            } else throw new RuntimeException(e);
        }
    }

    @Override
    public void migrateToVersion(int version) throws MigrationException {
        logger.info("Migrating to version: {}", version);
        verifyVersionNotMigrated(version);

        migrationService.migrateToVersion(version);
        updateMetadataVersion(version, generateChecksum(migrationService.getScript(version, ScriptType.DO)));

        logger.info("Migration to version {} completed successfully.", version);
    }

    private void updateMetadataVersion(int version, String checksum) {
        try {
            databaseService.executeTransactionalQuery("""
                    DELETE FROM metadata WHERE version = %d;
                    
                    INSERT INTO metadata (version, checksum, name)
                    VALUES (%d, '%s', '%s');
                    """.formatted(version, version, checksum, "v" + version + ".sql"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        updateCurrentVersion();
    }

    private void removeMetadataVersion(int version) {
        try {
            databaseService.executeTransactionalQuery("""
                    DELETE FROM metadata WHERE version = %d;
                    """.formatted(version));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        updateCurrentVersion();
    }

    private void createMetadataTable() {
        try {
            databaseService.executeTransactionalQuery("""
                    CREATE TABLE IF NOT EXISTS metadata (
                        version INT PRIMARY KEY,
                        checksum VARCHAR(255) NOT NULL,
                        name VARCHAR(255) NOT NULL
                    );
                    """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyVersionNotMigrated(int version) throws MigrationException {
        boolean sameChecksum = isChecksumMigrated(version);
        if (currentVersion == version) {
            if (sameChecksum) {
                throw new MigrationException("Version " + version + " is already migrated with the same checksum.");
            } else {
                throw new MigrationException("Version " + version + " is already migrated but with a different checksum.");
            }
        }
        if (currentVersion > version) {
            if (sameChecksum) {
                throw new MigrationException("Given version " + version + " is older than the current version " + currentVersion + " and has the same checksum.");
            } else {
                throw new MigrationException("Given version " + version + " is older than the current version " + currentVersion);
            }
        }
    }

    private boolean isChecksumMigrated(int version) throws MigrationException {
        return getMetaChecksum(currentVersion).equals(generateChecksum(migrationService.getScript(version, ScriptType.DO)));
    }

    /**
     * Fetches the checksum of the specified version from the metadata table
     *
     * @param version the version to fetch the checksum for
     * @return the checksum of the specified version (0 if the version does not exist)
     */
    private String getMetaChecksum(int version) {
        try (ResultSet result = databaseService.executeSelectQuery("""
                SELECT checksum FROM metadata WHERE version = %d;
                """.formatted(version))) {
            if (result.next())
                return result.getString(1);
            else
                return "";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fetches the current version from the metadata table and updates the currentVersion field
     */
    private void updateCurrentVersion() {
        try (ResultSet result = databaseService.executeSelectQuery("SELECT MAX(version) FROM metadata")) {
            if (result.next()) currentVersion = result.getInt(1);
            else throw new SQLException("No version found");
        } catch (SQLException e) {
            if (e.getMessage().contains("relation \"metadata\" does not exist")) {
                createMetadataTable();
                updateCurrentVersion();
            } else throw new RuntimeException(e);
        }
    }

    @Override
    public void undoMigration(int version) throws MigrationException {
        logger.info("Undoing migration from version: {}", version);
        migrationService.undoMigration(version);
        removeMetadataVersion(version);
        logger.info("Undoing migration from version {} completed successfully.", version);
    }

    private String generateChecksum(String script) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(script.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
