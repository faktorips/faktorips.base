/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;

import junit.framework.TestCase;

public class DefaultObjectReferenceStoreTest extends TestCase {

    public void testGetObject() {
        DefaultObjectReferenceStore store = new DefaultObjectReferenceStore();
        IMyInterface someObject = new MyImplementation();
        store.putObject("4711", someObject);
        
        assertEquals(someObject, store.getObject(MyImplementation.class, "4711"));
        assertEquals(someObject, store.getObject(IMyInterface.class, "4711"));
        
        assertNull(store.getObject(String.class, "4711"));
        assertNull(store.getObject(MyImplementation.class, "unknownId"));
    }

    
    interface IMyInterface {
        
        
    }
    
    class MyImplementation implements IMyInterface {
        
    }
}
