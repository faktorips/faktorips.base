/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
    private Element importedElement;

    @Mock
    private CDATASection cdata;

    @Mock
    private IpsObjectPartContainer part;

    @Mock
    private IExtensionPropertyDefinition propertyDef;

    @Mock
    private Object defaultObject;

    @Before
    public void setUpElement() {
        when(extPropertiesEl.getOwnerDocument()).thenReturn(document);
        when(document.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT)).thenReturn(valueElement);
    }

    @Test
    public void testAppendToXml_invalid_element() throws Exception {
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(ID,
                valueElement, part);
        when(valueElement.getTagName()).thenReturn("InvalidExt");
        Element newElement = mock(Element.class);
        when(document.createElement("InvalidExt")).thenReturn(newElement);

        extensionPropertyValue.appendToXml(extPropertiesEl);

        verify(part).getExtensionPropertyDefinition(ID);
        verify(extPropertiesEl).getOwnerDocument();
        verify(extPropertiesEl).appendChild(newElement);
        verifyNoMoreInteractions(part);
    }

    @Test
    public void testAppendToXml_valid_element() throws Exception {
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(ID,
                valueElement, part);
        extensionPropertyValue.setValue(VALUE);
        when(part.getExtensionPropertyDefinition(ID)).thenReturn(propertyDef);

        extensionPropertyValue.appendToXml(extPropertiesEl);

        verify(propertyDef).valueToXml(valueElement, VALUE);
        verify(extPropertiesEl).appendChild(valueElement);
        verify(extPropertiesEl).getOwnerDocument();
        verifyNoMoreInteractions(extPropertiesEl);
    }

    @Test
    public void testAppendToXml_elementISNull() throws Exception {
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(ID,
                (Element)null, part);
        extensionPropertyValue.setValue(defaultObject);

        extensionPropertyValue.appendToXml(extPropertiesEl);

        verify(extPropertiesEl).getOwnerDocument();
        verifyNoMoreInteractions(extPropertiesEl);
    }

    @Test
    public void testAppendToXml_invalid_string() throws Exception {
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(ID, VALUE,
                part);
        when(valueElement.getOwnerDocument()).thenReturn(document);
        when(document.createElement("Value")).thenReturn(valueElement);
        when(document.createCDATASection(VALUE)).thenReturn(cdata);

        extensionPropertyValue.appendToXml(extPropertiesEl);

        verify(part).getExtensionPropertyDefinition(ID);
        verify(extPropertiesEl).appendChild(valueElement);
        verify(valueElement).setAttribute("id", ID);
        verify(valueElement).setAttribute("isNull", "false");
        verify(valueElement).appendChild(cdata);
    }

    @Test
    public void testAppendToXml_valid_string() throws Exception {
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(ID, "",
                part);
        extensionPropertyValue.setValue(VALUE);
        when(part.getExtensionPropertyDefinition(ID)).thenReturn(propertyDef);

        extensionPropertyValue.appendToXml(extPropertiesEl);

        verify(propertyDef).valueToXml(valueElement, VALUE);
        verify(extPropertiesEl).appendChild(valueElement);
        verify(extPropertiesEl).getOwnerDocument();
        verifyNoMoreInteractions(extPropertiesEl);
    }

}
