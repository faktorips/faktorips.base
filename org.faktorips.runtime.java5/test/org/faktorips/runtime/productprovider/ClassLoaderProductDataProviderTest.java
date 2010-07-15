/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.productprovider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.faktorips.runtime.internal.toc.IReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.productprovider.ClassLoaderPdpFactory;
import org.faktorips.runtime.productprovider.DataModifiedException;
import org.faktorips.runtime.productprovider.IProductDataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ClassLoaderProductDataProviderTest extends TestCase {

    private IProductDataProvider pdp;

    private DocumentBuilder docBuilder;

    private final String TOC_FIlE_NAME = "org/faktorips/sample/model/internal/faktorips-repository-toc.xml";
    private final String TOC_FIlE_NAME_1 = "org/faktorips/sample/model/internal/faktorips-repository-toc.1.xml";
    private final String TOC_FIlE_NAME_2 = "org/faktorips/sample/model/internal/faktorips-repository-toc.2.xml";

    private ClassLoaderPdpFactory pdpFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pdpFactory = new ClassLoaderPdpFactory(TOC_FIlE_NAME);
        pdpFactory.setCheckForModifications(true);
        docBuilder = createDocumentnewInstanceer();
        pdp = pdpFactory.newInstance();
        copy(TOC_FIlE_NAME_1, TOC_FIlE_NAME);
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public void testgetVersion() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());
        tocFile.setLastModified(321321000);
        pdp = pdpFactory.newInstance();
        String stamp = pdp.getVersion();
        assertEquals("321321000", stamp);

        tocFile.setLastModified(123456000);
        pdp = pdpFactory.newInstance();
        stamp = pdp.getVersion();
        assertEquals("123456000", stamp);
    }

    public void testLoadTocData() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());
        tocFile.setLastModified(321321000);
        pdp = pdpFactory.newInstance();

        assertEquals("321321000", pdp.getVersion());

        ReadonlyTableOfContents expectedToc = new ReadonlyTableOfContents();
        expectedToc.initFromXml(getElement(TOC_FIlE_NAME_1));
        assertEquals(expectedToc.toString(), pdp.getToc().toString());

        copy(TOC_FIlE_NAME_2, TOC_FIlE_NAME);

        tocFile.setLastModified(999999000);

        pdp = pdpFactory.newInstance();

        expectedToc = new ReadonlyTableOfContents();
        expectedToc.initFromXml(getElement(TOC_FIlE_NAME_2));
        assertEquals(expectedToc.toString(), pdp.getToc().toString());
        assertEquals("999999000", pdp.getVersion());
    }

    public void testGetProductCmptData() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());

        Element expectedElement = getElement("org/faktorips/sample/model/internal/TestProduct 2006-01.xml");

        tocFile.setLastModified(321321000);

        pdp = pdpFactory.newInstance();

        IReadonlyTableOfContents toc = pdp.getToc();
        Element actualElement = pdp.getProductCmptData(toc.getProductCmptTocEntry("sample.TestProduct 2006-01"));

        assertEquals("321321000", pdp.getVersion());
        assertTrue(expectedElement.isEqualNode(actualElement));

        tocFile.setLastModified(987987000);
        try {
            pdp.getProductCmptData(toc.getProductCmptTocEntry("sample.TestProduct 2006-01"));
            fail();
        } catch (DataModifiedException e) {
            pdp = pdpFactory.newInstance();
            toc = pdp.getToc();
        }
        actualElement = pdp.getProductCmptData(toc.getProductCmptTocEntry("sample.TestProduct 2006-01"));

        assertEquals("987987000", pdp.getVersion());
        assertTrue(expectedElement.isEqualNode(actualElement));
    }

    public void testGetProductCmptGenerationData() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());

        NodeList generations = getElement("org/faktorips/sample/model/internal/TestProduct 2006-01.xml")
                .getElementsByTagName("Generation");

        tocFile.setLastModified(321321000);

        pdp = pdpFactory.newInstance();
        IReadonlyTableOfContents toc = pdp.getToc();
        assertEquals("321321000", pdp.getVersion());

        ProductCmptTocEntry pcmptEntry = toc.getProductCmptTocEntry("sample.TestProduct 2006-01");
        Element actualElement = pdp.getProductCmptGenerationData(pcmptEntry.getLatestGenerationEntry());
        assertTrue(generations.item(1).isEqualNode(actualElement));

        tocFile.setLastModified(987987000);
        try {
            pdp.getProductCmptGenerationData(toc.getProductCmptTocEntry("sample.TestProduct 2006-01")
                    .getLatestGenerationEntry());
            fail();
        } catch (DataModifiedException e) {
            pdp = pdpFactory.newInstance();
            toc = pdp.getToc();
        }

        assertEquals("987987000", pdp.getVersion());
        pcmptEntry = toc.getProductCmptTocEntry("sample.TestProduct 2006-01");
        actualElement = pdp.getProductCmptGenerationData(pcmptEntry.getLatestGenerationEntry());
        assertTrue(generations.item(1).isEqualNode(actualElement));
    }

    public void testGetTestCaseData() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());

        Element expectedElement = getElement("org/faktorips/sample/model/internal/Test.xml");

        tocFile.setLastModified(321321000);

        pdp = pdpFactory.newInstance();
        IReadonlyTableOfContents toc = pdp.getToc();
        Element actualElement = pdp.getTestcaseElement(toc.getTestCaseTocEntryByQName("testpack.Test"));

        assertEquals("321321000", pdp.getVersion());
        assertTrue(expectedElement.isEqualNode(actualElement));

        tocFile.setLastModified(987987000);
        try {
            pdp.getTestcaseElement(toc.getTestCaseTocEntryByQName("testpack.Test"));
            fail();
        } catch (DataModifiedException e) {
            pdp = pdpFactory.newInstance();
            toc = pdp.getToc();
        }
        actualElement = pdp.getTestcaseElement(toc.getTestCaseTocEntryByQName("testpack.Test"));

        assertEquals("987987000", pdp.getVersion());
        assertTrue(expectedElement.isEqualNode(actualElement));
    }

    public void testGetTableContentAsStream() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());

        String expectedContent = readStreamContent(getClassLoader().getResourceAsStream(
                "org/faktorips/sample/model/internal/A1Content.xml"));

        tocFile.setLastModified(321321000);

        pdp = pdpFactory.newInstance();
        IReadonlyTableOfContents toc = pdp.getToc();
        InputStream is = pdp.getTableContentAsStream(toc.getTableTocEntryByQualifiedTableName("testpack.A1Content"));
        String actualContent = readStreamContent(is);
        is.close();

        assertEquals("321321000", pdp.getVersion());
        assertEquals(expectedContent, actualContent);

        tocFile.setLastModified(987987000);
        try {
            pdp.getTableContentAsStream(toc.getTableTocEntryByQualifiedTableName("testpack.A1Content"));
            fail();
        } catch (DataModifiedException e) {
            pdp = pdpFactory.newInstance();
            toc = pdp.getToc();
        }
        is = pdp.getTableContentAsStream(toc.getTableTocEntryByQualifiedTableName("testpack.A1Content"));

        actualContent = readStreamContent(is);
        is.close();
        assertEquals("987987000", pdp.getVersion());
        assertEquals(expectedContent, actualContent);
    }

    public void testGetEnumContentAsStream() throws Exception {
        File tocFile = new File(getClassLoader().getResource(TOC_FIlE_NAME).toURI());

        String expectedContent = readStreamContent(getClassLoader().getResourceAsStream(
                "org/faktorips/sample/model/internal/TestEnum.xml"));

        tocFile.setLastModified(321321000);

        pdp = pdpFactory.newInstance();
        IReadonlyTableOfContents toc = pdp.getToc();
        InputStream is = pdp.getEnumContentAsStream(toc
                .getEnumContentTocEntry("org.faktorips.sample.model.gaa.TestEnum"));
        String actualContent = readStreamContent(is);
        is.close();

        assertEquals("321321000", pdp.getVersion());
        assertEquals(expectedContent, actualContent);

        tocFile.setLastModified(987987000);
        try {
            pdp.getEnumContentAsStream(toc.getEnumContentTocEntry("org.faktorips.sample.model.gaa.TestEnum"));
            fail();
        } catch (DataModifiedException e) {
            pdp = pdpFactory.newInstance();
            toc = pdp.getToc();
        }
        is = pdp.getEnumContentAsStream(toc.getEnumContentTocEntry("org.faktorips.sample.model.gaa.TestEnum"));

        actualContent = readStreamContent(is);
        is.close();
        assertEquals("987987000", pdp.getVersion());
        assertEquals(expectedContent, actualContent);
    }

    void copy(String srcName, String dstName) throws Exception {
        File src = new File(getClassLoader().getResource(srcName).toURI());
        File dst = new File(getClassLoader().getResource(dstName).toURI());
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    Element getElement(String resource) throws SAXException, IOException {
        InputStream is = getClassLoader().getResourceAsStream(resource);
        Document doc = docBuilder.parse(is);
        is.close();
        return doc.getDocumentElement();
    }

    public static String readStreamContent(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream);
        return readContent(reader);
    }

    public static String readContent(Reader reader) throws IOException {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(reader);
            StringBuilder tocnewInstanceer = new StringBuilder();
            while (bufferedReader.ready()) {
                tocnewInstanceer.append(bufferedReader.readLine()).append('\n');
            }
            return tocnewInstanceer.toString();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private static final DocumentBuilder createDocumentnewInstanceer() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new RuntimeException("Error creating document pdpFactory.", e1);
        }
        builder.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }
}
