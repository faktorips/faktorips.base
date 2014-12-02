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

import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProductComponentTest extends XmlAbstractTestCase {

    private IRuntimeRepository repository;
    private ProductComponent pc;

    @Before
    public void setUp() {
        repository = new InMemoryRuntimeRepository();
        pc = new TestProductComponent(repository, "TestProduct", "TestProductKind", "TestProductVersion");
    }

    @SuppressWarnings("unchecked")
    // the verify for the parameterized map cannot be type safe
    @Test
    public void testCallInitMethodsOnInitFromXML() {
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(repository, "id", "productKindId",
                "versionId"));
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
        IRuntimeRepository runtimeRepository = mock(IRuntimeRepository.class);
        ProductComponentTestClass cmpt = spy(new ProductComponentTestClass(runtimeRepository, "id", "productKindId",
                "versionId"));
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
        assertEquals(9, childNodes.getLength());

        pc.writeTableUsageToXml(prodCmptElement, "structureUsageValue", "tableContentNameValue");

        assertEquals(10, childNodes.getLength());
        Node namedItem = childNodes.item(9).getAttributes().getNamedItem("structureUsage");
        assertEquals("structureUsageValue", namedItem.getNodeValue());
        String nodeValue = childNodes.item(9).getFirstChild().getTextContent();
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

    /**
     * Test class for testing the {@link ProductComponent#toXml(Document) toXml} method. This class
     * is used instead of {@link TestProductComponent} because the method
     * {@link #writePropertiesToXml(Element)} has to be overridden for some tests here.
     */
    public static class ProductComponentTestClass extends ProductComponent {

        public ProductComponentTestClass(IRuntimeRepository repository, String id, String productKindId,
                String versionId) {
            super(repository, id, productKindId, versionId);
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
    }
}
