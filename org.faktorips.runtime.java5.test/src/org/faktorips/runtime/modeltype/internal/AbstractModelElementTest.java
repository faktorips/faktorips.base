/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.runtime.IRuntimeRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AbstractModelElementTest {

    @Mock
    private IRuntimeRepository repository;

    private AbstractModelElement element;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        element = new TestModelElement(repository);
    }

    @Test
    public void testGetExtensionPropertyValue() {
        assertNull(element.getExtensionPropertyValue("prop0"));

        element.setExtensionPropertyValue("prop0", new Integer(42));
        assertEquals(new Integer(42), element.getExtensionPropertyValue("prop0"));
        assertNull(element.getExtensionPropertyValue("prop1"));
    }

    @Test
    public void testGetExtensionPropertyIds() {
        Set<String> ids = element.getExtensionPropertyIds();
        assertEquals(0, ids.size());

        element.setExtensionPropertyValue("prop0", new Integer(42));
        ids = element.getExtensionPropertyIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains("prop0"));

        element.setExtensionPropertyValue("prop1", new Integer(42));
        ids = element.getExtensionPropertyIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("prop0"));
        assertTrue(ids.contains("prop1"));
    }

    private static class TestModelElement extends AbstractModelElement {

        public TestModelElement(IRuntimeRepository repository) {
            super(repository);
        }

    }

}
