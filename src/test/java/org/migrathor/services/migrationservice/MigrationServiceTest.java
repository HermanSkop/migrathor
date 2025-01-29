package org.migrathor.services.migrationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.migrathor.services.database.ConnectionPool;
import org.migrathor.services.database.DatabaseService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {
    private final MigrationService migrationService = MigrationService.getInstance();

    @Mock private DatabaseService databaseService;

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException, MigrationException {
        migrationService.init("test.properties");

        Field field = MigrationService.class.getDeclaredField("databaseService");
        field.setAccessible(true);
        field.set(migrationService, databaseService);
    }

    @Test
    void migrateToVersion_success() throws SQLException {
        doNothing().when(databaseService).executeTransactionalQuery(anyString());
        assertDoesNotThrow(() -> migrationService.migrateToVersion(2));
        verify(databaseService, times(1)).executeTransactionalQuery(anyString());
    }

    @Test
    void migrateToVersion_failsWhenDbError() throws SQLException {
        doThrow(new SQLException()).when(databaseService).executeTransactionalQuery(anyString());
        assertThrows(MigrationException.class, () -> migrationService.migrateToVersion(2));
        verify(databaseService, times(1)).executeTransactionalQuery(anyString());
    }

    @Test
    void undoMigration_success() throws SQLException {
        doNothing().when(databaseService).executeTransactionalQuery(anyString());
        assertDoesNotThrow(() -> migrationService.undoMigration(2));
        verify(databaseService, times(1)).executeTransactionalQuery(anyString());
    }

    @Test
    void undoMigration_failsWhenDbError() throws SQLException {
        doThrow(new SQLException()).when(databaseService).executeTransactionalQuery(anyString());
        assertThrows(MigrationException.class, () -> migrationService.undoMigration(2));
        verify(databaseService, times(1)).executeTransactionalQuery(anyString());
    }
}
