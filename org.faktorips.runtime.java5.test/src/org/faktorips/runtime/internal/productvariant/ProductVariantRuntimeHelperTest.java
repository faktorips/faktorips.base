/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal.productvariant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductVariantRuntimeHelperTest {

    private static final String ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT = "variedProductCmpt";
    private static final String XML_TAG_PRODUCT_VARIANT_CMPT = "ProductVariantCmpt";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void returnTrueForProductVariantXML() {
        Element element = createMockElementWithNodeName("ProductVariant");
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();

        assertTrue(helper.isProductVariantXML(element));
    }

    @Test
    public void returnFalseForAllOtherXMLs() {
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();
        Element element = createMockElementWithNodeName("ProductComponent");
        assertFalse(helper.isProductVariantXML(element));
        element = createMockElementWithNodeName("TestCase");
        assertFalse(helper.isProductVariantXML(element));
        element = createMockElementWithNodeName("XXX1234");
        assertFalse(helper.isProductVariantXML(element));
    }

    private Element createMockElementWithNodeName(String nodeName) {
        Element element = mock(Element.class);
        when(element.getNodeName()).thenReturn(nodeName);
        return element;
    }

    @Test
    public void findProductComponentElement() throws ParserConfigurationException {
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();
        Element variantElement = createVariantTree();
        Element productComponentElement = helper.findVariationElement(variantElement, "searchedRuntimeID");
        assertNotNull(productComponentElement);
        assertEquals("searchedRuntimeID",
                productComponentElement.getAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_RUNTIME_ID));
    }

    @Test(expected = RuntimeException.class)
    public void throwExceptionWhenNotFound() throws ParserConfigurationException {
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();
        Element variantElement = createVariantTree();
        helper.findVariationElement(variantElement, "inexistentRuntimeID");
    }

    private Element createVariantTree() throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element variant = document.createElement("ProductVariant");
        Element rootCmpt = createCmptWithID(document, "rootID");
        document.appendChild(variant);
        variant.appendChild(rootCmpt);

        Element firstChild = createCmptWithID(document, "firstChildID");
        rootCmpt.appendChild(firstChild);
        rootCmpt.appendChild(createCmptWithID(document, "secondChildID"));
        firstChild.appendChild(createCmptWithID(document, "searchedRuntimeID"));
        firstChild.appendChild(createCmptWithID(document, "otherGrandChild"));

        return document.getDocumentElement();
    }

    protected Element createCmptWithID(Document document, String id) {
        Element rootCmpt = document.createElement(XML_TAG_PRODUCT_VARIANT_CMPT);
        rootCmpt.setAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_RUNTIME_ID, id);
        return rootCmpt;
    }
}
