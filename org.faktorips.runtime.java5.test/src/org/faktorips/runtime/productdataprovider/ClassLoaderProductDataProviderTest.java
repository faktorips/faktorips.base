/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.productdataprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ClassLoaderProductDataProviderTest {

    private static final String TOC_RESOURCE_PATH = "Toc";

    private static final String INITIAL_TOC_FILE_LAST_MODIFIED = "1000";

    private ClassLoaderProductDataProvider productDataProvider;

    @Mock
    private ClassLoaderDataSource mockDataSource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockDataSource.getLastModificationStamp(TOC_RESOURCE_PATH)).thenReturn(INITIAL_TOC_FILE_LAST_MODIFIED);
        mockRootElementForResourcePath(TOC_RESOURCE_PATH);

        productDataProvider = createProductDataProvider(true);
    }

    @Test
    public void constructor() {
        assertNotNull(productDataProvider.getToc());
        assertEquals(INITIAL_TOC_FILE_LAST_MODIFIED, productDataProvider.getVersion());
    }

    @Test
    public void testGetBaseVersion() {
        when(mockDataSource.getLastModificationStamp(TOC_RESOURCE_PATH)).thenReturn("foo");

        assertEquals("foo", productDataProvider.getBaseVersion());
    }

    @Test
    public void testGetBaseVersionRepositoryDoesNotCheckTocModifications() {
        Element mockTocElement = mockRootElementForResourcePath(TOC_RESOURCE_PATH);
        when(mockTocElement.getAttribute(AbstractReadonlyTableOfContents.PRODUCT_DATA_VERSION_XML_ELEMENT)).thenReturn(
                "foo");
        productDataProvider = createProductDataProvider(false);

        assertEquals("foo", productDataProvider.getBaseVersion());
    }

    @Test
    public void testGetProductCmptData() throws DataModifiedException {
        String xmlResourceName = "XmlResourceName";
        ProductCmptTocEntry tocEntry = createProductCmptTocEntry(xmlResourceName, false);
        Element mockRootElement = mockRootElementForResourcePath(xmlResourceName);

        assertEquals(mockRootElement, productDataProvider.getProductCmptData(tocEntry));
    }

    @Test(expected = DataModifiedException.class)
    public void testGetProductCmptDataRepositoryModified() throws DataModifiedException {
        ProductCmptTocEntry tocEntry = createProductCmptTocEntry("XmlResourceName", true);

        modifyRepository();

        productDataProvider.getProductCmptData(tocEntry);
    }

    @Test
    public void testGetProductCmptDataRepositoryModifiedButRepositoryDoesNotCheckTocModifications()
            throws DataModifiedException {

        productDataProvider = createProductDataProvider(false);

        ProductCmptTocEntry tocEntry = createProductCmptTocEntry("XmlResourceName", true);

        modifyRepository();

        // Test successful if no exception is thrown
        productDataProvider.getProductCmptData(tocEntry);
    }

    @Test
    public void testGetTestcaseElement() throws DataModifiedException {
        String xmlResourceName = "XmlResourceName";
        TestCaseTocEntry tocEntry = createTestCaseTocEntry(xmlResourceName, false);
        Element mockRootElement = mockRootElementForResourcePath(xmlResourceName);

        assertEquals(mockRootElement, productDataProvider.getTestcaseElement(tocEntry));
    }

    @Test(expected = DataModifiedException.class)
    public void testGetTestcaseElementRepositoryModified() throws DataModifiedException {
        TestCaseTocEntry tocEntry = createTestCaseTocEntry("XmlResourceName", true);

        modifyRepository();

        productDataProvider.getTestcaseElement(tocEntry);
    }

    @Test
    public void testGetTestcaseElementRepositoryModifiedButRepositoryDoesNotCheckTocModifications()
            throws DataModifiedException {

        productDataProvider = createProductDataProvider(false);

        TestCaseTocEntry tocEntry = createTestCaseTocEntry("XmlResourceName", true);

        modifyRepository();

        // Test successful if no exception is thrown
        productDataProvider.getTestcaseElement(tocEntry);
    }

    @Test
    public void testGetProductCmptGenerationData() throws DataModifiedException {
        String xmlResourceName = "foo";
        DateTime generationValidFrom = DateTime.createDateOnly(new GregorianCalendar());

        ProductCmptTocEntry productCmptTocEntry = createProductCmptTocEntry(xmlResourceName, false);
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(productCmptTocEntry, generationValidFrom, "", "");

        Element mockProductCmptElement = mockRootElementForResourcePath(xmlResourceName);
        NodeList mockChildrenList = mock(NodeList.class);
        when(mockChildrenList.getLength()).thenReturn(2);
        mockGenerationChildElement(mockChildrenList, 0, DateTime.createDateOnly(new GregorianCalendar(1, 1, 1)));
        Element mockChild2 = mockGenerationChildElement(mockChildrenList, 1, generationValidFrom);
        when(mockProductCmptElement.getChildNodes()).thenReturn(mockChildrenList);

        assertEquals(mockChild2, productDataProvider.getProductCmptGenerationData(generationTocEntry));
    }

    @Test(expected = RuntimeException.class)
    public void testGetProductCmptGenerationDataGenerationDoesNotExist() throws DataModifiedException {
        String xmlResourceName = "foo";
        DateTime generationValidFrom = DateTime.createDateOnly(new GregorianCalendar());

        ProductCmptTocEntry productCmptTocEntry = createProductCmptTocEntry(xmlResourceName, false);
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(productCmptTocEntry, generationValidFrom, "", "");

        Element mockProductCmptElement = mockRootElementForResourcePath(xmlResourceName);
        NodeList mockChildrenList = mock(NodeList.class);
        when(mockChildrenList.getLength()).thenReturn(2);
        mockGenerationChildElement(mockChildrenList, 0, DateTime.createDateOnly(new GregorianCalendar(1, 1, 1)));
        mockGenerationChildElement(mockChildrenList, 1, DateTime.createDateOnly(new GregorianCalendar(2, 2, 2)));
        when(mockProductCmptElement.getChildNodes()).thenReturn(mockChildrenList);

        productDataProvider.getProductCmptGenerationData(generationTocEntry);
    }

    @Test(expected = DataModifiedException.class)
    public void testGetProductCmptGenerationDataRepositoryModified() throws DataModifiedException {
        String xmlResourceName = "foo";
        DateTime generationValidFrom = DateTime.createDateOnly(new GregorianCalendar());

        ProductCmptTocEntry productCmptTocEntry = createProductCmptTocEntry(xmlResourceName, false);
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(productCmptTocEntry, generationValidFrom, "", "");

        Element mockProductCmptElement = mockRootElementForResourcePath(xmlResourceName);
        NodeList mockChildrenList = mock(NodeList.class);
        when(mockChildrenList.getLength()).thenReturn(2);
        mockGenerationChildElement(mockChildrenList, 0, DateTime.createDateOnly(new GregorianCalendar(1, 1, 1)));
        mockGenerationChildElement(mockChildrenList, 1, generationValidFrom);
        when(mockProductCmptElement.getChildNodes()).thenReturn(mockChildrenList);

        modifyRepository();

        productDataProvider.getProductCmptGenerationData(generationTocEntry);
    }

    @Test
    public void testGetProductCmptGenerationDataRepositoryModifiedButRepositoryDoesNotCheckTocModifications()
            throws DataModifiedException {

        productDataProvider = createProductDataProvider(false);

        String xmlResourceName = "foo";
        DateTime generationValidFrom = DateTime.createDateOnly(new GregorianCalendar());

        ProductCmptTocEntry productCmptTocEntry = createProductCmptTocEntry(xmlResourceName, false);
        GenerationTocEntry generationTocEntry = new GenerationTocEntry(productCmptTocEntry, generationValidFrom, "", "");

        Element mockProductCmptElement = mockRootElementForResourcePath(xmlResourceName);
        NodeList mockChildrenList = mock(NodeList.class);
        when(mockChildrenList.getLength()).thenReturn(2);
        mockGenerationChildElement(mockChildrenList, 0, DateTime.createDateOnly(new GregorianCalendar(1, 1, 1)));
        mockGenerationChildElement(mockChildrenList, 1, generationValidFrom);
        when(mockProductCmptElement.getChildNodes()).thenReturn(mockChildrenList);

        modifyRepository();

        // Test successful if no exception is thrown
        productDataProvider.getProductCmptGenerationData(generationTocEntry);
    }

    @Test
    public void testGetTableContentAsStream() throws DataModifiedException {
        String xmlResourceName = "foo";
        TableContentTocEntry tocEntry = new TableContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        assertEquals(mockInputStream, productDataProvider.getTableContentAsStream(tocEntry));
    }

    @Test(expected = DataModifiedException.class)
    public void testGetTableContentAsStreamRepositoryModified() throws DataModifiedException {
        String xmlResourceName = "foo";
        TableContentTocEntry tocEntry = new TableContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        modifyRepository();

        productDataProvider.getTableContentAsStream(tocEntry);
    }

    @Test
    public void testGetTableContentAsStreamRepositoryModifiedButRepositoryDoesNotCheckTocModifications()
            throws DataModifiedException {

        productDataProvider = createProductDataProvider(false);

        String xmlResourceName = "foo";
        TableContentTocEntry tocEntry = new TableContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        modifyRepository();

        // Test successful if no exception is thrown
        productDataProvider.getTableContentAsStream(tocEntry);
    }

    @Test
    public void testGetEnumContentAsStream() throws DataModifiedException {
        String xmlResourceName = "foo";
        EnumContentTocEntry tocEntry = new EnumContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        assertEquals(mockInputStream, productDataProvider.getEnumContentAsStream(tocEntry));
    }

    @Test(expected = DataModifiedException.class)
    public void testGetEnumContentAsStreamRepositoryModified() throws DataModifiedException {
        String xmlResourceName = "foo";
        EnumContentTocEntry tocEntry = new EnumContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        modifyRepository();

        productDataProvider.getEnumContentAsStream(tocEntry);
    }

    @Test
    public void testGetEnumContentAsStreamRepositoryModifiedButRepositoryDoesNotCheckTocModifications()
            throws DataModifiedException {

        productDataProvider = createProductDataProvider(false);

        String xmlResourceName = "foo";
        EnumContentTocEntry tocEntry = new EnumContentTocEntry("", "", xmlResourceName, "");
        InputStream mockInputStream = mock(InputStream.class);
        when(mockDataSource.getResourceAsStream(xmlResourceName)).thenReturn(mockInputStream);

        modifyRepository();

        // Test successful if no exception is thrown
        productDataProvider.getEnumContentAsStream(tocEntry);
    }

    private void modifyRepository() {
        when(mockDataSource.getLastModificationStamp(TOC_RESOURCE_PATH)).thenReturn("1234");
    }

    private Element mockGenerationChildElement(NodeList nodeList, int index, DateTime validFrom) {
        Element mockChild = mock(Element.class);
        when(mockChild.getNodeName()).thenReturn(GenerationTocEntry.XML_TAG);
        when(mockChild.getAttribute(GenerationTocEntry.PROPERTY_VALID_FROM)).thenReturn(validFrom.toIsoFormat());
        when(nodeList.item(index)).thenReturn(mockChild);
        return mockChild;
    }

    private ClassLoaderProductDataProvider createProductDataProvider(boolean checkTocModifications) {
        return new ClassLoaderProductDataProvider(mockDataSource, TOC_RESOURCE_PATH, checkTocModifications);
    }

    /**
     * Creates a {@link ProductCmptTocEntry} with the given XML resource name.
     * 
     * @param xmlResourceName The name of the XML resource the TOC entry shall point to
     * @param mockRootElement Flag indicating whether a document root element mock shall be directly
     *            created for the given XML resource name
     */
    private ProductCmptTocEntry createProductCmptTocEntry(String xmlResourceName, boolean mockRootElement) {
        ProductCmptTocEntry tocEntry = new ProductCmptTocEntry("", "", "", "", xmlResourceName, "", "",
                mock(DateTime.class));
        if (mockRootElement) {
            mockRootElementForResourcePath(xmlResourceName);
        }
        return tocEntry;
    }

    /**
     * @see #createProductCmptTocEntry(String, boolean)
     */
    private TestCaseTocEntry createTestCaseTocEntry(String xmlResourceName, boolean mockRootElement) {
        TestCaseTocEntry tocEntry = new TestCaseTocEntry("", "", xmlResourceName, "");
        if (mockRootElement) {
            mockRootElementForResourcePath(xmlResourceName);
        }
        return tocEntry;
    }

    private Element mockRootElementForResourcePath(String resourcePath) {
        Document mockDocument = mock(Document.class);
        Element mockRootElement = mock(Element.class, Answers.RETURNS_DEEP_STUBS.get());
        when(mockDataSource.loadDocument(eq(resourcePath), any(DocumentBuilder.class))).thenReturn(mockDocument);
        when(mockDocument.getDocumentElement()).thenReturn(mockRootElement);
        return mockRootElement;
    }

}
