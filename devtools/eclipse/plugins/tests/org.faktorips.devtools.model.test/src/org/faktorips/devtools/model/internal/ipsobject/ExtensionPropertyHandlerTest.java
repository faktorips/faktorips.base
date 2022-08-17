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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.ipsobject.ExtensionPropertyHandler.ExtensionPropertyMap;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionPropertyHandlerTest {

    private static final String MY_ID = "anyId";

    private static final String MY_ID2 = "anyId2";

    private static final String INVALID_ID = "invalid";

    private static final String MY_DEFAULT_VALUE = "myDefaultValue";

    private static final String MY_DEFAULT_VALUE2 = "myDefaultValue2";

    private static final String MY_VALUE = "myValue";

    @Mock
    private IExtensionPropertyDefinition extPropDef;

    @Mock
    private IExtensionPropertyDefinition extPropDef2;

    @Mock
    private Element xmlRootElement;

    @Mock
    private Element xmlExtPropElement;

    @Mock
    private Element xmlValueElement;

    @Mock
    private Document xmlDocument;

    @Mock
    private DocumentBuilder documentBuilder;

    @Mock
    private IpsObjectPartContainer ipsObjectPartContainer;

    @Mock
    private ExtensionPropertyValue extensionProperty;

    @Mock
    private ExtensionPropertyValue extensionProperty2;

    @Mock
    private ExtensionPropertyValue extensionProperty3;

    @Mock
    private StringExtensionPropertyDefinition stringPropDef;

    @InjectMocks
    private ExtensionPropertyHandler extensionPropertyHandler;

    @Before
    public void setUpExtPropDefAndPart() {
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
        doReturn(extPropDef2).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID2);
        doReturn(Arrays.asList(extPropDef, extPropDef2)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        when(extPropDef.getPropertyId()).thenReturn(MY_ID);
        when(extPropDef.getDefaultValue()).thenReturn(MY_DEFAULT_VALUE);
        when(extPropDef.getDefaultValue(ipsObjectPartContainer)).thenReturn(MY_DEFAULT_VALUE);
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

        when(xmlDocument.importNode(xmlValueElement, true)).thenReturn(xmlValueElement);

        when(documentBuilder.newDocument()).thenReturn(xmlDocument);
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
    public void testGetExtPropertyValue_invalidInitialized() throws Exception {
        ExtensionPropertyValue value = ExtensionPropertyValue.createExtensionPropertyValue(MY_ID, xmlValueElement,
                ipsObjectPartContainer);
        when(extPropDef.getValueFromXml(xmlValueElement)).thenReturn(MY_VALUE);
        extensionPropertyHandler.getExtPropertyValuesMap().put(MY_ID, value);

        Object propertyValue = extensionPropertyHandler.getExtPropertyValue(MY_ID);

        assertEquals(MY_VALUE, propertyValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExtPropertyValue_expectIllegalArgumentException() throws Exception {
        doReturn(Arrays.asList()).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        extensionPropertyHandler.getExtPropertyValue(MY_ID);
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
        extensionPropertyHandler.checkExtProperty(INVALID_ID);
    }

    @Test
    public void testInitMissingExtProperties() throws Exception {
        extensionPropertyHandler.initMissingExtProperties();

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID).getValue());
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID2).getValue());
    }

    @Test
    public void testIsExtPropertyDefinitionAvailable() throws Exception {
        assertTrue(extensionPropertyHandler.isExtPropertyDefinitionAvailable(MY_ID));
        assertFalse(extensionPropertyHandler.isExtPropertyDefinitionAvailable("invalidId"));
    }

    @Test
    public void testToXml_checkExtPropertyElement() throws Exception {
        when(xmlExtPropElement.hasChildNodes()).thenReturn(true);

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
    public void testToXml_invalidThenValid() throws Exception {
        ExtensionPropertyValue propertyRepresentation = ExtensionPropertyValue.createExtensionPropertyValue(MY_ID,
                xmlValueElement, ipsObjectPartContainer);
        extensionPropertyHandler.getExtPropertyValuesMap().put(MY_ID, propertyRepresentation);
        doReturn(Arrays.asList(extPropDef)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(extPropDef).valueToXml(xmlValueElement, MY_VALUE);
        verify(xmlExtPropElement).appendChild(xmlValueElement);
        verify(xmlExtPropElement).getOwnerDocument();
        verify(xmlExtPropElement).hasChildNodes();
        verifyNoMoreInteractions(xmlExtPropElement);
    }

    @Test
    public void testToXml_invalidThenValidNoSet() throws Exception {
        ExtensionPropertyValue propertyRepresentation = ExtensionPropertyValue.createExtensionPropertyValue(MY_ID,
                xmlValueElement, ipsObjectPartContainer);
        extensionPropertyHandler.getExtPropertyValuesMap().put(MY_ID, propertyRepresentation);
        doReturn(Arrays.asList(extPropDef)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        when(extPropDef.getValueFromXml(xmlValueElement)).thenReturn(MY_VALUE);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(extPropDef).valueToXml(xmlValueElement, MY_VALUE);
        verify(xmlExtPropElement).appendChild(xmlValueElement);
        verify(xmlExtPropElement).getOwnerDocument();
        verify(xmlExtPropElement).hasChildNodes();
        verifyNoMoreInteractions(xmlExtPropElement);
    }

    /**
     * <strong>Scenario:</strong><br>
     * The extension property definition is valid (applicable) when the object ist loaded for the
     * first time. After editing and saving the object (1) the extension property definition gets
     * invalid (inapplicable) at (2).
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * We expect that the value element was added to the extension properties element exactly two
     * times. First time was called by save in (1) second time is called by the real test execution.
     */
    @Test
    public void testToXml_validThenInvalid() throws Exception {
        doReturn(Arrays.asList(extPropDef)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        setUpXmlElementsForInit(MY_ID, false);
        Element newElement = mock(Element.class);
        when(xmlDocument.createElement("InvalidExt")).thenReturn(newElement);

        extensionPropertyHandler.initPropertyFromXml(xmlValueElement);
        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);
        // (1) save for the first time
        extensionPropertyHandler.toXml(xmlRootElement);
        // (2) definition gets invalid
        doReturn(Arrays.asList()).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        when(ipsObjectPartContainer.getExtensionPropertyDefinition(MY_ID)).thenReturn(null);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(extPropDef).valueToXml(xmlValueElement, MY_VALUE);
        verify(xmlExtPropElement, times(1)).appendChild(xmlValueElement);
        verify(xmlExtPropElement, times(2)).getOwnerDocument();
        verify(xmlExtPropElement, times(2)).hasChildNodes();
        verify(xmlValueElement, times(1)).getTagName();
        verify(xmlDocument, times(1)).createElement("InvalidExt");
        verify(xmlValueElement, times(1)).getAttributes();
        verify(xmlValueElement, times(1)).getChildNodes();
        verify(xmlValueElement, times(1)).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, MY_ID);
        verify(xmlValueElement, times(1)).setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, "" + false);
        verify(xmlExtPropElement, times(1)).appendChild(newElement);
        verifyNoMoreInteractions(xmlExtPropElement);
    }

    @Test
    public void testToXml_noEmptyExtPropElement() throws Exception {
        doReturn(Arrays.asList(extPropDef)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();
        extensionPropertyHandler.setExtPropertyValue(MY_ID, MY_VALUE);
        doReturn(Arrays.asList()).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(xmlRootElement).getOwnerDocument();
        verifyNoMoreInteractions(xmlRootElement);
    }

    @Test
    public void testInitFromXml_emptyElementInitDefaults() throws Exception {
        NodeList nodeList = mock(NodeList.class);
        when(xmlRootElement.getChildNodes()).thenReturn(nodeList);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID).getValue());
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID2).getValue());
    }

    @Test
    public void testInitFromXml_initNull() throws Exception {
        setUpXmlElementsForInit(MY_ID, true);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(null, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID).getValue());
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID2).getValue());
    }

    @Test
    public void testInitFromXml_initValue() throws Exception {
        setUpXmlElementsForInit(MY_ID, false);
        when(extPropDef.getValueFromXml(xmlValueElement)).thenReturn(MY_VALUE);

        extensionPropertyHandler.initFromXml(xmlRootElement);

        assertEquals(MY_VALUE, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID).getValue());
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID2).getValue());
    }

    private void setUpXmlElementsForInit(String propId, boolean isNull) {
        extensionPropertyHandler = spy(extensionPropertyHandler);
        doReturn(documentBuilder).when(extensionPropertyHandler).getDocumentBuilder();

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
        when(xmlValueElement.getTagName()).thenReturn("InvalidExt");
        NamedNodeMap namedNodeMap = mock(NamedNodeMap.class);
        when(namedNodeMap.getLength()).thenReturn(2);
        Attr attr1 = mock(Attr.class);
        when(attr1.getName()).thenReturn(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        when(attr1.getValue()).thenReturn(propId);
        when(namedNodeMap.item(0)).thenReturn(attr1);
        Attr attr2 = mock(Attr.class);
        when(attr2.getName()).thenReturn(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        when(attr2.getValue()).thenReturn("" + isNull);
        when(namedNodeMap.item(1)).thenReturn(attr2);
        when(xmlValueElement.getAttributes()).thenReturn(namedNodeMap);
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

        assertEquals(mock, extensionPropertyHandler.getExtPropertyValuesMap().get(MY_ID).getValue());
    }

    @Test
    public void testToXML_saveInvalidPropertiesToXML() {
        doReturn(new ArrayList<IExtensionPropertyDefinition>()).when(ipsObjectPartContainer)
                .getExtensionPropertyDefinitions();
        when(extensionProperty.getPreviouslyStoredXml(xmlDocument)).thenReturn(xmlValueElement);
        when(extensionProperty2.getPreviouslyStoredXml(xmlDocument)).thenReturn(xmlValueElement);
        when(extensionProperty3.getPreviouslyStoredXml(xmlDocument)).thenReturn(xmlValueElement);
        initMaps(MY_ID);

        extensionPropertyHandler.toXml(xmlRootElement);

        verify(extensionProperty).appendToXml(xmlExtPropElement);
        verify(extensionProperty2).appendToXml(xmlExtPropElement);
        verify(extensionProperty3).appendToXml(xmlExtPropElement);
    }

    private void initMaps(String id) {
        ExtensionPropertyMap map = extensionPropertyHandler.getExtPropertyValuesMap();
        map.put(id, extensionProperty);
        map.put(id + 2, extensionProperty2);
        map.put(id + 3, extensionProperty3);
    }

    @Test
    public void testInitPropertyFromXml_InvalidPropertyToMap_XMLRepresentation() {
        setUpXmlElementsForInit(INVALID_ID, false);

        extensionPropertyHandler.initPropertyFromXml(xmlValueElement);

        ExtensionPropertyMap map = extensionPropertyHandler.getExtPropertyValuesMap();
        assertEquals(1, map.values().size());
        assertNotNull(map.get(INVALID_ID));
    }

    /**
     * Verify the order of the extension property map. This is important to store the properties
     * always in same order to xml.
     */
    @Test
    public void testGetExtPropertyValuesMap() throws Exception {
        initMaps(INVALID_ID);

        ExtensionPropertyMap extPropertyValuesMap = extensionPropertyHandler.getExtPropertyValuesMap();

        Iterator<ExtensionPropertyValue> iterator = extPropertyValuesMap.values().iterator();
        assertEquals(extensionProperty, iterator.next());
        assertEquals(extensionProperty2, iterator.next());
        assertEquals(extensionProperty3, iterator.next());
    }

    @Test
    public void testRemoveObsoleteExtensionProperties() {
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
        doReturn(extPropDef2).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID + 2);
        initMaps(MY_ID);

        extensionPropertyHandler.removeObsoleteExtensionProperties();
        ExtensionPropertyMap map = extensionPropertyHandler.getExtPropertyValuesMap();

        assertEquals(2, map.values().size());
        assertNull(map.get(MY_ID + 3));
    }

    @Test
    public void testRemoveObsoleteExtensionProperties_noNPE() {
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
        doReturn(extPropDef2).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID + 2);

        extensionPropertyHandler.removeObsoleteExtensionProperties();
        ExtensionPropertyMap map = extensionPropertyHandler.getExtPropertyValuesMap();

        assertEquals(0, map.values().size());
    }
}
