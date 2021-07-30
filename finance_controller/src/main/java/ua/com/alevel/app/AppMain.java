package ua.com.alevel.app;

import ua.com.alevel.app.controller.InputController;

import java.util.Arrays;
import java.util.List;

public class AppMain {

    public static void main(String[] args) {
        List<String> config = Arrays.asList(args);
        new InputController(config);
    }
}
