package org.migrathor.services.database;


import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private final List<Connection> connectionPool = new ArrayList<>();
    private final List<Connection> connectionsInUse = new ArrayList<>();
    private static final int CONNECTIONS = 5;
    private static final int MAX_CONNECTIONS = 15;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConnectionPool.class);

    ConnectionPool(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        for (int i = 0; i < CONNECTIONS; i++) {
            connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
        }
    }

    public Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if (connectionsInUse.size() < MAX_CONNECTIONS) {
                connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
                logger.info("Created a new connection.");
            }
            throw new RuntimeException("No connections available.");
        }
        Connection connection = connectionPool.removeLast();
        connectionsInUse.add(connection);
        return connection;
    }

    public void releaseConnection(Connection connection) throws SQLException {
        connectionsInUse.remove(connection);
        connectionPool.add(connection);

        while (connectionPool.size() > CONNECTIONS) {
            connection = connectionPool.removeLast();
            connection.close();
        }
    }

    public void closeAllConnections() {
        for (Connection connection : connectionPool) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close the connection.", e);
            }
        }
        for (Connection connection : connectionsInUse) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Failed to close the connection.", e);
            }
        }
    }
}
