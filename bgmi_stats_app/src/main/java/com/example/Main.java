package com.example;

import com.example.initialization.FirebaseInitialization;
import com.example.view.landing.SplashScreen;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        try {
            FirebaseInitialization.initialize();
        } catch (Exception e) {
            System.out.println("Exception in Firebase initialization: " + e.getMessage());
        }

        System.out.println("Beginning of the application...");
        Application.launch(SplashScreen.class, args);
    }

}