package com.example.controller;

import com.example.controller.admin.NotificationController;
import com.example.dao.FirebaseAuthDao;
import com.example.model.UserModel;

public class AuthController {

    /*
     * Current user instance for complete project made static.
     * Used in multiple screens
     */
    public static UserModel currentUser;

    /*
     * This method is a gateway / bridge between the Firebase auth and UI.
     * This method checks all the parameters and requirements before registering new
     * user.
     * All requirements of password and email are checked and known here.
     * 
     * If everything is satisfied then the signIn / new register method is called.
     * 
     * Parameters :
     * - email: (String)
     * - password: (String)
     * 
     * Returns:
     * - (String) Message to display either fault or success.
     */
    public static String handleNewRegister(
            String email,
            String password,
            String confirmPassword,
            String userName) {

        if (email.trim().isEmpty() || (!email.trim().endsWith("@gmail.com"))) {
            return "Please enter a valid email address.";
        } else if (userName.trim().length() < 3) {
            return "Username must be at least 3 characters long.";
        } else if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        } else if (!confirmPassword.equals(password)) {
            return "Passwords do not match.";
        } else if (email.trim().endsWith("@gmail.com") && (password.length() >= 6)
                && password.equals(confirmPassword)) {
            String userId = FirebaseAuthDao.signUp(email, confirmPassword);
            if (userId != null) {

                // Object to pass to the database
                UserModel newUser = new UserModel();
                newUser.setUserId(userId);
                newUser.setEmail(email);
                newUser.setPlayerName(userName);
                newUser.setIgn(userName); // Default IGN to username
                newUser.setRole("PLAYER"); // Default role

                boolean isSaved = UserController.createProfile(newUser);
                if (isSaved) {
                    // Log to the global system so Admins see the user growth in real-time
                    NotificationController.sendNotification(
                            "New Player Joined",
                            userName + " has successfully registered on GamerVault.",
                            "SYSTEM",
                            "GLOBAL");
                    return "User registered successfully! Login now";
                } else {
                    return "Failed to create profile. Please try again.";
                }
            } else {
                return "Email already exist or Registration failed...";
            }
        }
        return "Something went wrong, Try again...";
    }

    /*
     * This method is a gateway / bridge between the Firebase auth and UI.
     * This method checks all the parameters and requirements before logging in
     * user.
     * 
     * Parameters :
     * - email: (String)
     * - password: (String)
     * 
     * Returns:
     * - (String) Message to display either fault or success.
     */
    public static String handleLogin(String email, String password) {

        if (email.trim().isEmpty() || (!email.trim().endsWith("@gmail.com"))) {
            return "Please enter a valid email address.";
        } else if (password.length() < 6) {
            return "Password must be at least 6 characters long.";
        } else if (email.trim().endsWith("@gmail.com") && (password.length() >= 6)) {

            String userId = FirebaseAuthDao.loginWithEmailAndPassword(email, password);

            currentUser = UserController.getUserProfile(userId);
            if (currentUser != null && currentUser.isSuspended()) {
                currentUser = null; // Kick them out
                return "ACCESS DENIED: This account has been suspended by an Admin.";
            }

            if (currentUser != null) {
                return "Login successful! ";
            } else {
                return "Invalid email or password...";
            }
        }
        return "Something went wrong, Try again...";
    }

}
