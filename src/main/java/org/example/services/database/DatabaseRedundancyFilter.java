package org.example.services.database;

import java.sql.SQLException;

public class DatabaseRedundancyFilter implements Database{
    private Database databaseService;

    public DatabaseRedundancyFilter(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        databaseService = new DatabaseService(dbUrl, dbUser, dbPassword);
    }

    public String select(String query) throws SQLException {
        return databaseService.select(query);
    }
}
