package org.example.services.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService implements Database {
    private List<Connection> connectionPool = new ArrayList<>();
    private List<Connection> connectionsInUse = new ArrayList<>();
    private static final int CONNECTIONS = 5;
    private static final int MAX_CONNECTIONS = 15;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public DatabaseService(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        for (int i = 0; i < CONNECTIONS; i++) {
            connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
        }
    }

    public String select(String query) throws SQLException {
        Connection connection = getConnection();
        try {
            return connection.createStatement().executeQuery(query).toString();
        } finally {
            releaseConnection(connection);
        }
    }

    private Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if (connectionsInUse.size() < MAX_CONNECTIONS) {
                connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
            }
            throw new RuntimeException("No connections available.");
        }
        Connection connection = connectionPool.removeLast();
        connectionsInUse.add(connection);
        return connection;
    }

    private void releaseConnection(Connection connection) throws SQLException {
        connectionsInUse.remove(connection);
        connectionPool.add(connection);

        while (connectionPool.size() > CONNECTIONS) {
            connection = connectionPool.removeLast();
            connection.close();
        }
    }
}
