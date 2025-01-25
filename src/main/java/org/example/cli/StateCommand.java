package org.example.cli;

import ch.qos.logback.classic.Logger;
import org.example.services.Migration;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.*;

@Command(name = "state", description = "Perform a migration operation")
public class StateCommand implements Runnable {
    final static Logger logger = (Logger) LoggerFactory.getLogger(StateCommand.class);
    @Override
    public void run() {
        try {
            System.out.println("Current state: " + Migration.getMigration());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
