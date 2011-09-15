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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductVariantRuntimeHelperTest {

    private static final String XML_TAG_PRODUCT_VARIANT_CMPT = "ProductVariantCmpt";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testIsProductVariantXML() {
        Element element = mock(Element.class);
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();

        when(element.hasAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT)).thenReturn(true);
        assertTrue(helper.isProductVariantXML(element));

        when(element.hasAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT)).thenReturn(false);
        assertFalse(helper.isProductVariantXML(element));
    }

    @Test
    public void returnFalseForProductVariantXML() {
        Element element = mock(Element.class);
        when(element.hasAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT)).thenReturn(false);
        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();

        assertFalse(helper.isProductVariantXML(element));
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

    @Test
    public void initProdCmpt() {
        IRuntimeRepository runtimeRepository = mock(IRuntimeRepository.class);
        ProductComponent originalProductCmpt = mock(ProductComponent.class);
        when(runtimeRepository.getProductComponent(anyString())).thenReturn(originalProductCmpt);

        ProductComponent productCmptToInitialize = mock(ProductComponent.class);
        Element variationElement = mock(Element.class);

        ProductVariantRuntimeHelper helper = spy(new ProductVariantRuntimeHelper());
        /*
         * Do nothing as invoking initWithVariation() normally throws exceptions.
         * initWithVariation() calls two methods that are final (initFromXML(), toXml()) and thus
         * cannot be mocked.
         */
        doNothing().when(helper).loadAndVary(originalProductCmpt, variationElement, productCmptToInitialize);
        helper.loadAndVaryProductComponent(runtimeRepository, variationElement, productCmptToInitialize);

        verify(helper).loadAndVary(originalProductCmpt, variationElement, productCmptToInitialize);
    }

    /*
     * Cant mock generation acquiring methods as of yet, do this is ignored. Integration test is
     * working.
     */
    @Test
    @Ignore
    public void initGeneration() {
        IRuntimeRepository runtimeRepository = mock(IRuntimeRepository.class);
        ProductComponent originalProductCmpt = mock(ProductComponent.class);
        ProductComponentGeneration originalProductCmptGen = mock(ProductComponentGeneration.class);

        GregorianCalendar gregCal = new GregorianCalendar();

        ProductComponentGeneration productCmptGenToInitialize = mock(ProductComponentGeneration.class);
        Element variationElement = mock(Element.class);
        // when(variationElement.getAttribute(ProductVariantRuntimeHelper.ATTRIBUTE_NAME_VARIED_PRODUCT_CMPT)).thenReturn(
        // "VariedProdCmptName");

        ProductVariantRuntimeHelper helper = spy(new ProductVariantRuntimeHelper());
        /*
         * Do nothing as invoking initWithVariation() normally throws exceptions.
         * initWithVariation() calls two methods that are final (initFromXML(), toXml()) and thus
         * cannot be mocked.
         */
        doNothing().when(helper).loadAndVary(originalProductCmptGen, variationElement, productCmptGenToInitialize);
        when(helper.getOriginalProdCmpt(runtimeRepository, variationElement)).thenReturn(originalProductCmpt);
        // when(helper.getGenerationForValidFrom(originalProductCmpt,
        // gregCal)).thenReturn(originalProductCmptGen);
        helper.loadAndVaryProductComponentGeneration(runtimeRepository, gregCal, variationElement,
                productCmptGenToInitialize);

        verify(helper).loadAndVary(originalProductCmptGen, variationElement, productCmptGenToInitialize);
    }

    @Test
    public void initWithVariation() {
        Element variationElement = mock(Element.class);
        Document docMock = mock(Document.class);
        when(variationElement.getOwnerDocument()).thenReturn(docMock);
        when(docMock.cloneNode(false)).thenReturn(docMock);

        Element originalElement = mock(Element.class);
        IXmlPersistenceSupport originalObject = mock(IXmlPersistenceSupport.class);
        when(originalObject.toXml(docMock)).thenReturn(originalElement);

        IClRepositoryObject objectToInitialize = mock(IClRepositoryObject.class);

        ProductVariantRuntimeHelper helper = new ProductVariantRuntimeHelper();
        helper.loadAndVary(originalObject, variationElement, objectToInitialize);

        InOrder order = inOrder(objectToInitialize);
        order.verify(objectToInitialize).initFromXml(originalElement);
        order.verify(objectToInitialize).initFromXml(variationElement);
        verifyNoMoreInteractions(objectToInitialize);
    }

    @Test
    public void initProdCmptGeneration() {

    }

}
