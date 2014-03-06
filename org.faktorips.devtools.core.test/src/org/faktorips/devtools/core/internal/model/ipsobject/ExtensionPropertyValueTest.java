/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionPropertyValueTest {

    private String ID = "myId";

    private static final String VALUE = "myValue";

    @Mock
    private Element extPropertiesEl;

    @Mock
    private Element valueElement;

    @Mock
    private Document document;

    @Mock
    private Node importedElement;

    @Mock
    private IpsObjectPartContainer part;

    @Mock
    private IExtensionPropertyDefinition propertyDef;

    @Mock
    private Object defaultObject;

    @Before
    public void setUpElement() {
        when(extPropertiesEl.getOwnerDocument()).thenReturn(document);
    }

    @Test
    public void testAppendToXml_element() throws Exception {
        ExtensionPropertyValue invalidExtensionProperty = new ExtensionPropertyValue(ID, valueElement);
        when(document.importNode(valueElement, true)).thenReturn(importedElement);

        invalidExtensionProperty.appendToXml(part, extPropertiesEl);

        verify(part).getExtensionPropertyDefinition(ID);
        verify(extPropertiesEl).appendChild(importedElement);
        verifyNoMoreInteractions(part);
    }

    @Test
    public void testAppendToXml_elementISNull() throws Exception {
        ExtensionPropertyValue invalidExtensionProperty = new ExtensionPropertyValue(ID, null);
        invalidExtensionProperty.setValue(defaultObject);

        when(part.getExtensionPropertyDefinition(ID)).thenReturn(propertyDef);
        when(document.importNode(valueElement, true)).thenReturn(importedElement);
        when(document.createElement("Value")).thenReturn(valueElement);

        invalidExtensionProperty.appendToXml(part, extPropertiesEl);

        verify(valueElement).setAttribute("id", ID);
        verify(valueElement).setAttribute("isNull", "false");
        verify(extPropertiesEl).appendChild(valueElement);
        verify(propertyDef).valueToXml(valueElement, defaultObject);
    }
}
