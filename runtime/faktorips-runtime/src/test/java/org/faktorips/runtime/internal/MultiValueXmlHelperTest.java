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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.annotation.IpsGenerated;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.ListUtil;
import org.faktorips.values.LocalizedString;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MultiValueXmlHelperTest extends XmlAbstractTestCase {

    @Test
    public void readValuesFromXML() {
        Element configElement = getConfigElement(0);

        List<String> valuesFromXML = MultiValueXmlHelper.getValuesFromXML(configElement);
        assertEquals(3, valuesFromXML.size());
        assertEquals("foo", valuesFromXML.get(0));
        assertNull(valuesFromXML.get(1));
        assertEquals("bar", valuesFromXML.get(2));
    }

    private Element getConfigElement(int index) {
        return (Element)getTestDocument().getDocumentElement().getElementsByTagName("ConfigElement").item(index);
    }

    @Test(expected = NullPointerException.class)
    public void readValuesFromXML_missingOuterValueTag() {
        Element configElement = getConfigElement(1);
        MultiValueXmlHelper.getValuesFromXML(configElement);
    }

    @Test(expected = NullPointerException.class)
    public void readValuesFromXML_missingMultiValueTag() {
        Element configElement = getConfigElement(2);
        MultiValueXmlHelper.getValuesFromXML(configElement);
    }

    @Test
    public void addValuesToElement() {
        List<String> stringList = new ArrayList<>();
        stringList.add("foo");
        stringList.add(null);
        stringList.add("bar");
        stringList.add("4711");

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addValuesToElement(attrValueElement, stringList);

        NodeList outerValueElementNodeList = attrValueElement.getChildNodes();
        assertEquals(1, outerValueElementNodeList.getLength());
        Element outerValueElement = (Element)outerValueElementNodeList.item(0);
        assertEquals("MultiValue", outerValueElement.getAttribute("valueType"));

        NodeList multiValueElementNodeList = outerValueElement.getChildNodes();
        assertEquals(1, multiValueElementNodeList.getLength());
        Element multiValueElement = (Element)multiValueElementNodeList.item(0);

        NodeList valueElementNodeList = multiValueElement.getChildNodes();
        assertEquals(4, valueElementNodeList.getLength());
        assertEquals("foo", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(0)));
        assertNull(ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(1)));
        assertEquals("bar", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(2)));
        assertEquals("4711", ValueToXmlHelper.getValueFromElement((Element)valueElementNodeList.item(3)));
    }

    @SuppressWarnings("removal")
    @Test
    public void testAddInternationalStringsToElement() {
        List<DefaultInternationalString> stringList = new ArrayList<>();
        stringList.add(new DefaultInternationalString(
                Arrays.asList(new LocalizedString(Locale.GERMAN, "Eins"), new LocalizedString(Locale.ENGLISH, "One")),
                Locale.GERMAN));
        stringList.add(new DefaultInternationalString(
                Arrays.asList(new LocalizedString(Locale.GERMAN, "Zwei"), new LocalizedString(Locale.ENGLISH, "Two")),
                Locale.GERMAN));

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addInternationalStringsToElement(attrValueElement, stringList);

        NodeList outerValueElementNodeList = attrValueElement.getChildNodes();
        assertEquals(1, outerValueElementNodeList.getLength());
        Element outerValueElement = (Element)outerValueElementNodeList.item(0);
        assertEquals("MultiValue", outerValueElement.getAttribute("valueType"));

        List<DefaultInternationalString> internationalStringsFromXML = MultiValueXmlHelper
                .getInternationalStringsFromXML(attrValueElement);

        assertEquals(2, internationalStringsFromXML.size());
        assertEquals("Eins", internationalStringsFromXML.get(0).get(Locale.GERMAN));
        assertEquals("One", internationalStringsFromXML.get(0).get(Locale.ENGLISH));
        assertEquals(Locale.GERMAN, internationalStringsFromXML.get(0).getDefaultLocale());
        assertEquals("Zwei", internationalStringsFromXML.get(1).get(Locale.GERMAN));
        assertEquals("Two", internationalStringsFromXML.get(1).get(Locale.ENGLISH));
        assertEquals(Locale.GERMAN, internationalStringsFromXML.get(1).getDefaultLocale());
    }

    @Test
    public void testAddInternationalStringsToElement_WithFallback() {
        List<DefaultInternationalString> stringList = new ArrayList<>();
        stringList.add(new DefaultInternationalString(
                Arrays.asList(new LocalizedString(Locale.GERMAN, "Eins"), new LocalizedString(Locale.ENGLISH, "One")),
                Locale.GERMAN));
        stringList.add(new DefaultInternationalString(
                Arrays.asList(new LocalizedString(Locale.GERMAN, "Zwei"), new LocalizedString(Locale.ENGLISH, "Two")),
                Locale.GERMAN));

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addInternationalStringsToElement(attrValueElement, stringList);

        NodeList outerValueElementNodeList = attrValueElement.getChildNodes();
        assertEquals(1, outerValueElementNodeList.getLength());
        Element outerValueElement = (Element)outerValueElementNodeList.item(0);
        assertEquals("MultiValue", outerValueElement.getAttribute("valueType"));
        MultiLingualProduct multiLingualProduct = new MultiLingualProduct(new InMemoryRuntimeRepository(), "ML 2025-07",
                "ML", "2025-07");

        List<DefaultInternationalString> internationalStringsFromXML = MultiValueXmlHelper
                .getInternationalStringsFromXML(attrValueElement, multiLingualProduct,
                        MultiLingualProduct.PROPERTY_MULTILINGUALMULTIVALUEATTRIBUTE);

        assertEquals(2, internationalStringsFromXML.size());
        assertEquals("Eins", internationalStringsFromXML.get(0).get(Locale.GERMAN));
        assertEquals("One", internationalStringsFromXML.get(0).get(Locale.ENGLISH));
        assertEquals("uno", internationalStringsFromXML.get(0).get(Locale.ITALIAN));
        assertEquals("Eins", internationalStringsFromXML.get(0).get(Locale.CHINESE));
        assertEquals(Locale.GERMAN, internationalStringsFromXML.get(0).getDefaultLocale());
        assertEquals("Zwei", internationalStringsFromXML.get(1).get(Locale.GERMAN));
        assertEquals("Two", internationalStringsFromXML.get(1).get(Locale.ENGLISH));
        assertEquals("due", internationalStringsFromXML.get(1).get(Locale.ITALIAN));
        assertEquals("Zwei", internationalStringsFromXML.get(1).get(Locale.CHINESE));
        assertEquals(Locale.GERMAN, internationalStringsFromXML.get(1).getDefaultLocale());
    }

    @Test
    public void roundTripTest() {
        List<String> stringList = new ArrayList<>();
        stringList.add("foo");
        stringList.add(null);
        stringList.add("bar");
        stringList.add("4711");

        Element attrValueElement = getTestDocument().createElement(ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE);
        MultiValueXmlHelper.addValuesToElement(attrValueElement, stringList);
        List<String> resultList = MultiValueXmlHelper.getValuesFromXML(attrValueElement);
        assertNotSame(stringList, resultList);
        assertEquals(stringList, resultList);
    }

    @IpsProductCmptType(name = "productComponentTests.MultiLingualProduct")
    @IpsAttributes({ "multiLingualMultiValueAttribute" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.internal.model-label-and-descriptions", defaultLocale = "en")
    public static class MultiLingualProduct extends ProductComponent {

        public static final String PROPERTY_MULTILINGUALMULTIVALUEATTRIBUTE = "multiLingualMultiValueAttribute";
        @IpsAllowedValues("multiLingualMultiValueAttribute")
        public static final ValueSet<DefaultInternationalString> MAX_ALLOWED_VALUES_FOR_MULTILINGUALMULTIVALUEATTRIBUTE = new UnrestrictedValueSet<>(
                true);
        @IpsDefaultValue("multiLingualMultiValueAttribute")
        public static final List<DefaultInternationalString> DEFAULT_VALUE_FOR_MULTILINGUALMULTIVALUEATTRIBUTE = ListUtil
                .newList(DefaultInternationalString.EMPTY);
        private List<DefaultInternationalString> multiLingualMultiValueAttribute = DEFAULT_VALUE_FOR_MULTILINGUALMULTIVALUEATTRIBUTE;

        @IpsGenerated
        public MultiLingualProduct(IRuntimeRepository repository, String id, String kindId, String versionId) {
            super(repository, id, kindId, versionId);
        }

        @Override
        @IpsGenerated
        public boolean isChangingOverTime() {
            return false;
        }

        @IpsAttribute(name = "multiLingualMultiValueAttribute", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)
        @IpsGenerated
        public List<DefaultInternationalString> getMultiLingualMultiValueAttribute() {
            return new ArrayList<>(multiLingualMultiValueAttribute);
        }

        @IpsGenerated
        public List<String> getMultiLingualMultiValueAttribute(Locale locale) {
            List<String> result = new ArrayList<>();
            for (DefaultInternationalString internationalString : multiLingualMultiValueAttribute) {
                result.add(internationalString.get(locale));
            }
            return result;
        }

        @IpsAttributeSetter("multiLingualMultiValueAttribute")
        @IpsGenerated
        public void setMultiLingualMultiValueAttribute(List<DefaultInternationalString> newValue) {
            if (getRepository() != null && !getRepository().isModifiable()) {
                throw new IllegalRepositoryModificationException();
            }
            setMultiLingualMultiValueAttributeInternal(new ArrayList<>(newValue));
        }

        @IpsGenerated
        protected final void setMultiLingualMultiValueAttributeInternal(List<DefaultInternationalString> newValue) {
            multiLingualMultiValueAttribute = newValue;
        }

        @Override
        @IpsGenerated
        protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
            super.doInitPropertiesFromXml(configMap);
            doInitMultiLingualMultiValueAttribute(configMap);
        }

        @IpsGenerated
        private void doInitMultiLingualMultiValueAttribute(Map<String, Element> configMap) {
            Element configElement = configMap.get(PROPERTY_MULTILINGUALMULTIVALUEATTRIBUTE);
            if (configElement != null) {
                List<DefaultInternationalString> valueList = MultiValueXmlHelper
                        .getInternationalStringsFromXML(configElement, this, PROPERTY_MULTILINGUALMULTIVALUEATTRIBUTE);
                multiLingualMultiValueAttribute = valueList;
            }
        }

        @Override
        @IpsGenerated
        public IConfigurableModelObject createPolicyComponent() {
            throw new UnsupportedOperationException(
                    "This product component type does not configure a policy component type.");
        }
    }
}
