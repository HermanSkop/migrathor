package org.example.services.migrationservice;

import org.example.services.database.DatabaseService;

import java.io.IOException;
import java.sql.SQLException;

public class Filter implements Migration{
    MigrationService migrationService;
    DatabaseService databaseService;

    @Override
    public void init(String configPath) throws SQLException, IOException, IllegalArgumentException {
        migrationService = MigrationService.getMigration();
        migrationService.init(configPath);
        databaseService = DatabaseService.getDatabaseService();
        createMetadataTable();
    }

    @Override
    public String toString() {
        return migrationService.toString();
    }

    private void createMetadataTable() throws SQLException {
        databaseService.executeQuery("""
                CREATE TABLE IF NOT EXISTS metadata (
                    version INT PRIMARY KEY,
                    checksum INT NOT NULL,
                    name VARCHAR(255)
                );
                """);
    }
}
