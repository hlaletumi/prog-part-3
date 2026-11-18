package com.mycompany.part1.st10513583;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message class - handles a single QuickChat message plus the arrays
 * (ArrayLists) shared by all messages while the program runs.
 */
public final class Message {

    public static final String CELL_OK = "Cell phone number successfully captured.";
    public static final String CELL_BAD = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    public static final String LENGTH_OK = "Message ready to send.";

    // ---------- Arrays (filled while the program runs - nothing is hard-coded) ----------
    private static final ArrayList<Message> sentMessages = new ArrayList<>();
    private static final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private static final ArrayList<Message> storedMessages = new ArrayList<>();   // read from the JSON file at start-up
    private static final ArrayList<String> messageHashes = new ArrayList<>();
    private static final ArrayList<String> messageIDs = new ArrayList<>();

    private static int totalMessagesSent = 0;
    private static String senderName = "Unknown";
    private static String jsonFile = "messages.json";

    private final String messageID;     // kept as a String so a leading 0 is not lost
    private final int messageNumber;    // starts at 0 and goes up per message
    private final String recipient;
    private final String message;
    private final String messageHash;

    // Normal constructor - the message ID is randomly generated
    public Message(String recipient, String message, int messageNumber) {
        this(generateRandomID(), recipient, message, messageNumber);
    }

    // Constructor with a given ID (used by the unit tests so the hash is predictable)
    public Message(String messageID, String recipient, String message, int messageNumber) {
        this.messageID = messageID;
        this.recipient = recipient;
        this.message = message;
        this.messageNumber = messageNumber;
        this.messageHash = createMessageHash();
    }

    // Constructor used when a stored message is read back from the JSON file
    public Message(String messageID, String messageHash, String recipient, String message) {
        this.messageID = messageID;
        this.messageHash = messageHash;
        this.recipient = recipient;
        this.message = message;
        this.messageNumber = numberFromHash(messageHash);
    }

    // Generates a random ten-digit number
    private static String generateRandomID() {
        long number = (long) (new Random().nextDouble() * 10000000000L);
        return String.format("%010d", number);
    }

    // Gets the message number back out of a hash (e.g. 00:1:HITHANKS -> 1)
    private static int numberFromHash(String hash) {
        try {
            return Integer.parseInt(hash.split(":")[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------- Part 2 methods ----------

    // Ensures the message ID is not more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Returns the message ID creation text
    public String getMessageIDStatus() {
        return "Message ID generated: " + messageID;
    }

    // Ensures the message is not more than 250 characters
    public String checkMessageLength() {
        if (message.length() <= 250) {
            return LENGTH_OK;
        }
        int over = message.length() - 250;
        return "Message exceeds 250 characters by " + over + "; please reduce the size.";
    }

    // Ensures the recipient has the international code (+27) and is correctly formatted
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+27[0-9]{1,10}$")) {
            return CELL_OK;
        }
        return CELL_BAD;
    }

    // Creates the hash: first two ID digits : message number : FIRSTLAST (all caps)
    public String createMessageHash() {
        String[] words = message.trim().split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        return (messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    // option 1 = send, 2 = disregard, 3 = store to send later
    // Every message's ID and hash is added to the ID and hash arrays.
    public String SentMessage(int option) {
        if (option < 1 || option > 3) {
            return "Invalid option.";
        }
        messageIDs.add(messageID);
        messageHashes.add(messageHash);

        if (option == 1) {
            sentMessages.add(this);
            totalMessagesSent++;
            return "Message successfully sent.";
        } else if (option == 2) {
            disregardedMessages.add(this);
            return "Press 0 to delete the message.";
        }
        storeMessage();
        return "Message successfully stored.";
    }

    // Full details of one message: ID, Hash, Recipient, Message
    public String getMessageDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    // Returns all messages sent while the program is running
    public String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.getMessageDetails()).append("\n\n");
        }
        return sb.toString();
    }

    // Returns the total number of messages sent
    public int returnTotalMessagess() {
        return totalMessagesSent;
    }

    public static int getTotalMessagesSent() {
        return totalMessagesSent;
    }

    // Adds this message to the stored array and saves the array to the JSON file
    public void storeMessage() {
        storedMessages.add(this);
        writeStoredMessagesToFile();
    }

   
    // Reads the JSON file into the stored messages array (call once when the program starts).
    // REFERENCES (accessed 18 November 2026):
    // - JSON format (keys, string values, escape characters): Ecma International, ECMA-404, https://www.json.org/
    // - Reading a file into a String: Oracle, java.nio.file.Files, https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
    // - Matching the values with Pattern/Matcher: Oracle Java Tutorials, "Regular Expressions",
    //   https://docs.oracle.com/javase/tutorial/essential/regex/
    public static void loadStoredMessages() {
        String json;
        try {
            json = new String(Files.readAllBytes(Paths.get(jsonFile)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return; // no file yet, so nothing has been stored
        }

        String text = "\"((?:[^\"\\\\]|\\\\.)*)\"";   // a JSON string value
        Pattern pattern = Pattern.compile(
                "\"messageID\"\\s*:\\s*" + text + "\\s*,\\s*"
                + "\"messageHash\"\\s*:\\s*" + text + "\\s*,\\s*"
                + "\"recipient\"\\s*:\\s*" + text + "\\s*,\\s*"
                + "\"message\"\\s*:\\s*" + text);
        Matcher matcher = pattern.matcher(json);

        for (; matcher.find(); ) {
            Message m = new Message(unescape(matcher.group(1)), unescape(matcher.group(2)),
                    unescape(matcher.group(3)), unescape(matcher.group(4)));
            storedMessages.add(m);
            messageIDs.add(m.messageID);
            messageHashes.add(m.messageHash);
        }
    }

    // Texts of all sent messages
    public static ArrayList<String> getSentMessageTexts() {
        ArrayList<String> texts = new ArrayList<>();
        for (int i = 0; i < sentMessages.size(); i++) {
            texts.add(sentMessages.get(i).message);
        }
        return texts;
    }

    // a) Sender and recipient of all stored messages
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            sb.append("Sender: ").append(senderName)
              .append(" | Recipient: ").append(storedMessages.get(i).recipient).append("\n");
        }
        return sb.toString().trim();
    }

    // b) The longest stored message
    public static String displayLongestMessage() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (int i = 1; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).message.length() > longest.message.length()) {
                longest = storedMessages.get(i);
            }
        }
        return longest.message;
    }

    // c) Search for a message ID in the sent, disregarded and stored arrays
    public static Message searchByMessageID(String id) {
        ArrayList<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).messageID.equals(id)) {
                return all.get(i);
            }
        }
        return null;
    }

    public static String displayByMessageID(String id) {
        Message found = searchByMessageID(id);
        if (found == null) {
            return "No message found with that ID.";
        }
        return "Recipient: " + found.recipient + "\nMessage: " + found.message;
    }

    // d) All sent or stored messages for a particular recipient
    public static ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).recipient.equals(recipient)) {
                results.add(sentMessages.get(i).message);
            }
        }
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).recipient.equals(recipient)) {
                results.add(storedMessages.get(i).message);
            }
        }
        return results;
    }

    // e) Delete a stored message using its hash (also removes it from the hash/ID arrays and the JSON file)
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equals(hash)) {
                storedMessages.remove(i);
                messageHashes.remove(m.messageHash);
                messageIDs.remove(m.messageID);
                writeStoredMessagesToFile();
                return "Message: \"" + m.message + "\" successfully deleted.";
            }
        }
        return "No stored message found with that hash.";
    }

    // f) Report with the full details of every message in the given array
    public static String displayReport(ArrayList<Message> list) {
        if (list.isEmpty()) {
            return "No messages to report.";
        }
        StringBuilder sb = new StringBuilder("===== MESSAGE REPORT =====\n");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\n").append(list.get(i).getMessageDetails()).append("\n");
        }
        return sb.toString();
    }

    // ---------- JSON helpers ----------

    // Writes every stored message to the JSON file (written by hand, no library needed).
    // REFERENCES (accessed 18 November 2026): JSON structure and escaping from https://www.json.org/ ;
    // writing text files with FileWriter / try-with-resources: Oracle Java Tutorials, "Basic I/O",
    // https://docs.oracle.com/javase/tutorial/essential/io/
    private static void writeStoredMessagesToFile() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escape(m.messageID)).append("\",\n")
                .append("    \"messageHash\": \"").append(escape(m.messageHash)).append("\",\n")
                .append("    \"recipient\": \"").append(escape(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escape(m.message)).append("\"\n")
                .append("  }").append(i < storedMessages.size() - 1 ? "," : "").append("\n");
        }
        json.append("]");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write " + jsonFile + ": " + e.getMessage());
        }
    }

    // Escapes characters that would break the JSON text
    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"")
                   .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // Reverses escape() when reading the JSON text back in
    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;
                char next = text.charAt(i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- Helpers ----------

    // Clears all arrays and the counter (used by the unit tests)
    public static void resetMessages() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalMessagesSent = 0;
    }

    public static void setSenderName(String name) { senderName = name; }
    public static void setJsonFile(String fileName) { jsonFile = fileName; }

    public static ArrayList<Message> getSentMessages() { return sentMessages; }
    public static ArrayList<Message> getDisregardedMessages() { return disregardedMessages; }
    public static ArrayList<Message> getStoredMessages() { return storedMessages; }
    public static ArrayList<String> getMessageHashes() { return messageHashes; }
    public static ArrayList<String> getMessageIDs() { return messageIDs; }

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
}