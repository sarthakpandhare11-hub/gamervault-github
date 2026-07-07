package com.example.view.util;

import javafx.scene.layout.Region;

public class SizedBox extends Region {
    
    // Named constructor for a structural box with identical width and height
    public SizedBox(double width, double height) {
        setMinWidth(width); setPrefWidth(width); setMaxWidth(width);
        setMinHeight(height); setPrefHeight(height); setMaxHeight(height);
    }

    // Static helper factory method for vertical gaps
    public static Region height(double height) {
        Region spacer = new Region();
        spacer.setMinHeight(height); spacer.setPrefHeight(height); spacer.setMaxHeight(height);
        return spacer;
    }

    // Static helper factory method for horizontal gaps
    public static Region width(double width) {
        Region spacer = new Region();
        spacer.setMinWidth(width); spacer.setPrefWidth(width); spacer.setMaxWidth(width);
        return spacer;
    }
}
