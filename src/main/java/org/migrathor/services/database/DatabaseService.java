package org.migrathor.services.database;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService databaseService;
    private ConnectionPool connectionPool;

    private DatabaseService() throws SQLException {
    }

    public void init(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        connectionPool = new ConnectionPool(dbUrl, dbUser, dbPassword);
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        Connection connection = connectionPool.getConnection();
        try {
            return connection.createStatement().executeQuery(query);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }

    private void executeQuery(Connection connection, String query) throws SQLException {
        connection.createStatement().execute(query);
    }

    public void executeTransactionalQuery(String query) throws SQLException {
        executeTransaction(this::executeQuery, query);
    }

    private void executeTransaction(TransactionalConsumer<Connection, String> transactionalLogic, String query) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
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
            throw new SQLException("Transaction failed. Cause: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connectionPool.releaseConnection(connection);
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
