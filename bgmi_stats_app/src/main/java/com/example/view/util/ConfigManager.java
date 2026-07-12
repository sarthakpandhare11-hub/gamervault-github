package com.example.view.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();

    static {
        String currentDir = System.getProperty("user.dir");

        // 1st Check: Look in the immediate working directory
        File configFile = new File(currentDir, "config.properties");

        // 2nd Check: If it isn't there, look inside the bgmi_stats_app folder
        if (!configFile.exists()) {
            configFile = new File(currentDir, "bgmi_stats_app" + File.separator + "config.properties");
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            System.out.println("✅ Config loaded successfully from: " + configFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("==================================================");
            System.err.println("❌ ERROR: config.properties file not found!");
            System.err.println(
                    "❌ Java tried looking here: " + new File(currentDir, "config.properties").getAbsolutePath());
            System.err.println(
                    "❌ And also here: " + new File(currentDir, "bgmi_stats_app/config.properties").getAbsolutePath());
            System.err.println("❌ Please place your config.properties file in one of those locations.");
            System.err.println("==================================================");
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}