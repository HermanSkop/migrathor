package org.example.services.database;

import java.sql.SQLException;

public class DatabaseRedundancyFilter implements Database {
    private final Database databaseService;

    public DatabaseRedundancyFilter(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        databaseService = new DatabaseService(dbUrl, dbUser, dbPassword);
        createMetadataTable();
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

    @Override
    public String executeSelectQuery(String query) throws SQLException {
        return databaseService.executeSelectQuery(query);
    }

    @Override
    public void executeQuery(String query) throws SQLException {
        databaseService.executeQuery(query);
    }

    @Override
    public void executeTransactionalSelectQuery(String query) throws SQLException {
        databaseService.executeTransactionalSelectQuery(query);
    }
}
