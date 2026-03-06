package com.example.csc325_firebase_webview_auth.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Please enter both email and password.");
            alert.showAndWait();
            return;
        }

        // Simple validation - in production, use proper Firebase Auth
        if (email.length() > 0 && password.length() >= 6) {
            System.out.println("Login attempt with: " + email);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Login");
            successAlert.setHeaderText("Success");
            successAlert.setContentText("Login successful!");
            successAlert.showAndWait();

            try {
                App.setRoot("/files/AccessFBView.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("Invalid Credentials");
            alert.setContentText("Email or password is incorrect.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRegisterLink() {
        try {
            App.setRoot("/files/register.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



