package org.migrathor.cli;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.migrationservice.MetaMigrationLayer;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "state", description = "Perform a migration operation")
public class StateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(StateCommand.class);
    @Override
    public void run() {
        try {
            System.out.println("Current state: " + MetaMigrationLayer.getInstance());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
