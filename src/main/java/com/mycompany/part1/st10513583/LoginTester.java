package com.mycompany.part1.st10513583;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LoginTester {

    private final Login login;

    public LoginTester() {
        login = new Login();
        login.setFirstName("Bohlale");
        login.setLastName("Ramafalo");
    }

    // Username correctly formatted
    @Test
    public void testUsernameCorrectlyFormatted() {
        login.registerUser("boitu_1", "boitu&&sec@An12!", "+27810867109");
        assertTrue(login.checkUserName("boitu_1"));
    }

    // Username incorrectly formatted
    @Test
    public void testUsernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("Bohlale!!!!!!"));
    }

    // Password meets complexity requirements
    @Test
    public void testPasswordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("boitu&&sec@An12!"));
    }

    // Password does not meet complexity requirements
    @Test
    public void testPasswordDoesNotMeetComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // Cell phone number correctly formatted
    @Test
    public void testCellPhoneCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27810867109"));
    }

    // Cell phone number incorrectly formatted
    @Test
    public void testCellPhoneIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("0810867109"));
    }

    // Login successful
    @Test
    public void testLoginSuccessful() {
        Login user = new Login();
        user.setFirstName("Bohlale");
        user.setLastName("Ramafalo");
        user.registerUser("boitu_12", "boitu&&sec@An12!", "+27810867109");
        assertTrue(user.loginUser("kyl_1", "boitu&&sec@An12!"));
    }

    // Login failed
    @Test
    public void testLoginFailed() {
        Login user = new Login();
        user.setFirstName("Bohlae");
        user.setLastName("Ramafalo");
        user.registerUser("boitu_12", "boitu&&sec@An12!", "+27810867109");
        assertFalse(user.loginUser("kyl_1", "wrongPassword1!"));
    }

    // Returns correct welcome message on successful login
    @Test
    public void testReturnLoginStatusMessageSuccess() {
        Login user = new Login();
        user.setFirstName("Bohlale");
        user.setLastName("Ramafalo");
        user.registerUser("boitu_1", "boitu&&sec@An12!", "+27810867109");
        String message = user.returnLoginStatus("boitu_1", "boitu&&sec@An12!");
        assertEquals("Welcome Bohlale, Ramafalo it is great to see you again.", message);
    }

    // Returns correct failure message on failed login
    @Test
    public void testReturnLoginStatusMessageFailure() {
        Login user = new Login();
        user.setFirstName("Bohlae");
        user.setLastName("Ramafalo");
        user.registerUser("boitu_1", "boitu&&sec@An12!", "+27810867109");
        String message = user.returnLoginStatus("boitu_1", "wrong");
        assertEquals("Username or password incorrect, please try again.", message);
    }
}