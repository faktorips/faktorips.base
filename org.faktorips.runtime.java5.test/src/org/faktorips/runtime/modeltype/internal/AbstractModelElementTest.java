/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
