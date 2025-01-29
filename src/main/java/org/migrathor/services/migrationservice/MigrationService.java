package org.migrathor.services.migrationservice;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.database.DatabaseService;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Properties;

public class MigrationService implements Migration {
    static final Logger logger = (Logger) LoggerFactory.getLogger(MigrationService.class);
    private static MigrationService migration;

    private DatabaseService databaseService;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String scriptsPath;

    static MigrationService getInstance() {
        if (migration == null) migration = new MigrationService();
        return migration;
    }

    @Override
    public void init(String configPath) throws MigrationException {
        parseConfig(configPath);
        try {
            connectDb();
        } catch (IllegalStateException e) {
            destroy();
            throw new MigrationException(e.getMessage());
        }
        logger.info("Migration initialized with the following configuration: {}", this);
    }

    public static void destroy() {
        if (migration != null) {
            DatabaseService.destroy();
            migration = null;
        }
    }

    String getScript(int version, ScriptType type) throws MigrationException {
        File scriptFile = new File(scriptsPath + "/" + type + version + ".sql");
        logger.info("Reading the script file: {}", scriptFile.getAbsolutePath());
        if (!scriptFile.exists())
            throw new IllegalArgumentException("The specified script does not exist at " + scriptFile.getAbsolutePath());
        try {
            return new String(Files.readAllBytes(scriptFile.toPath()));
        } catch (IOException e) {
            throw new MigrationException("Unable to read the script file: " + e.getMessage());
        }
    }

    private void connectDb() throws IllegalStateException{
        try {
            databaseService = DatabaseService.getDatabaseService();
            databaseService.init(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new IllegalStateException("Error connecting to the database: " + e.getMessage());
        }
    }

    private void parseConfig(String configPath) throws IllegalArgumentException {
        File file = new File(configPath);
        logger.info("Reading the configuration file: {}", file.getAbsolutePath());
        if (!file.exists())
            throw new IllegalArgumentException("The specified configuration file does not exist at " + configPath);
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));

            dbUrl = properties.getProperty("db.url");
            dbUser = properties.getProperty("db.user");
            dbPassword = properties.getProperty("db.password");
            scriptsPath = properties.getProperty("scripts.dir");
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading the configuration file: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return """
                Migration{
                    dbUrl='%s',
                    dbUser='%s',
                    dbPassword='%s'
                }
                """.formatted(dbUrl, dbUser, dbPassword);
    }

    @Override
    public void migrateToVersion(int version) throws MigrationException {
        try {
            String script = getScript(version, ScriptType.DO);
            databaseService.executeTransactionalQuery(script);
            logger.info("Successfully migrated to version {}", version);
        } catch (SQLException e) {
            throw new MigrationException("Error migrating to version " + version + ": " + e.getMessage());
        }
    }

    @Override
    public void undoMigration(int version) throws MigrationException {
        try {
            String script = getScript(version, ScriptType.UNDO);
            databaseService.executeTransactionalQuery(script);
            logger.info("Successfully undone migration of version {}", version);
        } catch (SQLException e) {
            throw new MigrationException("Error undoing migration of version " + version + ": " + e.getMessage());
        }
    }
}
