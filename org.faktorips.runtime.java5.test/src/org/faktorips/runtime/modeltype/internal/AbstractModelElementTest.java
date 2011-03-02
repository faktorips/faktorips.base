/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.junit.Test;

public class AbstractModelElementTest {

    private final InMemoryRuntimeRepository repository = new InMemoryRuntimeRepository();

    @Test
    public void testGetExtensionPropertyValue() {
        ModelType type = new ModelType(repository);
        assertNull(type.getExtensionPropertyValue("prop0"));

        type.setExtensionPropertyValue("prop0", new Integer(42));
        assertEquals(new Integer(42), type.getExtensionPropertyValue("prop0"));
        assertNull(type.getExtensionPropertyValue("prop1"));
    }

    @Test
    public void testGetExtensionPropertyIds() {
        ModelType type = new ModelType(repository);

        Set<String> ids = type.getExtensionPropertyIds();
        assertEquals(0, ids.size());

        type.setExtensionPropertyValue("prop0", new Integer(42));
        ids = type.getExtensionPropertyIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains("prop0"));

        type.setExtensionPropertyValue("prop1", new Integer(42));
        ids = type.getExtensionPropertyIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("prop0"));
        assertTrue(ids.contains("prop1"));
    }

}
