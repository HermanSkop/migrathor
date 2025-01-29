package org.migrathor.services.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DatabaseServiceTest {
    @Mock private Connection connection;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        ConnectionPool mockPool = mock(ConnectionPool.class);
        when(mockPool.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        databaseService = DatabaseService.getDatabaseService();

        Field field = DatabaseService.class.getDeclaredField("connectionPool");
        field.setAccessible(true);
        field.set(databaseService, mockPool);
    }

    @Test
    void testExecuteSelectQuery() throws SQLException {
        String query = "SELECT * FROM test_table";
        ResultSet result = databaseService.executeSelectQuery(query);

        assertNotNull(result);
        verify(statement).executeQuery(query);
    }

    @Test
    void testExecuteTransactionalQuery_Success() throws SQLException {
        String query = "INSERT INTO test_table (name, value) VALUES ('test', 1)";
        when(statement.execute(query)).thenReturn(true);

        assertDoesNotThrow(() -> databaseService.executeTransactionalQuery(query));

        verify(connection).setAutoCommit(false);
        verify(statement).execute(query);
        verify(connection).commit();
    }

    @Test
    void testExecuteTransactionalQuery_Failure() throws SQLException {
        String query = "INSERT INTO test_table (name, value) VALUES ('test', 1)";
        when(statement.execute(query)).thenThrow(new SQLException("Test exception"));

        assertThrows(SQLException.class, () -> databaseService.executeTransactionalQuery(query));
        verify(connection).rollback();
    }

    @Test
    void testGetDatabaseService_Singleton() throws SQLException {
        DatabaseService instance1 = DatabaseService.getDatabaseService();
        DatabaseService instance2 = DatabaseService.getDatabaseService();

        assertSame(instance1, instance2);
    }
}