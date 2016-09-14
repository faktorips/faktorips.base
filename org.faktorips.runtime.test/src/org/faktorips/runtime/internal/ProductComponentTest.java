/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.XmlAbstractTestCase;
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
    }

    @SuppressWarnings("unchecked")
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
        when(document.createElement("ProductComponent")).thenReturn(prodCmptElement);
        when(prodCmptElement.getOwnerDocument()).thenReturn(ownerDocument);
        when(ownerDocument.createElement("validTo")).thenReturn(validToElement);

        cmpt.toXml(document, false);

        verify(cmpt).writePropertiesToXml(prodCmptElement);
        verify(cmpt).writeTableUsagesToXml(prodCmptElement);
        verify(cmpt).writeReferencesToXml(prodCmptElement);
        verify(cmpt).writeExtensionPropertiesToXml(prodCmptElement);
    }

    @Test
    public void testWriteTableUsageToXml() {
        Element prodCmptElement = getTestDocument().getDocumentElement();
        NodeList childNodes = prodCmptElement.getChildNodes();
        assertEquals(15, childNodes.getLength());

        pc.writeTableUsageToXml(prodCmptElement, "structureUsageValue", "tableContentNameValue");

        Node node = childNodes.item(15);
        Node namedItem = node.getAttributes().getNamedItem("structureUsage");
        String nodeValue = node.getFirstChild().getTextContent();
        assertEquals(16, childNodes.getLength());
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        assertEquals("tableContentNameValue", nodeValue);
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

        when(repository.getProductComponentGenerations(cmpt)).thenReturn(Arrays.asList(productComponentGeneration));
        when(repository.getProductComponentGeneration("id", new GregorianCalendar(1, 1, 1900))).thenReturn(
                productComponentGeneration);

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

        when(repository.getProductComponentGenerations(cmpt)).thenReturn(Arrays.asList(productComponentGeneration));
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

    /**
     * Test class for testing the {@link ProductComponent#toXml(Document) toXml} method. This class
     * is used instead of {@link TestProductComponent} because the method
     * {@link #writePropertiesToXml(Element)} has to be overridden for some tests here.
     */
    public static class ProductComponentTestClass extends ProductComponent {

        public ProductComponentTestClass(IRuntimeRepository repository) {
            super(repository, "id", "productKindId", "versionId");
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        protected void writePropertiesToXml(Element element) {
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
