package org.example.services.database;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private DatabaseService() throws SQLException {}

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
        connection.createStatement().execute(query);
        releaseConnection(connection);
    }

    public String executeSelectQuery(String query) throws SQLException {
        Connection connection = getConnection();
        try {
            return connection.createStatement().executeQuery(query).toString();
        } finally {
            releaseConnection(connection);
        }
    }

    private String executeSelectQuery(Connection connection, String query) throws SQLException {
        return connection.createStatement().executeQuery(query).toString();
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
        logger.info("Connections in use: {}", connectionsInUse.size());
        logger.info("Connections available: {}", connectionPool.size());
        return connection;
    }

    private void releaseConnection(Connection connection) throws SQLException {
        connectionsInUse.remove(connection);
        connectionPool.add(connection);

        while (connectionPool.size() > CONNECTIONS) {
            connection = connectionPool.removeLast();
            connection.close();
        }

        logger.info("Connections in use: {}", connectionsInUse.size());
        logger.info("Connections available: {}", connectionPool.size());
    }

    public void executeTransactionalSelectQuery(String query) {
        executeTransaction(this::executeSelectQuery, query);
    }

    public void executeTransactionalQuery(String query) {
        executeTransaction(this::executeSelectQuery, query);
    }

    private void executeTransaction(TransactionalConsumer<String> transactionalLogic, String query) {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            transactionalLogic.accept(query);

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
