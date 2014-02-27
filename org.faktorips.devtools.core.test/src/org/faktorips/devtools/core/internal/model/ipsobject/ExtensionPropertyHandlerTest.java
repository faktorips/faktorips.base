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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionPropertyHandlerTest {

    private static final String MY_ID = "anyId";

    private static final String MY_ID2 = "anyId2";

    private static final String MY_DEFAULT_VALUE = "myDefaultValue";

    private static final String MY_DEFAULT_VALUE2 = "myDefaultValue2";

    private static final String MY_VALUE = "myValue";

    @Mock
    private IExtensionPropertyDefinition extPropDef;

    @Mock
    private IExtensionPropertyDefinition2 extPropDef2;

    @Mock
    private Element xmlRootElement;

    @Mock
    private Element xmlExtPropElement;

    @Mock
    private Element xmlValueElement;

    @Mock
    private Document xmlDocument;

    @Mock
    private IpsObjectPartContainer ipsObjectPartContainer;

    @InjectMocks
    private ExtensionPropertyHandler extensionPropertyHandler;

    @Before
    public void setUpExtPropDefAndPart() {
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID2);
        doReturn(Arrays.asList(extPropDef, extPropDef2)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        when(extPropDef.getPropertyId()).thenReturn(MY_ID);
        when(extPropDef.getDefaultValue()).thenReturn(MY_DEFAULT_VALUE);
        when(extPropDef.beforeSetValue(any(IIpsObjectPartContainer.class), any())).thenReturn(true);

        when(extPropDef2.getPropertyId()).thenReturn(MY_ID2);
        when(extPropDef2.getDefaultValue()).thenReturn(MY_DEFAULT_VALUE);
        when(extPropDef2.getDefaultValue(ipsObjectPartContainer)).thenReturn(MY_DEFAULT_VALUE2);
        when(extPropDef2.beforeSetValue(any(IIpsObjectPartContainer.class), any())).thenReturn(true);
    }

    @Before
    public void setUpXmlElementAndDocument() {
        when(xmlRootElement.getOwnerDocument()).thenReturn(xmlDocument);
        when(xmlExtPropElement.getOwnerDocument()).thenReturn(xmlDocument);
        when(xmlValueElement.getOwnerDocument()).thenReturn(xmlDocument);
        when(xmlDocument.createElement(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT))
                .thenReturn(xmlExtPropElement);
        when(xmlDocument.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT)).thenReturn(xmlValueElement);

        when(xmlExtPropElement.getNodeName()).thenReturn(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        when(xmlValueElement.getNodeName()).thenReturn(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        when(xmlValueElement.getNodeType()).thenReturn(Node.ELEMENT_NODE);
    }

    @Test
    public void testGetExtPropertyValue_defaultValue() throws Exception {

        Object propertyValue = extensionPropertyHandler.getExtPropertyValue(MY_ID);

        assertEquals(MY_DEFAULT_VALUE, propertyValue);
    }

    @Test
    public void testGetExtPropertyValue_defaultValueDependingOnPart() throws Exception {

        Object propertyValue = extensionPropertyHandler.getExtPropertyValue(MY_ID2);

        assertEquals(MY_DEFAULT_VALUE2, propertyValue);
    }

    @Test
    public void testSetExtPropertyValue() throws Exception {

        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);

        assertEquals(MY_VALUE, extensionPropertyHandler.getExtPropertyValue(MY_ID));
        verify(ipsObjectPartContainer).objectHasChanged();
        verify(extPropDef).afterSetValue(ipsObjectPartContainer, MY_VALUE);
    }

    @Test
    public void testSetExtPropertyValue_veto() throws Exception {
        when(extPropDef.beforeSetValue(ipsObjectPartContainer, MY_VALUE)).thenReturn(false);

        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValue(MY_ID));
        verify(ipsObjectPartContainer, times(0)).objectHasChanged();
        verify(extPropDef, times(0)).afterSetValue(ipsObjectPartContainer, MY_VALUE);
    }

    @Test
    public void testSetExtPropertyValue_sameValue() throws Exception {

        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_DEFAULT_VALUE);

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValue(MY_ID));
        verify(ipsObjectPartContainer, times(0)).objectHasChanged();
        verify(extPropDef).afterSetValue(ipsObjectPartContainer, MY_DEFAULT_VALUE);
    }

    @Test
    public void testCheckExtProperty() throws Exception {
        extensionPropertyHandler.checkExtProperty(MY_ID);

        verify(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckExtProperty_fail() throws Exception {
        extensionPropertyHandler.checkExtProperty("invalidId");
    }

    @Test
    public void testInitMissingExtProperties() throws Exception {
        extensionPropertyHandler.initMissingExtProperties();

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValues().get(MY_ID));
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValues().get(MY_ID2));
    }

    @Test
    public void testIsExtPropertyDefinitionAvailable() throws Exception {
        assertTrue(extensionPropertyHandler.isExtPropertyDefinitionAvailable(MY_ID));
        assertFalse(extensionPropertyHandler.isExtPropertyDefinitionAvailable("invalidId"));
    }

    @Test
    public void testToXml_checkExtPropertyElement() throws Exception {

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(xmlRootElement).appendChild(xmlExtPropElement);
    }

    @Test
    public void testToXml_oneValue() throws Exception {
        doReturn(Arrays.asList(extPropDef)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(xmlExtPropElement).appendChild(xmlValueElement);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, "" + false);
        verify(extPropDef).valueToXml(xmlValueElement, MY_VALUE);
    }

    @Test
    public void testToXml_values() throws Exception {

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(xmlExtPropElement, times(2)).appendChild(xmlValueElement);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID2);
        verify(xmlValueElement, times(2)).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, "" + false);
        verify(extPropDef).valueToXml(xmlValueElement, MY_DEFAULT_VALUE);
        verify(extPropDef2).valueToXml(xmlValueElement, MY_DEFAULT_VALUE2);
    }

    @Test
    public void testToXml_valueNull() throws Exception {
        extensionPropertyHandler.setExtPropertyValue(MY_ID, null);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(xmlExtPropElement, times(2)).appendChild(xmlValueElement);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID2);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, "" + false);
        verify(xmlValueElement).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, "" + true);
        verify(extPropDef2).valueToXml(xmlValueElement, MY_DEFAULT_VALUE2);
    }

    @Test
    public void testInitFromXml_emptyElementInitDefaults() throws Exception {
        NodeList nodeList = mock(NodeList.class);
        when(xmlRootElement.getChildNodes()).thenReturn(nodeList);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValues().get(MY_ID));
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValues().get(MY_ID2));
    }

    @Test
    public void testInitFromXml_initNull() throws Exception {
        setUpXmlElementsForInit(MY_ID, true);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(null, extensionPropertyHandler.getExtPropertyValues().get(MY_ID));
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValues().get(MY_ID2));
    }

    @Test
    public void testInitFromXml_initValue() throws Exception {
        setUpXmlElementsForInit(MY_ID, false);
        when(extPropDef.getValueFromXml(xmlValueElement)).thenReturn(MY_VALUE);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(MY_VALUE, extensionPropertyHandler.getExtPropertyValues().get(MY_ID));
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValues().get(MY_ID2));
    }

    private void setUpXmlElementsForInit(String propId, boolean isNull) {
        NodeList rootNodeList = mock(NodeList.class);
        when(xmlRootElement.getChildNodes()).thenReturn(rootNodeList);
        when(rootNodeList.getLength()).thenReturn(1);
        when(rootNodeList.item(0)).thenReturn(xmlExtPropElement);
        NodeList extPropNodeList = mock(NodeList.class);
        when(xmlExtPropElement.getChildNodes()).thenReturn(extPropNodeList);
        when(extPropNodeList.getLength()).thenReturn(1);
        when(extPropNodeList.item(0)).thenReturn(xmlValueElement);
        when(xmlValueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID)).thenReturn(propId);
        when(xmlValueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL)).thenReturn("" + isNull);
    }

    @Test
    public void testValidate_noMessages() throws Exception {

        MessageList messageList = extensionPropertyHandler.validate();

        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_foundMessages() throws Exception {
        MessageList messageList = new MessageList();
        Message myMessage = mock(Message.class);
        messageList.add(myMessage);
        when(extPropDef.validate(ipsObjectPartContainer, MY_DEFAULT_VALUE)).thenReturn(messageList);

        MessageList resultList = extensionPropertyHandler.validate();

        assertEquals(1, resultList.size());
        assertEquals(myMessage, resultList.getMessage(0));
    }

    @Test
    public void testAddExtensionPropertyValue() throws Exception {
        Object mock = mock(Object.class);
        when(extPropDef.getValueFromString(MY_VALUE)).thenReturn(mock);

        extensionPropertyHandler.addExtensionPropertyValue(MY_ID, MY_VALUE);

        assertEquals(mock, extensionPropertyHandler.getExtPropertyValues().get(MY_ID));
    }

}
