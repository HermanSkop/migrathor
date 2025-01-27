package org.example.services.migrationservice;

import java.io.IOException;
import java.sql.SQLException;

public interface Migration {
    void init(String configPath) throws IllegalArgumentException, IOException, SQLException;
    String toString();
    void migrateToVersion(int version) throws IOException, SQLException;
}
