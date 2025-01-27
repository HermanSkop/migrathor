package org.example.cli;

import ch.qos.logback.classic.Logger;
import org.example.services.migrationservice.Filter;
import org.example.services.migrationservice.Migration;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.Scanner;

@Command(
        name = "Main",
        description = "Start the application with the specified configuration file.",
        subcommands = {MigrateCommand.class, StateCommand.class}
)
public class Main implements Callable<Integer> {
    @Parameters(index = "0", description = "The path to the .properties configuration file.")
    private String configPath;
    final static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

    @Override
    public Integer call() {
        Migration migration = Filter.getInstance();
        logger.info("Starting the application with the configuration file: {}", configPath);
        try {
            migration.init(configPath);
        } catch (IllegalArgumentException | IOException | SQLException e) {
            logger.error(e.getMessage(), e);
            return 1;
        }

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);
        while (true) {
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("(×_×)");
                break;
            }
            try {
                cmd.execute((configPath + " " + input).split(" "));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}

