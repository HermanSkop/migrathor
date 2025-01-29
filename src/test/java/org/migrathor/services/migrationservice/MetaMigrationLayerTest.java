package org.migrathor.services.migrationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.migrathor.services.database.DatabaseService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaMigrationLayerTest {
    private final MetaMigrationLayer metaMigrationLayer = MetaMigrationLayer.getInstance();
    @Mock private MigrationService migrationService;
    @Mock private DatabaseService databaseService;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws MigrationException, NoSuchFieldException, IllegalAccessException, SQLException {
        metaMigrationLayer.init("test.properties");

        Field migrationServiceField = MetaMigrationLayer.class.getDeclaredField("migrationService");
        migrationServiceField.setAccessible(true);
        migrationServiceField.set(metaMigrationLayer, migrationService);

        Field databaseServiceField = MetaMigrationLayer.class.getDeclaredField("databaseService");
        databaseServiceField.setAccessible(true);
        databaseServiceField.set(metaMigrationLayer, databaseService);
        }

    @Test
    void migrateToVersion_success() throws MigrationException, SQLException {
        when(databaseService.executeSelectQuery(anyString())).thenReturn(resultSet);
        doReturn(1).when(resultSet).getInt(1);
        when(resultSet.next()).thenReturn(true);
        when(migrationService.getScript(anyInt(), any())).thenReturn("script");


        assertDoesNotThrow(() -> metaMigrationLayer.migrateToVersion(2));
        verify(migrationService, times(1)).migrateToVersion(2);
    }

    @Test
    void migrateToVersion_failsWhenDbError() throws MigrationException, SQLException {
        when(databaseService.executeSelectQuery(anyString())).thenReturn(resultSet);
        doReturn(1).when(resultSet).getInt(1);
        when(resultSet.next()).thenReturn(true);
        when(migrationService.getScript(anyInt(), any())).thenReturn("script");
        doThrow(new SQLException()).when(databaseService).executeTransactionalQuery(anyString());

        assertThrows(RuntimeException.class, () -> metaMigrationLayer.migrateToVersion(2));
        verify(migrationService, times(1)).migrateToVersion(2);
    }

    @Test
    void undoMigration_success() throws MigrationException, SQLException {
        doReturn(1).when(resultSet).getInt(1);
        when(resultSet.next()).thenReturn(true);
        when(databaseService.executeSelectQuery(anyString())).thenReturn(resultSet);

        assertDoesNotThrow(() -> metaMigrationLayer.undoMigration(2));
        verify(migrationService, times(1)).undoMigration(2);
    }

    @Test
    void undoMigration_failsWhenDbError() throws MigrationException, SQLException {
        doThrow(new SQLException()).when(databaseService).executeTransactionalQuery(anyString());

        assertThrows(RuntimeException.class, () -> metaMigrationLayer.undoMigration(2));
        verify(migrationService, times(1)).undoMigration(2);
    }
}