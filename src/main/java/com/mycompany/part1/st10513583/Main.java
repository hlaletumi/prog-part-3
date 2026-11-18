/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.part1.st10513583;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main class - registration, login, then the QuickChat menu.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (scanner) {
            Login login = new Login();
            
            // ---------- Registration ----------
            System.out.println("=== User Registration ===");
            
            System.out.print("Enter first name: ");
            String firstName = scanner.nextLine();
            login.setFirstName(firstName);
            
            System.out.print("Enter last name: ");
            String lastName = scanner.nextLine();
            login.setLastName(lastName);
            
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            
            System.out.print("Enter South African cell phone number (e.g. +27838968976): ");
            String cellPhoneNumber = scanner.nextLine();
            
            System.out.println(login.registerUser(username, password, cellPhoneNumber));
            
            // getUsername() is only set when registration succeeded
            if (login.getUsername() == null) {
                System.out.println("Registration failed. Please run the program again.");
                return;
            }
            
            // ---------- Login ----------
            System.out.println("\n=== Login ===");
            System.out.print("Enter username: ");
            String loginUsername = scanner.nextLine();
            
            System.out.print("Enter password: ");
            String loginPassword = scanner.nextLine();
            
            System.out.println(login.returnLoginStatus(loginUsername, loginPassword));
            
            // Only logged-in users may continue
            if (!login.loginUser(loginUsername, loginPassword)) {
                return;
            }
            
            // ---------- QuickChat ----------
            System.out.println("\nWelcome to QuickChat.");
            
            Message.setSenderName(firstName + " " + lastName);
            Message.loadStoredMessages();   // read the JSON file into the stored messages array
            
            int maxMessages = readNumber("How many messages do you wish to enter? ");
            int entered = 0;
            
            int choice;
            for (choice = showMenu(); choice != 3; choice = showMenu()) {
                switch (choice) {
                    case 1:
                        if (entered >= maxMessages) {
                            System.out.println("You have already entered all " + maxMessages + " messages.");
                        } else {
                            for (; entered < maxMessages; entered++) {
                                enterMessage(entered);
                            }
                            System.out.println("\nTotal messages sent: " + Message.getTotalMessagesSent());
                        }   break;
                    case 2:
                        System.out.println("Coming Soon.");
                        break;
                    case 4:
                        storedMessagesMenu();
                        break;
                    default:
                        System.out.println("Please choose option 1, 2, 3 or 4.");
                        break;
                }
            }
            
            System.out.println("\nTotal messages sent: " + Message.getTotalMessagesSent());
            System.out.println("Goodbye.");
        }
    }

    // Prints the main menu and returns the option chosen
    private static int showMenu() {
        System.out.println("\n1) Send Messages");
        System.out.println("2) Show recently sent messages");
        System.out.println("3) Quit");
        System.out.println("4) Stored Messages");
        return readNumber("Choose an option: ");
    }

    // Stored Messages menu - uses the arrays in the Message class
    private static void storedMessagesMenu() {
        int option;
        for (option = showStoredMenu(); option != 7; option = showStoredMenu()) {
            switch (option) {
                case 1 -> System.out.println(Message.displaySenderAndRecipient());
                case 2 -> System.out.println(Message.displayLongestMessage());
                case 3 -> {
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.displayByMessageID(scanner.nextLine().trim()));
                }
                case 4 -> {
                    System.out.print("Enter the recipient cell number: ");
                    ArrayList<String> results = Message.searchByRecipient(scanner.nextLine().trim());
                    if (results.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }   for (int i = 0; i < results.size(); i++) {
                        System.out.println((i + 1) + ") " + results.get(i));
                    }
                }
                case 5 -> {
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByHash(scanner.nextLine().trim().toUpperCase()));
                }
                case 6 -> System.out.println(Message.displayReport(Message.getStoredMessages()));
                default -> System.out.println("Please choose an option from 1 to 7.");
            }
        }
    }

    private static int showStoredMenu() {
        System.out.println("\n--- Stored Messages ---");
        System.out.println("1) Display sender and recipient of all stored messages");
        System.out.println("2) Display the longest stored message");
        System.out.println("3) Search for a message ID");
        System.out.println("4) Search for all messages for a recipient");
        System.out.println("5) Delete a message using its hash");
        System.out.println("6) Display report of all stored messages");
        System.out.println("7) Back");
        return readNumber("Choose an option: ");
    }

    // Captures one message and lets the user send, disregard or store it
    private static void enterMessage(int messageNumber) {
        System.out.println("\n--- Message " + (messageNumber + 1) + " ---");

        // Recipient - ask again until correctly formatted
        String recipient = "";
        for (boolean valid = false; !valid; ) {
            System.out.print("Enter recipient cell number (e.g. +27718693002): ");
            recipient = scanner.nextLine();
            String cellResult = new Message(recipient, "x", messageNumber).checkRecipientCell();
            System.out.println(cellResult);
            valid = cellResult.equals(Message.CELL_OK);
        }

        // Message text - ask again until it is 250 characters or less
        String text = "";
        for (boolean valid = false; !valid; ) {
            System.out.print("Enter your message: ");
            text = scanner.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else if (text.length() > 250) {
                System.out.println("Please enter a message of less than 250 characters.");
            } else {
                System.out.println("Message sent");
                valid = true;
            }
        }

        Message msg = new Message(recipient, text, messageNumber);
        System.out.println(msg.getMessageIDStatus());

        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int option = readNumber("Choose an option: ");
        for (; option < 1 || option > 3; ) {
            option = readNumber("Please choose 1, 2 or 3: ");
        }

        System.out.println(msg.SentMessage(option));

        if (option == 1) {
            System.out.println("\n" + msg.getMessageDetails());
        } else if (option == 2) {
            String answer = scanner.nextLine();
            if (answer.trim().equals("0")) {
                System.out.println("Message deleted.");
            }
        }
    }

    // Reads a whole number from the user, asking again until it is valid
    private static int readNumber(String prompt) {
        int number = 0;
        for (boolean valid = false; !valid; ) {
            System.out.print(prompt);
            try {
                number = Integer.parseInt(scanner.nextLine().trim());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        return number;
    }
}