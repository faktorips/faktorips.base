package org.faktorips.datatype;

import org.faktorips.datatype.DefaultEnumType;
import org.faktorips.datatype.DefaultEnumValue;

import junit.framework.TestCase;

/**
 *
 */
public class DefaultEnumTypeTest extends TestCase {
    
    private DefaultEnumType gender;
    private DefaultEnumValue male;
    private DefaultEnumValue female;
    
    protected void setUp() {
        gender = new DefaultEnumType("Gender", DefaultEnumValue.class);
        male = new DefaultEnumValue(gender, "male");
        female = new DefaultEnumValue(gender, "female");
    }

    public void testAddValue() {
    }

    public void testGetValues() {
        assertEquals(2, gender.getValues().length);
        assertEquals(male, gender.getValues()[0]);
        assertEquals(female, gender.getValues()[1]);
        
        // defensive copy test
        gender.getValues()[0] = null; // modifiy resulting array
        assertEquals(male, gender.getValues()[0]);
    }

    public void testGetValueIds() {
        assertEquals(2, gender.getValueIds().length);
        assertEquals("male", gender.getValueIds()[0]);
        assertEquals("female", gender.getValueIds()[1]);
    }

    public void testContainsValue() {
        assertTrue(gender.containsValue("male"));
        assertFalse(gender.containsValue("unkown"));
    }

    public void testGetEnumValue() {
    }

    public void testContains() {
    }

    public void testContainsNull() {
    }

    public void testGetNumOfValues() {
    }

    public void testCompareTo() {
    }
    
    public void testGetEnumValue_int() {
        assertEquals(male, gender.getEnumValue(0));
    }

}
