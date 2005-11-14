package org.faktorips.util;

import org.faktorips.util.ArgumentCheck;

import junit.framework.TestCase;

/**
 *
 */
public class ArgumentCheckTest extends TestCase {

    public void testIsSubclassOf() {
        ArgumentCheck.isSubclassOf(this.getClass(), TestCase.class);
        try {
            ArgumentCheck.isSubclassOf(String.class, TestCase.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIsInstanceOf() {
        ArgumentCheck.isInstanceOf(this, TestCase.class);
        try {
            ArgumentCheck.isInstanceOf(this, String.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
