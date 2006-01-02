package org.faktorips.devtools.core.model;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetTypeTest extends TestCase {
    
    public void testGetValueSetType() {
        assertNull(ValueSetType.getValueSetType("unknown"));
        assertEquals(ValueSetType.ALL_VALUES, ValueSetType.getValueSetType(ValueSetType.ALL_VALUES.getId()));
    }

    public void testGetValueSetTypeByName() {
        assertNull(ValueSetType.getValueSetTypeByName("unknown"));
        assertEquals(ValueSetType.ALL_VALUES, ValueSetType.getValueSetTypeByName(
                ValueSetType.ALL_VALUES.getName()));
    }

    public void testGetValueSetTypes() {
        assertEquals(3, ValueSetType.getValueSetTypes().length);
    }

}
