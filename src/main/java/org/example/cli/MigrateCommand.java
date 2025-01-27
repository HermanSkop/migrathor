package org.example.cli;

import ch.qos.logback.classic.Logger;
import org.example.services.migrationservice.Filter;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "migrate", description = "Perform a migration operation")
class MigrateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(MigrateCommand.class);

    @Option(names = {"-v", "--migrate-to-version"}, description = "The version to migrate the configuration file to.")
    private int version;

    @Override
    public void run() {
        try {
            logger.info("Migrating to version: {}", version);
            Filter.getInstance().migrateToVersion(version);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
