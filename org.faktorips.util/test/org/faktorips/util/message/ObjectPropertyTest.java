package org.faktorips.util.message;

import junit.framework.TestCase;

public class ObjectPropertyTest extends TestCase {

    /*
     * Test method for 'org.faktorips.util.message.ObjectProperty.hashCode()'
     */
    public void testHashCode() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1.hashCode(), op2.hashCode());

        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertFalse(op1.hashCode() == op3.hashCode());
        
    }

    /*
     * Test method for 'org.faktorips.util.message.ObjectProperty.equals(Object)'
     */
    public void testEqualsObject() {
        ObjectProperty op1 = new ObjectProperty(new Integer(1), "toString");
        ObjectProperty op2 = new ObjectProperty(new Integer(1), "toString");
        assertEquals(op1, op2);
        
        ObjectProperty op3 = new ObjectProperty(new Integer(2), "toString");
        assertTrue(!op1.equals(op3));
        
    }

}
