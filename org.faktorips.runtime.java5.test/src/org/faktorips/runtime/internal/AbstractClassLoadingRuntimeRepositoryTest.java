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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.internal.productvariant.ProductVariantRuntimeHelper;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AbstractClassLoadingRuntimeRepositoryTest {

    private AbstractClassLoadingRuntimeRepository repo;
    private ProductComponent loadedProdCmpt;

    @Before
    public void setUp() throws Exception {
        loadedProdCmpt = mock(ProductComponent.class);
        repo = new ClassloaderRuntimeRepository(getClass().getClassLoader(), "",
                "org/faktorips/runtime/internal/ClassLoaderRuntimeRepoToc.xml");
        repo = spy(repo);
        doReturn(loadedProdCmpt).when(repo).getProductComponent(anyString());
        doNothing().when(repo).initialize();
    }

    @Test
    public void shouldLoadNormalProductComponentNormally() {
        ProductCmptTocEntry entry = mock(ProductCmptTocEntry.class);
        Element element = mock(Element.class);
        ProductComponent newProdCmptInstance = mock(ProductComponent.class);

        doReturn(element).when(repo).getDocumentElement(entry);
        when(element.hasAttribute(anyString())).thenReturn(false);
        doReturn(newProdCmptInstance).when(repo).createProductComponentInstance(anyString(), anyString(), anyString(),
                anyString());

        repo.createProductCmpt(entry);

        verify(newProdCmptInstance).initFromXml(element);
        verifyNoMoreInteractions(loadedProdCmpt);
    }

    @Test
    public void shouldLoadVariedProductComponent() {
        ProductCmptTocEntry entry = mock(ProductCmptTocEntry.class);
        Element element = mock(Element.class);
        Element copyElement = mock(Element.class);
        Document docMock = mock(Document.class);
        ProductComponent newProdCmptInstance = mock(ProductComponent.class);

        when(element.getOwnerDocument()).thenReturn(docMock);
        when(docMock.cloneNode(anyBoolean())).thenReturn(docMock);
        doReturn(element).when(repo).getDocumentElement(entry);
        when(element.hasAttribute(anyString())).thenReturn(true);
        doReturn(copyElement).when(loadedProdCmpt).toXml(any(Document.class));
        doReturn(newProdCmptInstance).when(repo).createProductComponentInstance(anyString(), anyString(), anyString(),
                anyString());

        repo.createProductCmpt(entry);

        verify(newProdCmptInstance).initFromXml(element);
        verify(newProdCmptInstance).initFromXml(copyElement);
        verifyNoMoreInteractions(newProdCmptInstance);
    }

    @Test
    public void shouldLoadNormalGenerationNormally() {
        GenerationTocEntry genEntry = mock(GenerationTocEntry.class);
        ProductCmptTocEntry parentEntry = mock(ProductCmptTocEntry.class);
        Element element = mock(Element.class);
        Element prodCmptElement = mock(Element.class);
        ProductComponentGeneration gen = mock(ProductComponentGeneration.class);

        when(genEntry.getParent()).thenReturn(parentEntry);
        doReturn(element).when(repo).getDocumentElement(genEntry);
        doReturn(prodCmptElement).when(repo).getDocumentElement(parentEntry);
        when(prodCmptElement.hasAttribute(anyString())).thenReturn(false);
        doReturn(gen).when(repo).createProductComponentGenerationInstance(genEntry, loadedProdCmpt);

        repo.createProductCmptGeneration(genEntry);

        verify(gen).initFromXml(element);
    }

    @Test
    public void shouldLoadVariedGeneration() {
        GenerationTocEntry genEntry = mock(GenerationTocEntry.class);
        Element genElement = mock(Element.class);
        ProductVariantRuntimeHelper helper = mock(ProductVariantRuntimeHelper.class);

        doReturn(genElement).when(repo).getDocumentElement(genEntry);
        when(genElement.hasAttribute(anyString())).thenReturn(true);
        doReturn(helper).when(repo).getProductVariantHelper();

        when(helper.isProductVariantXML(genElement)).thenReturn(true);

        repo.createProductCmptGeneration(genEntry);

        verify(helper).initProductComponentGenerationVariation(repo, genEntry, genElement);
    }

    @Test
    public void testCreateEnumValues() throws Exception {
        EnumContentTocEntry tocEntry = mock(EnumContentTocEntry.class);
        when(tocEntry.getXmlResourceName()).thenReturn("org/faktorips/runtime/internal/EmptyEnum.xml");

        List<Object> enumValues = repo.createEnumValues(tocEntry, Object.class);

        assertTrue(enumValues.isEmpty());
    }
}
