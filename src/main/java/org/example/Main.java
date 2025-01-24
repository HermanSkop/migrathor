package org.example;

import org.example.services.Migration;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.Scanner;

@Command(
        name = "Main",
        description = "Start the application with the specified configuration file.",
        subcommands = {MigrateCommand.class}
)
public class Main implements Callable<Integer> {
    @Parameters(index = "0", description = "The path to the .properties configuration file.")
    private String configPath;

    @Override
    public Integer call() {
        Migration migration = Migration.getMigration();
        try {
            migration.setConfig(configPath);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(this);
        while (true) {
            String input = configPath + " " + scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("(×_×)");
                break;
            }
            try {
                cmd.execute(input.split(" "));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}

@Command(name = "migrate", description = "Perform a migration operation")
class MigrateCommand implements Runnable {

    @Option(names = {"-mv", "--migrate-to-version"}, description = "The version to migrate the configuration file to.")
    private Integer migrateToVersion;

    @Override
    public void run() {
        try {

            System.out.println("Migrating to version " + migrateToVersion + "...");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
