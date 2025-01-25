package org.example.services.database;

import java.sql.SQLException;

public interface Database {
    String select(String query) throws SQLException;
}
