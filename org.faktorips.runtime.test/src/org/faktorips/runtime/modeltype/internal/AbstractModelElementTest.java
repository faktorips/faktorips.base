/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class AbstractModelElementTest {

    private static final String ANY_NAME = "any_name";

    private AbstractModelElement element;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        element = new TestModelElement(ANY_NAME);
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

    @Test
    public void testGetName() throws Exception {
        assertEquals(ANY_NAME, element.getName());
    }

    private static class TestModelElement extends AbstractModelElement {

        public TestModelElement(String name) {
            super(name);
        }

    }

}
