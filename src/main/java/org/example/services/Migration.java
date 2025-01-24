package org.example.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Migration {
    private static Migration migration;
    private File configFile;
    private Migration() {}
    private Properties properties = new Properties();

    private Migration(File configFile) {
        try {
            properties.load(new FileInputStream(configFile));
        } catch (IOException e) {

        }
    }

    public static Migration getMigration() {
        if (migration == null) migration = new Migration();
        return migration;
    }

    public void setConfig(String configPath) throws IllegalArgumentException{
        File tempFile = new File(configPath);
        if (!tempFile.exists())
            throw new IllegalArgumentException("The specified file does not exist.");
        this.configFile = tempFile;
    }

    public File getConfigFile() {
        return new File(configFile.getAbsolutePath());
    }


}
