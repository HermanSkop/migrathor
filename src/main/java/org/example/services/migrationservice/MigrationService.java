package org.example.services.migrationservice;

import ch.qos.logback.classic.Logger;
import org.example.services.database.DatabaseService;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Properties;

public class MigrationService implements Migration{
    static final Logger logger = (Logger) LoggerFactory.getLogger(MigrationService.class);
    private static MigrationService migration;

    private DatabaseService database;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String scriptsPath;

    static MigrationService getInstance() {
        if (migration == null) migration = new MigrationService();
        return migration;
    }

    public void init(String configPath) throws IllegalArgumentException, IOException, SQLException {
        parseConfig(configPath);
        connectDb();
        logger.info("Migration initialized with the following configuration: {}", this);
    }

    String getScript(int version) throws IOException {
        File scriptFile = new File(scriptsPath + "/v" + version + ".sql");
        logger.info("Reading the script file: {}", scriptFile.getAbsolutePath());
        if (!scriptFile.exists())
            throw new IllegalArgumentException("The specified script does not exist.");
        try {
            return new String(Files.readAllBytes(scriptFile.toPath()));
        } catch (IOException e) {
            throw new IOException("Error reading the script file v" + version + ":" + e.getMessage());
        }
    }

    private void connectDb() throws SQLException {
        database = DatabaseService.getDatabaseService();
        database.init(dbUrl, dbUser, dbPassword);
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
    public void migrateToVersion(int version) {
        try {
            String script = getScript(version);
            database.executeQuery(script);
            logger.info("Successfully migrated to version {}", version);
        } catch (IOException | SQLException e) {
            logger.error("Error migrating to version {}: {}", version, e.getMessage());
        }
    }
}
