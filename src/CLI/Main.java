package CLI;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.Scanner;

@Command(name = "Main", description = "Start the application with the specified configuration file.")
public class Main implements Callable<Integer> {
    @Parameters(index = "0", description = "The path to the .properties configuration file.")
    private String configPath;
    private File configFile;

    @Override
    public Integer call() {
        configFile = new File(configPath);
        if (!configFile.exists()) {
            System.err.println("Cannot find the specified configuration file.");
            return 1;
        }

        Scanner scanner = new Scanner(System.in);
        CommandLine cmd = new CommandLine(new Migrate(configFile));
        while (true) {
            String input = scanner.nextLine();

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