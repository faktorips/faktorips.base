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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.faktorips.runtime.IClRepositoryObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.IXmlPersistenceSupport;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductVariantRuntimeHelperTest {

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
    public void initGeneration() {
        IRuntimeRepository runtimeRepository = mock(IRuntimeRepository.class);
        GenerationTocEntry genEntry = mock(GenerationTocEntry.class);
        ProductCmptTocEntry parentEntry = mock(ProductCmptTocEntry.class);

        ProductComponent originalProductCmpt = mock(ProductComponent.class);
        ProductComponent variedProdCmpt = mock(ProductComponent.class);
        ProductComponentGeneration originalProductCmptGen = mock(ProductComponentGeneration.class);
        ProductComponentGeneration productCmptGenToInitialize = mock(ProductComponentGeneration.class);
        Element genVariationElement = mock(Element.class);

        ProductVariantRuntimeHelper helper = spy(new ProductVariantRuntimeHelper());
        doNothing().when(helper).loadAndVary(originalProductCmptGen, genVariationElement, productCmptGenToInitialize);
        doReturn(originalProductCmpt).when(helper).getOriginalProdCmpt(runtimeRepository, genVariationElement);
        when(originalProductCmpt.getGenerationBase(any(Calendar.class))).thenReturn(originalProductCmptGen);
        when(runtimeRepository.getProductComponent(anyString())).thenReturn(variedProdCmpt);
        doReturn(productCmptGenToInitialize).when(helper).createNewInstance(originalProductCmptGen, variedProdCmpt);
        when(genEntry.getValidFrom()).thenReturn(new DateTime(2010, 1, 1));
        when(genEntry.getParent()).thenReturn(parentEntry);

        helper.initProductComponentGenerationVariation(runtimeRepository, genEntry, genVariationElement);

        verify(helper).loadAndVary(originalProductCmptGen, genVariationElement, productCmptGenToInitialize);
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
