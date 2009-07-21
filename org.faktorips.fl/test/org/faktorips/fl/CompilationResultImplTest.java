/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import java.util.HashSet;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.Message;

/**
 *
 */
public class CompilationResultImplTest extends TestCase {

    public void testGetIdentifiersUsed() {
        CompilationResultImpl result = new CompilationResultImpl();
        assertEquals(0, result.getResolvedIdentifiers().length);

        result.addIdentifierUsed("a");
        assertEquals(1, result.getResolvedIdentifiers().length);
        
        result.addIdentifierUsed("b");
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertEquals("a", result.getResolvedIdentifiers()[0]);
        assertEquals("b", result.getResolvedIdentifiers()[1]);
    }
    
    public void testIsUsedAsIdentifier() {
        CompilationResultImpl result = new CompilationResultImpl();
        assertFalse(result.isUsedAsIdentifier("a"));

        result.addIdentifierUsed("a");
        assertTrue(result.isUsedAsIdentifier("a"));
        
        assertFalse(result.isUsedAsIdentifier(null));
    }
    
    public void testAddIdentifiersUsed() {
        CompilationResultImpl result = new CompilationResultImpl();
        result.addIdentifiersUsed(null);
        assertEquals(0, result.getResolvedIdentifiers().length);

        HashSet set = new HashSet();
        set.add("a");
        set.add("b");
        result.addIdentifiersUsed(set);
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertContains("a", result.getResolvedIdentifiers());
        assertContains("b", result.getResolvedIdentifiers());

        result.addIdentifiersUsed(null);
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertContains("a", result.getResolvedIdentifiers());
        assertContains("b", result.getResolvedIdentifiers());
    }
    
    private void assertContains(String expected, String[] arrayToCheck) {
        for (int i = 0; i < arrayToCheck.length; i++) {
            if (expected.equals(arrayToCheck[i])){
                return;
            }
        }
        fail("Expected value: " + expected + " not in array!");
    }

    public void testAdd() {
        CompilationResultImpl result1 = new CompilationResultImpl();
        
        CompilationResultImpl result2 = new CompilationResultImpl();
        result1.add(result2);
        assertEquals(0, result1.getResolvedIdentifiers().length);
        
        result2.addIdentifierUsed("a");
        result2.addIdentifierUsed("b");
        result1.add(result2);
        assertEquals(2, result1.getResolvedIdentifiers().length);
        assertEquals("a", result1.getResolvedIdentifiers()[0]);
        assertEquals("b", result1.getResolvedIdentifiers()[1]);

        // result with no addition identifier
        result2 = new CompilationResultImpl();
        result1.add(result2);
        assertEquals(2, result1.getResolvedIdentifiers().length);
        assertEquals("a", result1.getResolvedIdentifiers()[0]);
        assertEquals("b", result1.getResolvedIdentifiers()[1]);
        
        // duplicates shouldn't be added
        result1.addIdentifierUsed("a");
        assertEquals(2, result1.getResolvedIdentifiers().length);
        assertEquals("a", result1.getResolvedIdentifiers()[0]);
        assertEquals("b", result1.getResolvedIdentifiers()[1]);
        
        
    }
    
    public void testSuccessfullFailed() {
        CompilationResultImpl result = new CompilationResultImpl("blabla", Datatype.STRING);
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newInfo("1", "blabla"));
        assertTrue(result.successfull());
        assertFalse(result.failed());
        result.addMessage(Message.newError("1", "blabla"));
        assertFalse(result.successfull());
        assertTrue(result.failed());
    }
    
    public void testToString() {
        CompilationResultImpl result = new CompilationResultImpl("blabla", Datatype.STRING);
        result.toString();
        result = new CompilationResultImpl(Message.newError("1", "blabla"));
        result.toString();
    }

}
