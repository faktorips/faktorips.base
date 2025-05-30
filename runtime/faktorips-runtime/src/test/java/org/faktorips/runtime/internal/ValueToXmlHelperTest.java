/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.valueset.StringLengthValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author Thorsten Guenther
 */
public class ValueToXmlHelperTest extends XmlAbstractTestCase {

    @Test
    public void testAddValueToElement() {
        Document doc = getTestDocument();
        Element node = doc.createElement("ParentEl");
        assertEquals(0, node.getChildNodes().getLength());
        ValueToXmlHelper.addValueToElement("Value", node, "Property");
        assertEquals("Value", ValueToXmlHelper.getValueFromElement(node, "Property"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        ValueToXmlHelper.addValueToElement(null, node, "ValueNode");
        node = (Element)node.getElementsByTagName("ValueNode").item(0);
        assertEquals("true", node.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL));
    }

    @Test
    public void testAddValueToElement_NullValue() {
        Document doc = getTestDocument();
        Element node = doc.createElement("ParentEl");
        assertEquals(0, node.getChildNodes().getLength());
        ValueToXmlHelper.addValueToElement(null, node, "Property");
        assertNull(ValueToXmlHelper.getValueFromElement(node, "Property"));
        assertFalse(node.hasAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL));
    }

    @Test
    public void testDeleteExistingElementAndCreateNewElement() {
        Document doc = getTestDocument();
        Element parent = doc.createElement("ParentElement");
        Element node = doc.createElement("TestElement");
        node.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE, "testAttribute");
        parent.appendChild(node);
        assertEquals(1, parent.getElementsByTagName("TestElement").getLength());
        ValueToXmlHelper.deleteExistingElementAndCreateNewElement(parent, "TestElement", "testAttribute");
        assertEquals(1, parent.getElementsByTagName("TestElement").getLength());
    }

    @Test
    public void testGetValueFromElement() {
        Document doc = getTestDocument();

        Element node = (Element)doc.getDocumentElement().getElementsByTagName("TestElement").item(0);
        assertEquals("cdataValue", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyValueTestElement").item(0);
        assertEquals("", ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("NullTestElement").item(0);
        assertNull(ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        assertNull(ValueToXmlHelper.getValueFromElement(node, "ValueNode"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("TextNodeElement").item(0);
        assertEquals("42", ValueToXmlHelper.getValueFromElement(node, "Property"));
    }

    @Test
    public void testGetRangeFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(0);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNotNull(range);
        assertEquals("100", range.getLower());
        assertEquals("200", range.getUpper());
        assertEquals("10", range.getStep());
        assertFalse(range.containsNull());
        assertFalse(range.isEmpty());

        node = (Element)configElements.item(1);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNull(range);

        node = (Element)configElements.item(2);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNull(range);

        node = (Element)configElements.item(7);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertNotNull(range);
        assertTrue(range.isEmpty());
        assertFalse(range.containsNull());
        assertNull(range.getLower());
        assertNull(range.getUpper());
        assertNull(range.getStep());
    }

    @Test
    public void testGetEnumValueSetFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(3);
        EnumValues enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertNotNull(enumValues);
        assertEquals(2, enumValues.getNumberOfValues());
        assertEquals("10.0", enumValues.getValue(0));
        assertEquals("20.0", enumValues.getValue(1));
        assertFalse(enumValues.containsNull());

        node = (Element)configElements.item(4);
        enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertNotNull(enumValues);
        assertEquals(3, enumValues.getNumberOfValues());
        assertEquals("j", enumValues.getValue(0));
        assertEquals("h", enumValues.getValue(1));
        assertTrue(enumValues.containsNull());
    }

    @Test
    public void testGetStringLengthValueSetFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(8);

        StringLengthValueSet valueSet = ValueToXmlHelper.getStringLengthValueSetFromElement(node, "ValueSet");

        assertThat(valueSet.containsNull(), is(false));
        assertThat(valueSet.getMaximumLength(), is(15));
    }

    @Test
    public void testGetStringLengthValueSetFromElement_Unlimited() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(9);

        StringLengthValueSet valueSet = ValueToXmlHelper.getStringLengthValueSetFromElement(node, "ValueSet");

        assertThat(valueSet.containsNull(), is(true));
        assertThat(valueSet.getMaximumLength(), is(nullValue()));
    }

    @Test
    public void testGetUnrestrictedValueSet_containsNull() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");

        Element node = (Element)configElements.item(5);
        UnrestrictedValueSet<String> valueSet = ValueToXmlHelper.getUnrestrictedValueSet(node, "ValueSet");
        assertTrue(valueSet.containsNull());

        node = (Element)configElements.item(6);
        valueSet = ValueToXmlHelper.getUnrestrictedValueSet(node, "ValueSet");
        assertFalse(valueSet.containsNull());
    }

    @Test
    public void testAddTableUsageToElement() {
        Element element = getTestDocument().getDocumentElement();
        NodeList childNodes = element.getChildNodes();
        assertEquals(33, childNodes.getLength());

        ValueToXmlHelper.addTableUsageToElement(element, "structureUsageValue", "tableContentNameValue");

        assertEquals(34, childNodes.getLength());
        Node namedItem = childNodes.item(33).getAttributes().getNamedItem("structureUsage");
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        String nodeValue = childNodes.item(33).getFirstChild().getTextContent();
        assertEquals("tableContentNameValue", nodeValue);
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetInternationalStringFromElement() {
        Element attributeValueElement = (Element)getTestDocument().getDocumentElement()
                .getElementsByTagName("AttributeValue").item(0);

        DefaultInternationalString internationalString = ValueToXmlHelper.getInternationalStringFromElement(
                attributeValueElement, "Value");

        assertEquals("Wrong default locale", Locale.of("hy"), internationalString.getDefaultLocale());
        assertEquals("Wrong value for locale 'as'", "asfdsa", internationalString.get(Locale.of("as")));
        assertEquals("Wrong value for locale 'hy'", "hyfds", internationalString.get(Locale.of("hy")));
        assertEquals("Wrong value for undefined locale 'ko'", "hyfds", internationalString.get(Locale.of("ko")));
    }

    @Test
    public void testGetInternationalStringFromElement_WithFallback() {
        Element attributeValueElement = (Element)getTestDocument().getDocumentElement()
                .getElementsByTagName("AttributeValue").item(0);
        MultiLingualProduct multiLingualProduct = new MultiLingualProduct(new InMemoryRuntimeRepository(), "ML 2025-07",
                "ML", "2025-07");

        DefaultInternationalString internationalString = ValueToXmlHelper.getInternationalStringFromElement(
                attributeValueElement, "Value", multiLingualProduct, MultiLingualProduct.PROPERTY_MLATTRIBUTE);

        assertEquals("Wrong default locale", Locale.of("hy"), internationalString.getDefaultLocale());
        assertEquals("Wrong value for locale 'as'", "asfdsa", internationalString.get(Locale.of("as")));
        assertEquals("Wrong value for locale 'hy'", "hyfds", internationalString.get(Locale.of("hy")));
        assertEquals("Wrong value for i18n locale 'it'", "Attributo internazionalizzato",
                internationalString.get(Locale.ITALIAN));
        assertEquals("Wrong value for undefined locale 'ko'", "hyfds", internationalString.get(Locale.of("ko")));
    }

    @Test
    public void testIsAttributeTrue_False() {
        Element allValuesElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("AllValues")
                .item(0);

        assertFalse(ValueToXmlHelper.isAttributeTrue(allValuesElement, "containsNull"));
    }

    @Test
    public void testIsAttributeTrue_True() {
        Element valueNodeElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("ValueNode")
                .item(2);

        assertTrue(ValueToXmlHelper.isAttributeTrue(valueNodeElement, "isNull"));
    }

    @Test
    public void testIsAttributeTrue_NotSet() {
        Element testElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("TestElement")
                .item(0);

        assertFalse(ValueToXmlHelper.isAttributeTrue(testElement, "foobar"));
    }

    @IpsProductCmptType(name = "mlTest.MultiLingualProduct")
    @IpsAttributes({ "mlAttribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.internal.model-label-and-descriptions", defaultLocale = "en")
    public class MultiLingualProduct extends ProductComponent implements IToXmlSupport {

        public static final String PROPERTY_MLATTRIBUTE = "mlAttribute";
        @IpsAllowedValues("mlAttribute")
        public static final ValueSet<DefaultInternationalString> MAX_ALLOWED_VALUES_FOR_MLATTRIBUTE = new UnrestrictedValueSet<>(
                true);
        @IpsDefaultValue("mlAttribute")
        public static final DefaultInternationalString DEFAULT_VALUE_FOR_MLATTRIBUTE = DefaultInternationalString.EMPTY;
        private DefaultInternationalString mlAttribute = DEFAULT_VALUE_FOR_MLATTRIBUTE;

        public MultiLingualProduct(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        public boolean isChangingOverTime() {
            return false;
        }

        @IpsAttribute(name = "mlAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        public DefaultInternationalString getStaticAttribute() {
            return mlAttribute;
        }

        public String getStaticAttribute(Locale locale) {
            return mlAttribute.get(locale);
        }

        @IpsAttributeSetter("mlAttribute")
        public void setStaticAttribute(DefaultInternationalString newValue) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            setStaticAttributeInternal(newValue);
        }

        protected final void setStaticAttributeInternal(DefaultInternationalString newValue) {
            mlAttribute = newValue;
        }

        @Override
        protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
            super.doInitPropertiesFromXml(configMap);
            doInitStaticAttribute(configMap);
        }

        private void doInitStaticAttribute(Map<String, Element> configMap) {
            Element configElement = configMap.get(PROPERTY_MLATTRIBUTE);
            if (configElement != null) {
                DefaultInternationalString value = ValueToXmlHelper.getInternationalStringFromElement(configElement,
                        ValueToXmlHelper.XML_TAG_VALUE, this, PROPERTY_MLATTRIBUTE);
                mlAttribute = value;
            }
        }

        @Override
        public void writePropertiesToXml(Element element) {
            element.setAttribute("productCmptType", "productComponentTests.MultiLingualProduct");
            element.setAttribute("runtimeId", getId());
            writeStaticAttribute(element);
        }

        private void writeStaticAttribute(Element element) {
            Element attributeElement = ValueToXmlHelper.deleteExistingElementAndCreateNewElement(element,
                    ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE, PROPERTY_MLATTRIBUTE);
            ValueToXmlHelper.addInternationalStringToElement(mlAttribute, attributeElement,
                    ValueToXmlHelper.XML_TAG_VALUE);
            element.appendChild(attributeElement);
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            throw new UnsupportedOperationException(
                    "This product component type does not configure a policy component type.");
        }

    }

}
