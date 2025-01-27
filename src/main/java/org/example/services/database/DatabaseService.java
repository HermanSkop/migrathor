package org.example.services.database;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private final List<Connection> connectionPool = new ArrayList<>();
    private final List<Connection> connectionsInUse = new ArrayList<>();
    private static final int CONNECTIONS = 5;
    private static final int MAX_CONNECTIONS = 15;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private static final Logger logger = (Logger) LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService databaseService;

    private DatabaseService() throws SQLException {
    }

    public void init(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        for (int i = 0; i < CONNECTIONS; i++) {
            connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
        }
    }

    public void executeQuery(String query) throws SQLException {
        Connection connection = getConnection();
        try {
            connection.createStatement().execute(query);
        } finally {
            releaseConnection(connection);
        }
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        Connection connection = getConnection();
        try {
            return connection.createStatement().executeQuery(query);
        } finally {
            releaseConnection(connection);
        }
    }

    private void executeQuery(Connection connection, String query) throws SQLException {
        connection.createStatement().execute(query);
    }

    private Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if (connectionsInUse.size() < MAX_CONNECTIONS) {
                connectionPool.add(DriverManager.getConnection(dbUrl, dbUser, dbPassword));
                logger.info("Created a new connection.");
            }
            throw new RuntimeException("No connections available.");
        }
        Connection connection = connectionPool.removeLast();
        connectionsInUse.add(connection);
        logger.info("Connection acquired.");
        logger.info(connectionsInUse.size() + "/" + connectionPool.size());
        return connection;
    }

    private void releaseConnection(Connection connection) throws SQLException {
        connectionsInUse.remove(connection);
        connectionPool.add(connection);

        while (connectionPool.size() > CONNECTIONS) {
            connection = connectionPool.removeLast();
            connection.close();
        }

        logger.info("Connection released.");
        logger.info(connectionsInUse.size() + "/" + connectionPool.size());
    }

    public void executeTransactionalQuery(String query) {
        executeTransaction(this::executeQuery, query);
    }

    private void executeTransaction(TransactionalConsumer<Connection, String> transactionalLogic, String query) {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            transactionalLogic.accept(connection, query);

            connection.commit();
            logger.info("Transaction committed.");
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    logger.warn("Transaction rolled back.");
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to roll back transaction.", rollbackEx);
                }
            }
            throw new RuntimeException("Transaction failed.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    releaseConnection(connection);
                } catch (SQLException closeEx) {
                    logger.error("Failed to reset connection.", closeEx);
                }
            }
        }
    }

    public static DatabaseService getDatabaseService() throws SQLException {
        if (databaseService == null) databaseService = new DatabaseService();
        return databaseService;
    }

}
