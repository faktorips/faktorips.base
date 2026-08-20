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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    private MockitoSession mockito;

    @InjectMocks
    private ExtensionPropertyHandler extensionPropertyHandler;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID);
        lenient().doReturn(extPropDef2).when(ipsObjectPartContainer).getExtensionPropertyDefinition(MY_ID2);
        lenient().doReturn(Arrays.asList(extPropDef, extPropDef2)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        lenient().when(extPropDef.getPropertyId()).thenReturn(MY_ID);
        lenient().when(extPropDef.getDefaultValue(ipsObjectPartContainer)).thenReturn(MY_DEFAULT_VALUE);
        lenient().when(extPropDef.beforeSetValue(any(IIpsObjectPartContainer.class), any())).thenReturn(true);

        lenient().when(extPropDef2.getPropertyId()).thenReturn(MY_ID2);
        lenient().when(extPropDef2.getDefaultValue(ipsObjectPartContainer)).thenReturn(MY_DEFAULT_VALUE2);

        lenient().when(xmlRootElement.getOwnerDocument()).thenReturn(xmlDocument);
        lenient().when(xmlExtPropElement.getOwnerDocument()).thenReturn(xmlDocument);
        lenient().when(xmlDocument.createElement(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT))
                .thenReturn(xmlExtPropElement);
        lenient().when(xmlDocument.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT)).thenReturn(xmlValueElement);

        lenient().when(xmlExtPropElement.getNodeName()).thenReturn(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        lenient().when(xmlValueElement.getNodeName()).thenReturn(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        lenient().when(xmlValueElement.getNodeType()).thenReturn(Node.ELEMENT_NODE);

        lenient().when(xmlDocument.importNode(xmlValueElement, true)).thenReturn(xmlValueElement);

        lenient().when(documentBuilder.newDocument()).thenReturn(xmlDocument);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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

    @Test
    public void testGetExtPropertyValue_expectIllegalArgumentException() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            doReturn(Arrays.asList()).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

            extensionPropertyHandler.getExtPropertyValue(MY_ID);
        });
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

    @Test
    public void testCheckExtProperty_fail() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            extensionPropertyHandler.checkExtProperty(INVALID_ID);
        });
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
        lenient().when(xmlRootElement.getChildNodes()).thenReturn(rootNodeList);
        lenient().when(rootNodeList.getLength()).thenReturn(1);
        lenient().when(rootNodeList.item(0)).thenReturn(xmlExtPropElement);
        NodeList extPropNodeList = mock(NodeList.class);
        lenient().when(xmlExtPropElement.getChildNodes()).thenReturn(extPropNodeList);
        lenient().when(extPropNodeList.getLength()).thenReturn(1);
        lenient().when(extPropNodeList.item(0)).thenReturn(xmlValueElement);
        lenient().when(xmlValueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID)).thenReturn(propId);
        lenient().when(xmlValueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL)).thenReturn("" + isNull);
        lenient().when(xmlValueElement.getTagName()).thenReturn("InvalidExt");
        NamedNodeMap namedNodeMap = mock(NamedNodeMap.class);
        lenient().when(namedNodeMap.getLength()).thenReturn(2);
        Attr attr1 = mock(Attr.class);
        lenient().when(attr1.getName()).thenReturn(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        lenient().when(attr1.getValue()).thenReturn(propId);
        lenient().when(namedNodeMap.item(0)).thenReturn(attr1);
        Attr attr2 = mock(Attr.class);
        lenient().when(attr2.getName()).thenReturn(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        lenient().when(attr2.getValue()).thenReturn("" + isNull);
        lenient().when(namedNodeMap.item(1)).thenReturn(attr2);
        lenient().when(xmlValueElement.getAttributes()).thenReturn(namedNodeMap);
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
        doReturn(new ArrayList<>()).when(ipsObjectPartContainer)
                .getExtensionPropertyDefinitions();
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
        extensionPropertyHandler.removeObsoleteExtensionProperties();
        ExtensionPropertyMap map = extensionPropertyHandler.getExtPropertyValuesMap();

        assertEquals(0, map.values().size());
    }

}
