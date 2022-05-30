/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.Set;

import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsExtensionProperty;
import org.faktorips.runtime.util.MessagesHelper;
import org.junit.Test;

public class ModelElementTest {

    private static final String ANY_TYPE = "any_type";

    private static final String ANY_NAME = "any_name";

    private final ModelElement element = new TestModelElement(ANY_NAME);

    @Test
    public void testGetExtensionPropertyValue() {
        assertThat(element.getExtensionPropertyValue("id1"), is(nullValue()));

        assertThat(element.getExtensionPropertyValue("id2"), equalTo((Object)"anyValue"));
    }

    @Test
    public void testGetExtensionPropertyIds() {
        Set<String> ids = element.getExtensionPropertyIds();
        assertEquals(2, ids.size());

        assertThat(ids, hasItems("id1", "id2"));
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(ANY_NAME, element.getName());
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals("testLabel", element.getLabel(Locale.CANADA));
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals("testDescription", element.getDescription(Locale.CANADA));
    }

    private static class TestModelElement extends ModelElement {

        public TestModelElement(String name) {
            super(name, AnnotatedModelElement.class.getAnnotation(IpsExtensionProperties.class));
        }

        @Override
        protected MessagesHelper getMessageHelper() {
            return createMessageHelper(AnnotatedModelElement.class.getAnnotation(IpsDocumented.class), getClass()
                    .getClassLoader());
        }

        @Override
        protected String getMessageKey(DocumentationKind messageType) {
            return messageType.getKey(getName(), ANY_TYPE, ANY_NAME);
        }
    }

    @IpsExtensionProperties({ @IpsExtensionProperty(id = "id1", isNull = true),
            @IpsExtensionProperty(id = "id2", value = "anyValue") })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class AnnotatedModelElement {

    }

}
