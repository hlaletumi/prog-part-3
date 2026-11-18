package com.mycompany.part1.st10513583;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTester {

    // Part 2 test data (IDs are fixed here so the hashes are predictable)
    private Message msg1;
    private Message msg2;

    // Part 3 test data
    private Message p1, p2, p3, p4, p5;

    @BeforeEach
    public void setUp() {
        Message.resetMessages();
        Message.setJsonFile("test_messages.json");   // so the real messages.json is never touched
        msg1 = new Message("0012345678", "+27718693002", "Hi Mike, can you join us for dinner tonight?", 0);
        msg2 = new Message("0812345678", "08575975889", "Hi Keegan, did you receive the payment?", 1);
    }

    @AfterEach
    public void cleanUp() {
        new File("test_messages.json").delete();
    }

    // Builds Part 3 test messages 1-5 and flags them (1 = Sent, 2 = Stored, 3 = Disregard, 4 = Sent, 5 = Stored)
    private void loadPart3TestData() {
        p1 = new Message("1000000001", "+27834557896", "Did you get the cake?", 0);
        p2 = new Message("2000000002", "+27838884567", "Where are you? You are late! I have asked you to be on time.", 1);
        p3 = new Message("3000000003", "+27834484567", "Yohoooo, I am at your gate.", 2);
        p4 = new Message("0838884567", "0838884567", "It is dinner time!", 3);   // developer entry
        p5 = new Message("5000000005", "+27838884567", "Ok, I am leaving without you.", 4);

        p1.SentMessage(1);
        p2.SentMessage(3);
        p3.SentMessage(2);
        p4.SentMessage(1);
        p5.SentMessage(3);
    }

    // ---------- Part 2 tests ----------

    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", msg1.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        String longText = "a".repeat(260);
        Message longMsg = new Message("0012345678", "+27718693002", longText, 0);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                longMsg.checkMessageLength());
    }

    @Test
    public void testRecipientNumberSuccess() {
        assertEquals("Cell phone number successfully captured.", msg1.checkRecipientCell());
    }

    @Test
    public void testRecipientNumberFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                msg2.checkRecipientCell());
    }

    @Test
    public void testMessageHashTestCase1() {
        assertEquals("00:0:HITONIGHT", msg1.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        String[] ids = {"0012345678", "0812345678"};
        String[] texts = {"Hi Mike, can you join us for dinner tonight?",
                          "Hi Keegan, did you receive the payment?"};
        String[] expected = {"00:0:HITONIGHT", "08:1:HIPAYMENT"};

        for (int i = 0; i < ids.length; i++) {
            Message m = new Message(ids[i], "+27718693002", texts[i], i);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    @Test
    public void testMessageIDCreated() {
        Message random = new Message("+27718693002", "Hello there", 0);
        assertTrue(random.checkMessageID());
        assertEquals("Message ID generated: " + random.getMessageID(), random.getMessageIDStatus());
    }

    @Test
    public void testMessageIDTooLong() {
        Message bad = new Message("00123456789", "+27718693002", "Hello there", 0);
        assertFalse(bad.checkMessageID());
    }

    @Test
    public void testSentMessageSend() {
        assertEquals("Message successfully sent.", msg1.SentMessage(1));
    }

    @Test
    public void testSentMessageDisregard() {
        assertEquals("Press 0 to delete the message.", msg2.SentMessage(2));
    }

    @Test
    public void testSentMessageStore() {
        assertEquals("Message successfully stored.", msg1.SentMessage(3));
    }

    @Test
    public void testReturnTotalMessages() {
        msg1.SentMessage(1);
        msg2.SentMessage(2);
        assertEquals(1, msg1.returnTotalMessagess());
    }

    @Test
    public void testPrintMessages() {
        msg1.SentMessage(1);
        assertTrue(msg1.printMessages().contains("00:0:HITONIGHT"));
    }

    // ---Part 3 tests -----

    // Sent messages array contains the expected test data (messages 1 and 4)
    @Test
    public void testSentMessagesArrayPopulated() {
        loadPart3TestData();
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                Message.getSentMessageTexts());
    }

    // Disregarded messages array contains message 3
    @Test
    public void testDisregardedMessagesArrayPopulated() {
        loadPart3TestData();
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.", Message.getDisregardedMessages().get(0).getMessage());
    }

    // Hash and ID arrays contain all five messages
    @Test
    public void testHashAndIDArraysPopulated() {
        loadPart3TestData();
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
        assertTrue(Message.getMessageHashes().contains(p2.getMessageHash()));
        assertTrue(Message.getMessageIDs().contains("0838884567"));
    }

    // Stored messages array is read back from the JSON file
    @Test
    public void testStoredMessagesReadFromJson() {
        loadPart3TestData();
        Message.resetMessages();          // empty the arrays; the JSON file still has messages 2 and 5
        Message.loadStoredMessages();
        assertEquals(2, Message.getStoredMessages().size());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.getStoredMessages().get(0).getMessage());
        assertEquals("Ok, I am leaving without you.", Message.getStoredMessages().get(1).getMessage());
    }

    // Display the longest message
    @Test
    public void testDisplayLongestMessage() {
        loadPart3TestData();
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.displayLongestMessage());
    }

    // Search for a message ID (message 4)
    @Test
    public void testSearchForMessageID() {
        loadPart3TestData();
        assertEquals("It is dinner time!", Message.searchByMessageID("0838884567").getMessage());
    }

    // Search all messages sent or stored for a recipient
    @Test
    public void testSearchByRecipient() {
        loadPart3TestData();
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."));
        assertEquals(expected, Message.searchByRecipient("+27838884567"));
    }

    // Delete a message using its hash (test message 2)
    @Test
    public void testDeleteMessageUsingHash() {
        loadPart3TestData();
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                Message.deleteByHash(p2.getMessageHash()));
        assertEquals(1, Message.getStoredMessages().size());
    }

    // Deleting a hash that does not exist
    @Test
    public void testDeleteMessageUsingUnknownHash() {
        loadPart3TestData();
        assertEquals("No stored message found with that hash.", Message.deleteByHash("99:9:NOPE"));
    }

    // Display report shows hash, recipient and message of the sent messages
    @Test
    public void testDisplayReport() {
        loadPart3TestData();
        String report = Message.displayReport(Message.getSentMessages());
        assertTrue(report.contains(p1.getMessageHash()));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains(p4.getMessageHash()));
        assertTrue(report.contains("It is dinner time!"));
    }
}