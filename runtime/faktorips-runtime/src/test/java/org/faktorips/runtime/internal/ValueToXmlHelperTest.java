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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
import org.faktorips.valueset.DerivedValueSet;
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
        assertThat(node.getChildNodes().getLength(), is(0));
        ValueToXmlHelper.addValueToElement("Value", node, "Property");
        assertThat(ValueToXmlHelper.getValueFromElement(node, "Property"), is("Value"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        ValueToXmlHelper.addValueToElement(null, node, "ValueNode");
        node = (Element)node.getElementsByTagName("ValueNode").item(0);
        assertThat(node.getAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL), is("true"));
    }

    @Test
    public void testAddValueToElement_NullValue() {
        Document doc = getTestDocument();
        Element node = doc.createElement("ParentEl");
        assertThat(node.getChildNodes().getLength(), is(0));
        ValueToXmlHelper.addValueToElement(null, node, "Property");
        assertThat(ValueToXmlHelper.getValueFromElement(node, "Property"), is(nullValue()));
        assertThat(node.hasAttribute(ValueToXmlHelper.XML_ATTRIBUTE_IS_NULL), is(false));
    }

    @Test
    public void testDeleteExistingElementAndCreateNewElement() {
        Document doc = getTestDocument();
        Element parent = doc.createElement("ParentElement");
        Element node = doc.createElement("TestElement");
        node.setAttribute(ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE, "testAttribute");
        parent.appendChild(node);
        assertThat(parent.getElementsByTagName("TestElement").getLength(), is(1));
        ValueToXmlHelper.deleteExistingElementAndCreateNewElement(parent, "TestElement", "testAttribute");
        assertThat(parent.getElementsByTagName("TestElement").getLength(), is(1));
    }

    @Test
    public void testGetValueFromElement() {
        Document doc = getTestDocument();

        Element node = (Element)doc.getDocumentElement().getElementsByTagName("TestElement").item(0);
        assertThat(ValueToXmlHelper.getValueFromElement(node, "ValueNode"), is("cdataValue"));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyValueTestElement").item(0);
        assertThat(ValueToXmlHelper.getValueFromElement(node, "ValueNode"), is(""));

        node = (Element)doc.getDocumentElement().getElementsByTagName("NullTestElement").item(0);
        assertThat(ValueToXmlHelper.getValueFromElement(node, "ValueNode"), is(nullValue()));

        node = (Element)doc.getDocumentElement().getElementsByTagName("EmptyTestElement").item(0);
        assertThat(ValueToXmlHelper.getValueFromElement(node, "ValueNode"), is(nullValue()));

        node = (Element)doc.getDocumentElement().getElementsByTagName("TextNodeElement").item(0);
        assertThat(ValueToXmlHelper.getValueFromElement(node, "Property"), is("42"));
    }

    @Test
    public void testGetRangeFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(0);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.getLower(), is("100"));
        assertThat(range.getUpper(), is("200"));
        assertThat(range.getStep(), is("10"));
        assertThat(range.containsNull(), is(false));
        assertThat(range.isEmpty(), is(false));

        node = (Element)configElements.item(1);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(nullValue()));

        node = (Element)configElements.item(2);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(nullValue()));

        node = (Element)configElements.item(7);
        range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.isEmpty(), is(true));
        assertThat(range.containsNull(), is(false));
        assertThat(range.getLower(), is(nullValue()));
        assertThat(range.getUpper(), is(nullValue()));
        assertThat(range.getStep(), is(nullValue()));
    }

    @Test
    public void testGetRangeFromElement_WithBothOpenBounds() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(10);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.getLower(), is("5"));
        assertThat(range.getUpper(), is("10"));
        assertThat(range.getStep(), is(nullValue()));
        assertThat(range.containsNull(), is(false));
        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(true));
    }

    @Test
    public void testGetRangeFromElement_WithLowerOpenBound() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(11);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.getLower(), is("0"));
        assertThat(range.getUpper(), is("100"));
        assertThat(range.getStep(), is("10"));
        assertThat(range.containsNull(), is(true));
        assertThat(range.isLowerBoundOpen(), is(true));
        assertThat(range.isUpperBoundOpen(), is(false));
    }

    @Test
    public void testGetRangeFromElement_WithUpperOpenBound() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(12);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.getLower(), is("0"));
        assertThat(range.getUpper(), is("50"));
        assertThat(range.getStep(), is(nullValue()));
        assertThat(range.containsNull(), is(false));
        assertThat(range.isLowerBoundOpen(), is(false));
        assertThat(range.isUpperBoundOpen(), is(true));
    }

    @Test
    public void testGetRangeFromElement_DefaultClosedBounds() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(0);
        Range range = ValueToXmlHelper.getRangeFromElement(node, "ValueSet");
        assertThat(range, is(not(nullValue())));
        assertThat(range.isLowerBoundOpen(), is(false));
        assertThat(range.isUpperBoundOpen(), is(false));
    }

    @Test
    public void testGetEnumValueSetFromElement() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");
        Element node = (Element)configElements.item(3);
        EnumValues enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertThat(enumValues, is(not(nullValue())));
        assertThat(enumValues.getNumberOfValues(), is(2));
        assertThat(enumValues.getValue(0), is("10.0"));
        assertThat(enumValues.getValue(1), is("20.0"));
        assertThat(enumValues.containsNull(), is(false));

        node = (Element)configElements.item(4);
        enumValues = ValueToXmlHelper.getEnumValueSetFromElement(node, "ValueSet");
        assertThat(enumValues, is(not(nullValue())));
        assertThat(enumValues.getNumberOfValues(), is(3));
        assertThat(enumValues.getValue(0), is("j"));
        assertThat(enumValues.getValue(1), is("h"));
        assertThat(enumValues.containsNull(), is(true));
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
        assertThat(valueSet.containsNull(), is(true));

        node = (Element)configElements.item(6);
        valueSet = ValueToXmlHelper.getUnrestrictedValueSet(node, "ValueSet");
        assertThat(valueSet.containsNull(), is(false));
    }

    @Test
    public void testGetDerivedValueSet_containsNullFromAllValues() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");

        Element node = (Element)configElements.item(13);
        DerivedValueSet<String> valueSet = ValueToXmlHelper.getDerivedValueSet(node, "ValueSet");
        assertThat(valueSet.containsNull(), is(false));
    }

    @Test
    public void testGetDerivedValueSet_containsNullDefaultsToTrue() {
        Document doc = getTestDocument();
        NodeList configElements = doc.getDocumentElement().getElementsByTagName("ConfigElement");

        Element node = (Element)configElements.item(14);
        DerivedValueSet<String> valueSet = ValueToXmlHelper.getDerivedValueSet(node, "ValueSet");
        assertThat(valueSet.containsNull(), is(true));
    }

    @Test
    public void testAddTableUsageToElement() {
        Element element = getTestDocument().getDocumentElement();
        NodeList childNodes = element.getChildNodes();
        assertThat(childNodes.getLength(), is(43));

        ValueToXmlHelper.addTableUsageToElement(element, "structureUsageValue", "tableContentNameValue");

        assertThat(childNodes.getLength(), is(44));
        Node namedItem = childNodes.item(43).getAttributes().getNamedItem("structureUsage");
        assertThat(namedItem.getNodeValue(), is("structureUsageValue"));
        String nodeValue = childNodes.item(43).getFirstChild().getTextContent();
        assertThat(nodeValue, is("tableContentNameValue"));
    }

    @SuppressWarnings("removal")
    @Test
    public void testGetInternationalStringFromElement() {
        Element attributeValueElement = (Element)getTestDocument().getDocumentElement()
                .getElementsByTagName("AttributeValue").item(0);

        DefaultInternationalString internationalString = ValueToXmlHelper.getInternationalStringFromElement(
                attributeValueElement, "Value");

        assertThat(internationalString.getDefaultLocale(), is(Locale.of("hy")));
        assertThat(internationalString.get(Locale.of("as")), is("asfdsa"));
        assertThat(internationalString.get(Locale.of("hy")), is("hyfds"));
        assertThat(internationalString.get(Locale.of("ko")), is("hyfds"));
    }

    @Test
    public void testGetInternationalStringFromElement_WithFallback() {
        Element attributeValueElement = (Element)getTestDocument().getDocumentElement()
                .getElementsByTagName("AttributeValue").item(0);
        MultiLingualProduct multiLingualProduct = new MultiLingualProduct(new InMemoryRuntimeRepository(), "ML 2025-07",
                "ML", "2025-07");

        DefaultInternationalString internationalString = ValueToXmlHelper.getInternationalStringFromElement(
                attributeValueElement, "Value", multiLingualProduct, MultiLingualProduct.PROPERTY_MLATTRIBUTE);

        assertThat(internationalString.getDefaultLocale(), is(Locale.of("hy")));
        assertThat(internationalString.get(Locale.of("as")), is("asfdsa"));
        assertThat(internationalString.get(Locale.of("hy")), is("hyfds"));
        assertThat(internationalString.get(Locale.ITALIAN), is("Attributo internazionalizzato"));
        assertThat(internationalString.get(Locale.of("ko")), is("hyfds"));
    }

    @Test
    public void testIsAttributeTrue_False() {
        Element allValuesElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("AllValues")
                .item(0);

        assertThat(ValueToXmlHelper.isAttributeTrue(allValuesElement, "containsNull"), is(false));
    }

    @Test
    public void testIsAttributeTrue_True() {
        Element valueNodeElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("ValueNode")
                .item(2);

        assertThat(ValueToXmlHelper.isAttributeTrue(valueNodeElement, "isNull"), is(true));
    }

    @Test
    public void testIsAttributeTrue_NotSet() {
        Element testElement = (Element)getTestDocument().getDocumentElement().getElementsByTagName("TestElement")
                .item(0);

        assertThat(ValueToXmlHelper.isAttributeTrue(testElement, "foobar"), is(false));
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
