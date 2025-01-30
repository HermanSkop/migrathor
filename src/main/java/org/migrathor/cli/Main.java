package org.migrathor.cli;

import ch.qos.logback.classic.Logger;
import org.migrathor.services.migrationservice.MetaMigrationLayer;
import org.migrathor.services.migrationservice.Migration;
import org.migrathor.services.migrationservice.MigrationException;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.*;
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
    public Integer call() throws MigrationException {
        Migration migration = MetaMigrationLayer.getInstance();
        logger.info("Starting the application with the configuration file: {}", configPath);
        migration.init(configPath);

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
                logger.error(e.getMessage());
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}

