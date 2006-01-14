package org.faktorips.message;

import junit.framework.TestCase;

import org.faktorips.util.message.*;

/**
 *
 */
public class MessageListTest extends TestCase {

    public void testAddMessage() {
        MessageList list = new MessageList();
        
        Message msg1 = Message.newError("1", "blabla");
        list.add(msg1);
        assertEquals(1, list.getNoOfMessages());
        assertTrue(msg1==list.getMessage(0));
        
        Message msg2 = Message.newError("1", "blabla");
        list.add(msg2);
        assertEquals(2, list.getNoOfMessages());
        assertTrue(msg2==list.getMessage(1));
    }

    public void testAddMessageList() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);
        
        MessageList list2 = new MessageList();
        Message msg2 = Message.newError("2", "blabla");
        list2.add(msg2);
        Message msg3 = Message.newError("3", "blabla");
        list2.add(msg3);
        
        list1.add(list2);
        assertEquals(3, list1.getNoOfMessages());
        assertTrue(msg1==list1.getMessage(0));
        assertTrue(msg2==list1.getMessage(1));
        assertTrue(msg3==list1.getMessage(2));
        
        MessageList nullList = null;
        list1.add(nullList); // should not throw an exception
        assertEquals(3, list1.getNoOfMessages());
    }
    
    public void testAddMessageListObjectProperty_OverrideTrue() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);
        
        MessageList list2 = new MessageList();
        Message msg2 = Message.newInfo("2", "blabla");
        list2.add(msg2);
        ObjectProperty objProp1 = new ObjectProperty(this, "property1");
        Message msg3 = new Message("", "msg2", Message.ERROR, objProp1);
        list2.add(msg3);
        
        ObjectProperty objProp2 = new ObjectProperty(this, "property2");
        list1.add(list2, objProp2, true);
        assertEquals(3, list1.getNoOfMessages());
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().length);
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties()[0]);
        assertEquals(objProp2, list1.getMessage(2).getInvalidObjectProperties()[0]);
        
        assertEquals(msg3.getCode(), list1.getMessage(2).getCode());
        assertEquals(msg3.getText(), list1.getMessage(2).getText());
        assertEquals(msg3.getSeverity(), list1.getMessage(2).getSeverity());
    }
        
    public void testAddMessageListObjectProperty_OverrideFalse() {
        MessageList list1 = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        list1.add(msg1);
        
        MessageList list2 = new MessageList();
        Message msg2 = Message.newError("2", "blabla");
        list2.add(msg2);
        ObjectProperty objProp1 = new ObjectProperty(this, "property1");
        Message msg3 = new Message("", "msg2", Message.ERROR, objProp1);
        list2.add(msg3);
        
        ObjectProperty objProp2 = new ObjectProperty(this, "property2");
        list1.add(list2, objProp2, false);
        assertEquals(3, list1.getNoOfMessages());
        assertEquals(0, list1.getMessage(0).getInvalidObjectProperties().length);
        assertEquals(objProp2, list1.getMessage(1).getInvalidObjectProperties()[0]);
        // message 3 should remain untouched, as the invalid object properties were set!
        assertEquals(objProp1, list1.getMessage(2).getInvalidObjectProperties()[0]);
    }
        
    public void testGetNoOfMessages() {
        MessageList list = new MessageList();
        assertEquals(0, list.getNoOfMessages());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(1, list.getNoOfMessages());
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(2, list.getNoOfMessages()); // messages are equal, but the list does not filter duplicate messages
    }

    public void testGetMessage() {
        MessageList list = new MessageList();
        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("1", "blabla");
        list.add(msg1);
        list.add(msg2);
        
        assertTrue(msg1==list.getMessage(0));
        assertTrue(msg2==list.getMessage(1));
        
        try {
            list.getMessage(2);
            fail();
        } catch (IndexOutOfBoundsException e) {            
        }
    }

    public void testGetMessageByCode() {
        MessageList list = new MessageList();
        assertNull(list.getMessageByCode("1"));
        
        Message msg1 = Message.newError("1", "blabla");
        Message msg2 = Message.newError("2", "blabla");
        Message msg3 = Message.newError("2", "blabla");
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        
        assertTrue(msg1==list.getMessageByCode("1"));
        assertTrue(msg2==list.getMessageByCode("2"));
        assertNull(list.getMessageByCode("3"));
    }
    
    public void testGetSeverity() {
        MessageList list = new MessageList();
        assertEquals(0, list.getSeverity());
        
        list.add(Message.newInfo("1", "blabla"));
        assertEquals(Message.INFO, list.getSeverity());
        
        list.add(Message.newWarning("1", "blabla"));
        assertEquals(Message.WARNING, list.getSeverity());
        
        list.add(Message.newError("1", "blabla"));
        assertEquals(Message.ERROR, list.getSeverity());
    }

    public void testContainsErrorMsg() {
        MessageList list = new MessageList();
        assertFalse(list.containsErrorMsg());
        
        list.add(Message.newInfo("1", "blabla"));
        assertFalse(list.containsErrorMsg());
        
        list.add(Message.newWarning("1", "blabla"));
        assertFalse(list.containsErrorMsg());
        
        list.add(Message.newError("1", "blabla"));
        assertTrue(list.containsErrorMsg());
    }
    
    public void testToString() {
        MessageList list = new MessageList();
        list.toString();
        list.add(Message.newInfo("1", "blabla"));
        list.toString();
    }
    
    public void testEquals() {
        MessageList list = new MessageList();
        list.add(Message.newInfo("1", "blabla"));
        list.add(Message.newInfo("2", "Hello World"));
        
        assertFalse(list.equals(null));
        assertFalse(list.equals(this));

        MessageList list2 = new MessageList();
        list2.add(Message.newInfo("1", "blabla"));
        assertFalse(list.equals(list2));
        
        list2.add(Message.newInfo("2", "Hello World"));
        assertTrue(list.equals(list2));
    }
    
    public void testGetMessagesFor() {
        MessageList list = new MessageList();
        Message msg1 = new Message("1", "text1", Message.ERROR, this, "name");
        Message msg2 = new Message("2", "text2", Message.ERROR, "invalid object", "name");
        Message msg3 = new Message("2", "text3", Message.ERROR, this, "descriptopm");
        Message msg4 = new Message("2", "text4", Message.ERROR,  
                new ObjectProperty[]{new ObjectProperty(this, "description"), new ObjectProperty(this, "name")});
        
        list.add(msg1);
        list.add(msg2);
        list.add(msg3);
        list.add(msg4);

        // the object/property variant
        MessageList result = list.getMessagesFor(this, "name");
        assertEquals(2, result.getNoOfMessages());
        assertEquals(msg1, result.getMessage(0));        
        assertEquals(msg4, result.getMessage(1));

        // the object/any property variant
        result = list.getMessagesFor(this);
        assertEquals(3, result.getNoOfMessages());
        assertEquals(msg1, result.getMessage(0));        
        assertEquals(msg3, result.getMessage(1));        
        assertEquals(msg4, result.getMessage(2));

        // empty list
        result = list.getMessagesFor(result, "name");
        assertEquals(0, result.getNoOfMessages());
    }

    

}
