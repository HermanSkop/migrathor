package CLI;

import picocli.CommandLine.*;

import java.io.File;

@Command(name = "Migrate", description = "Perform migration operations on the project.")
public class Migrate implements Runnable {
    private final File configFile;

    public Migrate(File configFile) {
        if (!configFile.exists())
            throw new IllegalArgumentException("The specified configuration file does not exist.");
        this.configFile = configFile;
    }

    @Option(names = {"-mv", "--migrate-to-version"}, description = "The version to migrate the configuration file to.")
    private Integer migrateToVersion;

    @Override
    public void run() {
        System.out.println("Migrating " + configFile.getName() + "...");
    }
}
