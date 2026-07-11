package com.example.view.util;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (Exception e) {
            System.err.println("WARNING: config.properties file not found in the root directory!");
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}