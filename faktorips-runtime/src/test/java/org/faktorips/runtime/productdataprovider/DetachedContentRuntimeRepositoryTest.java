/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

public class DetachedContentRuntimeRepositoryTest {

    private DetachedContentRuntimeRepository repository;

    @Mock
    private IProductDataProvider productDataProvider;

    @Mock
    private IFormulaEvaluatorFactory formulaEvaluatorFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        repository = new DetachedContentRuntimeRepository("", mock(ICacheFactory.class), mock(ClassLoader.class),
                productDataProvider, formulaEvaluatorFactory);
    }

    @Test
    public void testLoadTableOfContents() {
        IReadonlyTableOfContents mockToc = mock(IReadonlyTableOfContents.class);
        when(productDataProvider.getToc()).thenReturn(mockToc);

        assertEquals(mockToc, repository.loadTableOfContents());
    }

    @Test
    public void testGetDocumentElementProductCmptTocEntry() throws DataModifiedException {
        Element mockElement = mock(Element.class);
        ProductCmptTocEntry mockTocEntry = mock(ProductCmptTocEntry.class);
        when(productDataProvider.getProductCmptData(mockTocEntry)).thenReturn(mockElement);

        assertEquals(mockElement, repository.getDocumentElement(mockTocEntry));
    }

    @Test(expected = DataModifiedRuntimeException.class)
    public void testGetDocumentElementProductCmptTocEntryDataModifiedExceptionThrown() throws DataModifiedException {
        ProductCmptTocEntry mockTocEntry = mock(ProductCmptTocEntry.class);
        when(productDataProvider.getProductCmptData(mockTocEntry)).thenThrow(new DataModifiedException("", "", ""));

        repository.getDocumentElement(mockTocEntry);
    }

    @Test
    public void testGetDocumentElementGenerationTocEntry() throws DataModifiedException {
        Element mockElement = mock(Element.class);
        GenerationTocEntry mockTocEntry = mock(GenerationTocEntry.class);
        when(productDataProvider.getProductCmptGenerationData(mockTocEntry)).thenReturn(mockElement);

        assertEquals(mockElement, repository.getDocumentElement(mockTocEntry));
    }

    @Test(expected = DataModifiedRuntimeException.class)
    public void testGetDocumentElementGenerationTocEntryDataModifiedExceptionThrown() throws DataModifiedException {
        GenerationTocEntry mockTocEntry = mock(GenerationTocEntry.class);
        when(productDataProvider.getProductCmptGenerationData(mockTocEntry)).thenThrow(
                new DataModifiedException("", "", ""));

        repository.getDocumentElement(mockTocEntry);
    }

    @Test
    public void testGetDocumentElementTestCaseTocEntry() throws DataModifiedException {
        Element mockElement = mock(Element.class);
        TestCaseTocEntry mockTocEntry = mock(TestCaseTocEntry.class);
        when(productDataProvider.getTestcaseElement(mockTocEntry)).thenReturn(mockElement);

        assertEquals(mockElement, repository.getDocumentElement(mockTocEntry));
    }

    @Test(expected = DataModifiedRuntimeException.class)
    public void testGetDocumentElementTestCaseTocEntryDataModifiedExceptionThrown() throws DataModifiedException {
        TestCaseTocEntry mockTocEntry = mock(TestCaseTocEntry.class);
        when(productDataProvider.getTestcaseElement(mockTocEntry)).thenThrow(new DataModifiedException("", "", ""));

        repository.getDocumentElement(mockTocEntry);
    }

    @Test
    public void testGetProductComponentGenerationImplClass() {
        String generationImplClassName = "generationImplClassName";
        ProductCmptTocEntry prodctCmptTocEntry = new ProductCmptTocEntry("", "", "", "", "", "",
                generationImplClassName, mock(DateTime.class));
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(prodctCmptTocEntry, mock(DateTime.class,
                Answers.RETURNS_DEEP_STUBS.get()), "", "");

        assertEquals(generationImplClassName, repository.getProductComponentGenerationImplClass(generationTocEntry));
    }

    @Test
    public void testGetProductComponentGenerationImplClassFormulaEvaluatorFactoryIsNull() {
        repository = new DetachedContentRuntimeRepository("", mock(ICacheFactory.class), mock(ClassLoader.class),
                productDataProvider, null);

        String generationImplClassName = "generationImplClassName";
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(mock(ProductCmptTocEntry.class), mock(
                DateTime.class, Answers.RETURNS_DEEP_STUBS.get()), generationImplClassName, "");

        assertEquals(generationImplClassName, repository.getProductComponentGenerationImplClass(generationTocEntry));
    }

    @Test
    public void testGetXmlAsStreamEnumContent() throws DataModifiedException {
        InputStream mockInputStream = mock(InputStream.class);
        EnumContentTocEntry mockTocEntry = mock(EnumContentTocEntry.class);
        when(productDataProvider.getEnumContentAsStream(mockTocEntry)).thenReturn(mockInputStream);

        assertEquals(mockInputStream, repository.getXmlAsStream(mockTocEntry));
    }

    @Test(expected = DataModifiedRuntimeException.class)
    public void testGetXmlAsStreamEnumContentDataModifiedExceptionThrown() throws DataModifiedException {
        EnumContentTocEntry mockTocEntry = mock(EnumContentTocEntry.class);
        when(productDataProvider.getEnumContentAsStream(mockTocEntry)).thenThrow(new DataModifiedException("", "", ""));

        repository.getXmlAsStream(mockTocEntry);
    }

    @Test
    public void testGetXmlAsStreamTableContent() throws DataModifiedException {
        InputStream mockInputStream = mock(InputStream.class);
        TableContentTocEntry mockTocEntry = mock(TableContentTocEntry.class);
        when(productDataProvider.getTableContentAsStream(mockTocEntry)).thenReturn(mockInputStream);

        assertEquals(mockInputStream, repository.getXmlAsStream(mockTocEntry));
    }

    @Test(expected = DataModifiedRuntimeException.class)
    public void testGetXmlAsStreamTableContentDataModifiedExceptionThrown() throws DataModifiedException {
        TableContentTocEntry mockTocEntry = mock(TableContentTocEntry.class);
        when(productDataProvider.getTableContentAsStream(mockTocEntry))
                .thenThrow(new DataModifiedException("", "", ""));

        repository.getXmlAsStream(mockTocEntry);
    }

    @Test
    public void testGetProductDataVersion() {
        String productDataVersion = "testVersion";
        when(productDataProvider.getVersion()).thenReturn(productDataVersion);
        assertEquals(productDataVersion, repository.getProductDataVersion());
    }

    @Test
    public void testIsUpToDate() {
        when(productDataProvider.isCompatibleToBaseVersion()).thenReturn(true);
        assertTrue(repository.isUpToDate());
        when(productDataProvider.isCompatibleToBaseVersion()).thenReturn(false);
        assertFalse(repository.isUpToDate());
    }

    @Test
    public void testIsModifiable() {
        assertFalse(repository.isModifiable());
    }

}
