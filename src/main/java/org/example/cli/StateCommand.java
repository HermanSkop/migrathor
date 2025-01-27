package org.example.cli;

import ch.qos.logback.classic.Logger;
import org.example.services.migrationservice.Filter;
import org.example.services.migrationservice.MigrationService;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "state", description = "Perform a migration operation")
public class StateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(StateCommand.class);
    @Override
    public void run() {
        try {
            System.out.println("Current state: " + Filter.getInstance());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
