package org.migrathor.services.migrationservice;

import java.io.IOException;
import java.sql.SQLException;

public interface Migration {
    void init(String configPath) throws MigrationException;
    String toString();
    void migrateToVersion(int version) throws MigrationException;
    void undoMigration(int version) throws MigrationException;
}
