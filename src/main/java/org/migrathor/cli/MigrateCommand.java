package org.migrathor.cli;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.migrationservice.MetaMigrationLayer;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "migrate", description = "Perform a migration operation")
class MigrateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(MigrateCommand.class);

    @Option(names = {"-v", "--version"}, description = "The version to migrate the configuration file to.")
    private int version;

    @Option(names = {"-u", "--undo-version"}, description = "Perform undo instead of migration.")
    private boolean undo;

    @Override
    public void run() {
        try {
            if (undo) MetaMigrationLayer.getInstance().undoMigration(version);
            else MetaMigrationLayer.getInstance().migrateToVersion(version);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
