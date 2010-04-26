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
