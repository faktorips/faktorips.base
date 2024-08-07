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

import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_ATTRIBUTE_ATTRIBUTE;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_ATTRIBUTE_VALUE;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_CONFIGURED_DEFAULT;
import static org.faktorips.runtime.internal.ValueToXmlHelper.XML_TAG_CONFIGURED_VALUE_SET;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProductComponentTest extends XmlAbstractTestCase {

    @Mock
    private IRuntimeRepository repository;

    private ProductComponent pc;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    // the verify for the parameterized map cannot be type safe
    @Test
    public void testCallInitMethodsOnInitFromXML() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        Element element = setUpElement();

        cmpt.initFromXml(element);

        verify(cmpt).doInitPropertiesFromXml(anyMap());
        verify(cmpt).doInitReferencesFromXml(anyMap());
        verify(cmpt).doInitTableUsagesFromXml(anyMap());
        verify(cmpt).doInitFormulaFromXml(element);
    }

    private Element setUpElement() {
        Element element = mock(Element.class);
        Element validToElement = mock(Element.class);
        NodeList nodeList = mock(NodeList.class);
        NodeList emptyNodeList = mock(NodeList.class);

        when(element.getElementsByTagName(anyString())).thenReturn(nodeList);
        when(nodeList.item(0)).thenReturn(validToElement);
        when(element.getChildNodes()).thenReturn(emptyNodeList);

        return element;
    }

    @Test
    public void testGetLinks() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        List<IProductComponentLink<? extends IProductComponent>> links = cmpt.getLinks();

        assertEquals(0, links.size());
    }

    @Test
    public void testGetLinkForName() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        IProductComponentLink<? extends IProductComponent> link = cmpt.getLink("", null);

        assertNull(link);
    }

    @Test
    public void testCallWriteMethodsOnToXML() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        Document document = mock(Document.class);
        Element prodCmptElement = mock(Element.class);
        Document ownerDocument = mock(Document.class);
        Element validToElement = mock(Element.class);
        NodeList childList = mock(NodeList.class);
        when(document.createElement("ProductCmpt")).thenReturn(prodCmptElement);
        when(prodCmptElement.getOwnerDocument()).thenReturn(ownerDocument);
        when(prodCmptElement.getChildNodes()).thenReturn(childList);
        when(childList.getLength()).thenReturn(0);
        when(ownerDocument.createElement("validTo")).thenReturn(validToElement);
        when(cmpt.getRepository().getNumberOfProductComponentGenerations(cmpt)).thenReturn(1);
        doNothing().when(prodCmptElement).setAttribute(anyString(), anyString());

        cmpt.toXml(document, false);

        verify(cmpt).writePropertiesToXml(prodCmptElement);
        verify(cmpt).writeTableUsagesToXml(prodCmptElement);
        verify(cmpt).writeReferencesToXml(prodCmptElement);
        verify(cmpt).writeExtensionPropertiesToXml(prodCmptElement);
    }

    @Test
    public void testSortAttributesValueSetsAndDefaultValues() {
        Document document = XmlUtil.getDocumentBuilder().newDocument();
        ProductComponentTestClass cmpt = new ProductComponentTestClass(repository) {
            @Override
            public void writePropertiesToXml(Element parent) {
                addElement(parent, "f", XML_TAG_ATTRIBUTE_VALUE);
                addElement(parent, "b", XML_TAG_CONFIGURED_VALUE_SET);
                addElement(parent, "b", XML_TAG_CONFIGURED_DEFAULT);
                // the order of default values and value sets might be different when written with
                // older versions of Faktor-IPS
                addElement(parent, "d", XML_TAG_CONFIGURED_DEFAULT);
                addElement(parent, "d", XML_TAG_CONFIGURED_VALUE_SET);
                addElement(parent, "a", XML_TAG_CONFIGURED_DEFAULT);
                addElement(parent, "c", XML_TAG_ATTRIBUTE_VALUE);
                addElement(parent, "a", XML_TAG_CONFIGURED_VALUE_SET);
                addElement(parent, "e", XML_TAG_ATTRIBUTE_VALUE);
            }

            private void addElement(Element parent, String attribute, String xmlTag) {
                Element childElement = document.createElement(xmlTag);
                childElement.setAttribute(XML_ATTRIBUTE_ATTRIBUTE, attribute);
                parent.appendChild(childElement);
            }
        };
        when(cmpt.getRepository().getNumberOfProductComponentGenerations(cmpt)).thenReturn(0);

        Element xml = cmpt.toXml(document, false);

        List<Element> childElements = XmlUtil.getChildElements(xml, XML_TAG_ATTRIBUTE_VALUE,
                XML_TAG_CONFIGURED_VALUE_SET, XML_TAG_CONFIGURED_DEFAULT);
        assertThat(childElements.size(), is(9));
        assertThat(childElements.get(0).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("c"));
        assertThat(childElements.get(0).getNodeName(), is(XML_TAG_ATTRIBUTE_VALUE));
        assertThat(childElements.get(1).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("e"));
        assertThat(childElements.get(1).getNodeName(), is(XML_TAG_ATTRIBUTE_VALUE));
        assertThat(childElements.get(2).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("f"));
        assertThat(childElements.get(2).getNodeName(), is(XML_TAG_ATTRIBUTE_VALUE));
        assertThat(childElements.get(3).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("a"));
        assertThat(childElements.get(3).getNodeName(), is(XML_TAG_CONFIGURED_VALUE_SET));
        assertThat(childElements.get(4).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("a"));
        assertThat(childElements.get(4).getNodeName(), is(XML_TAG_CONFIGURED_DEFAULT));
        assertThat(childElements.get(5).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("b"));
        assertThat(childElements.get(5).getNodeName(), is(XML_TAG_CONFIGURED_VALUE_SET));
        assertThat(childElements.get(6).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("b"));
        assertThat(childElements.get(6).getNodeName(), is(XML_TAG_CONFIGURED_DEFAULT));
        assertThat(childElements.get(7).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("d"));
        assertThat(childElements.get(7).getNodeName(), is(XML_TAG_CONFIGURED_VALUE_SET));
        assertThat(childElements.get(8).getAttribute(XML_ATTRIBUTE_ATTRIBUTE), is("d"));
        assertThat(childElements.get(8).getNodeName(), is(XML_TAG_CONFIGURED_DEFAULT));
    }

    @Test
    public void testWriteTableUsageToXml() {
        Element prodCmptElement = getTestDocument().getDocumentElement();
        NodeList childNodes = prodCmptElement.getChildNodes();
        assertEquals(21, childNodes.getLength());

        pc.writeTableUsageToXml(prodCmptElement, "structureUsageValue", "tableContentNameValue");

        assertEquals(22, childNodes.getLength());
        Node node = childNodes.item(21);
        Node namedItem = node.getAttributes().getNamedItem("structureUsage");
        String nodeValue = node.getFirstChild().getTextContent();
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        assertEquals("tableContentNameValue", nodeValue);
    }

    @Test
    public void testWriteTableUsageToXml_NoUsage() {
        Element prodCmptElement = getTestDocument().getDocumentElement();
        NodeList childNodes = prodCmptElement.getChildNodes();
        assertEquals(21, childNodes.getLength());

        pc.writeTableUsageToXml(prodCmptElement, "structureUsageValue", null);

        assertEquals(22, childNodes.getLength());
        Node node = childNodes.item(21);
        Node namedItem = node.getAttributes().getNamedItem("structureUsage");
        Node tableContentNameNode = node.getFirstChild();
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        assertEquals("", tableContentNameNode.getTextContent());
        assertFalse(tableContentNameNode.hasAttributes());
    }

    @Test
    public void testWriteImplementationClassToXml() {
        Element prodCmptElement = pc.toXml(XmlUtil.getDocumentBuilder().newDocument());

        assertThat(prodCmptElement.getAttribute(ProductComponent.PROPERTY_IMPLEMENTATION_CLASS),
                is(TestProductComponent.class.getName()));
    }

    @Test
    public void testIsFormulaAvailable() {
        Element genElement = getTestDocument().getDocumentElement();
        pc.initFromXml(genElement);

        assertTrue(pc.isFormulaAvailable("testFormula"));
        assertFalse(pc.isFormulaAvailable("emptyFormula"));
        assertFalse(pc.isFormulaAvailable("notExistingFormula"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetGenerationBase_ThrowUnsupportedOperationExceptionIfNotChangingOverTime() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        when(cmpt.isChangingOverTime()).thenReturn(false);

        cmpt.getGenerationBase(new GregorianCalendar());
    }

    @Test
    public void testGetGenerationBase_ReturnGenerationBaseIfChangingOverTime() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        when(cmpt.isChangingOverTime()).thenReturn(true);
        IProductComponentGeneration productComponentGeneration = mock(IProductComponentGeneration.class);

        when(repository.getProductComponentGenerations(cmpt)).thenReturn(List.of(productComponentGeneration));
        when(repository.getProductComponentGeneration("id", new GregorianCalendar(1, 1, 1900)))
                .thenReturn(productComponentGeneration);

        assertEquals(productComponentGeneration, cmpt.getGenerationBase(new GregorianCalendar(1, 1, 1900)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetLatestProductComponentGeneration_ThrowUnsupportedOperationExceptionIfNotChangingOverTime() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        when(cmpt.isChangingOverTime()).thenReturn(false);
        cmpt.getLatestProductComponentGeneration();
    }

    @Test
    public void testGetLatestProductComponentGeneration_ReturnLatestGenerationIfChangingOverTime() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository));
        when(cmpt.isChangingOverTime()).thenReturn(true);
        IProductComponentGeneration productComponentGeneration = mock(IProductComponentGeneration.class);

        when(repository.getProductComponentGenerations(cmpt)).thenReturn(List.of(productComponentGeneration));
        when(repository.getLatestProductComponentGeneration(cmpt)).thenReturn(productComponentGeneration);

        assertEquals(productComponentGeneration, cmpt.getLatestProductComponentGeneration());
    }

    @Test
    public void testSetValidFrom() {
        when(repository.isModifiable()).thenReturn(true);

        pc.setValidFrom(new DateTime(2010, 1, 1));

        assertEquals(new DateTime(2010, 1, 1), pc.getValidFrom());
    }

    @Test
    public void testSetValidFrom_noRuntimeRepository() {
        pc = spy(pc);
        when(pc.getRepository()).thenReturn(null);

        pc.setValidFrom(new DateTime(2010, 1, 1));

        assertEquals(new DateTime(2010, 1, 1), pc.getValidFrom());
    }

    @Test(expected = IllegalRepositoryModificationException.class)
    public void testSetValidFrom_throwExceptionIfRepositoryNotModifiable() {
        when(repository.isModifiable()).thenReturn(false);

        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");

        pc.setValidFrom(new DateTime(2010, 1, 1));
    }

    @Test(expected = NullPointerException.class)
    public void testSetValidFrom_throwExceptionIfValidFromIsNull() {
        when(repository.isModifiable()).thenReturn(true);
        pc.setValidFrom(null);
    }

    @Test
    public void testDescription_existingLanguage() {
        pc.initFromXml(getTestDocument().getDocumentElement());

        assertEquals("English description.", pc.getDescription(Locale.ENGLISH));
        assertEquals("Deutsche Beschreibung.", pc.getDescription(Locale.GERMAN));
        assertEquals("Je ne parle pas français.", pc.getDescription(Locale.FRENCH));
    }

    @Test
    public void testDescription_existingLanguage_fallback() {
        pc.initFromXml(getTestDocument().getDocumentElement());

        assertEquals("English description.", pc.getDescription(Locale.US));
        assertEquals("English description.", pc.getDescription(Locale.UK));
        assertEquals("English description.", pc.getDescription(Locale.CANADA));
        assertEquals("Deutsche Beschreibung.", pc.getDescription(Locale.GERMANY));
        assertEquals("Je ne parle pas français.", pc.getDescription(Locale.FRANCE));
        assertEquals("Je ne parle pas français.", pc.getDescription(Locale.CANADA_FRENCH));
    }

    @Test
    public void testDescription_nonExistingLanguage() {
        pc.initFromXml(getTestDocument().getDocumentElement());

        // English is currently considered the default language as it is defined as the first
        // language in the XML
        assertEquals("English description.", pc.getDescription(Locale.CHINESE));
    }

    @Test
    public void testWriteDescriptionToXml() {
        pc.initFromXml(getTestDocument().getDocumentElement());
        Document newDocument = newDocument();

        Element productElement = pc.toXml(newDocument);

        NodeList desriptionNodes = productElement.getElementsByTagName("Description");
        assertEquals(3, desriptionNodes.getLength());
        assertEquals("en", ((Element)desriptionNodes.item(0)).getAttribute("locale"));
        assertEquals("English description.", ((Element)desriptionNodes.item(0)).getTextContent());
        assertEquals("fr", ((Element)desriptionNodes.item(1)).getAttribute("locale"));
        assertEquals("Je ne parle pas français.", ((Element)desriptionNodes.item(1)).getTextContent());
        assertEquals("de", ((Element)desriptionNodes.item(2)).getAttribute("locale"));
        assertEquals("Deutsche Beschreibung.", ((Element)desriptionNodes.item(2)).getTextContent());
    }

    @Test
    public void testIsVariant_variant_shouldReturnTrue() {
        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, "variedId");

        pc.initFromXml(element);
        assertTrue(pc.isVariant());
    }

    @Test
    public void testIsVariant_noVariant_shouldReturnFalse() {
        pc.initFromXml(getTestDocument().getDocumentElement());
        assertFalse(pc.isVariant());
    }

    @Test
    public void testGetVariedBase_noVariant_shouldReturnNull() {
        pc.initFromXml(getTestDocument().getDocumentElement());
        assertNull(pc.getVariedBase());
    }

    @Test
    public void testGetVariedBase_variant_shouldReturnBaseComponent() {

        String id = "variedId";
        TestProductComponent baseComponent = new TestProductComponent(repository, id, "kind", "1.0");
        when(repository.getProductComponent(id)).thenReturn(baseComponent);

        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, id);

        pc.initFromXml(element);
        IProductComponent baseFromProduct = pc.getVariedBase();

        verify(repository).getProductComponent(id);
        assertSame(baseComponent, baseFromProduct);
    }

    @Test
    public void testGetVariedBase_variantIdNotFound_shouldReturnNull() {

        String id = "missingComponentId";
        when(repository.getProductComponent(anyString())).thenReturn(null);

        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, id);

        pc.initFromXml(element);

        assertNull(pc.getVariedBase());
        verify(repository).getProductComponent(id);
    }

    @Test
    public void testSetVariedBase() {
        repository = new InMemoryRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        String oldId = "oldId";
        TestProductComponent oldBaseComponent = new TestProductComponent(repository, oldId, "old", "1.0");
        ((InMemoryRuntimeRepository)repository).putProductComponent(oldBaseComponent);
        String newId = "newId";
        TestProductComponent newBaseComponent = new TestProductComponent(repository, newId, "new", "1.0");
        ((InMemoryRuntimeRepository)repository).putProductComponent(newBaseComponent);
        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, oldId);
        pc.initFromXml(element);

        pc.setVariedBase(newBaseComponent);

        IProductComponent baseFromProduct = pc.getVariedBase();
        assertSame(newBaseComponent, baseFromProduct);
    }

    @Test
    public void testSetVariedBase_Null() {
        repository = new InMemoryRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
        String oldId = "oldId";
        TestProductComponent oldBaseComponent = new TestProductComponent(repository, oldId, "old", "1.0");
        ((InMemoryRuntimeRepository)repository).putProductComponent(oldBaseComponent);
        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, oldId);
        pc.initFromXml(element);

        pc.setVariedBase(null);

        IProductComponent baseFromProduct = pc.getVariedBase();
        assertNull(baseFromProduct);
    }

    @Test(expected = IllegalRepositoryModificationException.class)
    public void testSetVariedBase_NotModifiable() {
        String oldId = "oldId";
        TestProductComponent oldBaseComponent = new TestProductComponent(repository, oldId, "old", "1.0");
        when(repository.getProductComponent(oldId)).thenReturn(oldBaseComponent);
        String newId = "newId";
        TestProductComponent newBaseComponent = new TestProductComponent(repository, newId, "new", "1.0");
        when(repository.getProductComponent(newId)).thenReturn(newBaseComponent);
        Element element = getTestDocument().getDocumentElement();
        element.setAttribute(ProductComponent.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT, oldId);
        pc.initFromXml(element);

        pc.setVariedBase(newBaseComponent);
    }

    @Test
    public void testInitVRuleConfigs() {
        pc.initFromXml(getTestDocument().getDocumentElement());

        assertEquals(true, pc.isValidationRuleActivated("activeRule"));
        assertEquals(false, pc.isValidationRuleActivated("inactiveRule"));
        assertEquals(false, pc.isValidationRuleActivated("invalidActivationRule"));

        assertEquals(false, pc.isValidationRuleActivated("nonExistentRule"));
    }

    /**
     * Test class for testing the {@link ProductComponent#toXml(Document) toXml} method. This class
     * is used instead of {@link TestProductComponent} because the method
     * {@link #writePropertiesToXml(Element)} has to be overridden for some tests here.
     */
    public static class ProductComponentTestClass extends ProductComponent implements IToXmlSupport {

        public ProductComponentTestClass(IRuntimeRepository repository) {
            super(repository, "id", "productKindId", "versionId");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public void writePropertiesToXml(Element element) {
            /*
             * Nothing to be done. This method is overridden to avoid throwing
             * UnsupportedOperationException in super class implementation.
             */
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

    }
}
