package org.example.cli;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "migrate", description = "Perform a migration operation")
class MigrateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(MigrateCommand.class);

    @Option(names = {"-v", "--migrate-to-version"}, description = "The version to migrate the configuration file to.")
    private Integer version;

    @Override
    public void run() {
        try {
            System.out.println("Migrating to version " + version + "...");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
