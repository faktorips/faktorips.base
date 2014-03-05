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
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.ipsobject.ExtensionPropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.CDATASection;
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
    private CDATASection cdata;

    @Before
    public void setUpElement() {
        when(extPropertiesEl.getOwnerDocument()).thenReturn(document);
    }

    @Test
    public void testAppendToXml_string() throws Exception {
        ExtensionPropertyValue invalidExtensionPropertyStringRepresentation = ExtensionPropertyValue
                .createInvalidExtensionProperty(ID, VALUE);
        when(valueElement.getOwnerDocument()).thenReturn(document);
        when(document.createElement("Value")).thenReturn(valueElement);
        when(document.createCDATASection(VALUE)).thenReturn(cdata);

        invalidExtensionPropertyStringRepresentation.appendToXml(extPropertiesEl);

        verify(extPropertiesEl).appendChild(valueElement);
        verify(valueElement).setAttribute("id", ID);
        verify(valueElement).setAttribute("isNull", "false");
        verify(valueElement).appendChild(cdata);
    }

    @Test
    public void testAppendToXml_element() throws Exception {
        ExtensionPropertyValue invalidExtensionProperty = ExtensionPropertyValue
                .createInvalidExtensionProperty(valueElement);
        when(document.importNode(valueElement, true)).thenReturn(importedElement);

        invalidExtensionProperty.appendToXml(extPropertiesEl);

        verify(extPropertiesEl).appendChild(importedElement);
    }
}
