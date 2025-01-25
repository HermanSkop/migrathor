package org.example.services.database;

import java.sql.SQLException;

public interface Database {
    String executeSelectQuery(String query) throws SQLException;
    void executeQuery(String query) throws SQLException;
    void executeTransactionalSelectQuery(String query) throws SQLException;
}
