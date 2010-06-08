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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link ClassLoaderProductDataProvider} is a local implementation of
 * {@link IProductDataProvider} normally for testing purpose. It getting the data similar as the
 * {@link ClassloaderRuntimeRepository} and do not check for any real modifications. For testing you
 * could simulate a modification by calling {@link #modify()}.
 * 
 * @author dirmeier
 */
public class ClassLoaderProductDataProvider implements IProductDataProvider {

    private final ClassLoader cl;
    private final DocumentBuilder docBuilder;
    private String tocResourcePath;
    private long myTimestamp = 0;
    private long pdsTimestamp = 0;

    public ClassLoaderProductDataProvider(ClassLoader cl, String tocResourcePath, DocumentBuilder docBuilder) {
        this.cl = cl;
        this.tocResourcePath = tocResourcePath;
        this.docBuilder = docBuilder;
    }

    public Element getProductCmptData(IProductCmptTocEntry tocEntry) throws DataModifiedException {
        String resourcePath = tocEntry.getXmlResourceName();
        checkForModifications(tocEntry.getIpsObjectId(), pdsTimestamp);
        return getDocumentElement(resourcePath);
    }

    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getParent().getIpsObjectId(), pdsTimestamp);
        Element docElement = getDocumentElement(tocEntry.getParent().getXmlResourceName());
        NodeList nl = docElement.getChildNodes();
        DateTime validFrom = tocEntry.getValidFrom();
        for (int i = 0; i < nl.getLength(); i++) {
            if ("Generation".equals(nl.item(i).getNodeName())) {
                Element genElement = (Element)nl.item(i);
                DateTime generationValidFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
                if (validFrom.equals(generationValidFrom)) {
                    return genElement;
                }
            }
        }
        throw new RuntimeException("Can't find the generation for the toc entry " + tocEntry);
    }

    public Element getTestcaseElement(ITestCaseTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getIpsObjectId(), pdsTimestamp);
        String resourcePath = tocEntry.getXmlResourceName();
        return getDocumentElement(resourcePath);
    }

    public InputStream getTableContentAsStream(ITableContentTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getIpsObjectId(), pdsTimestamp);
        return cl.getResourceAsStream(tocEntry.getXmlResourceName());
    }

    public InputStream getEnumContentAsStream(IEnumContentTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getIpsObjectId(), pdsTimestamp);
        return cl.getResourceAsStream(tocEntry.getXmlResourceName());
    }

    public ReadonlyTableOfContents loadToc() {
        InputStream is = null;
        Document doc;
        try {
            is = cl.getResourceAsStream(tocResourcePath);
            if (is == null) {
                throw new IllegalArgumentException("Can' find table of contents file " + tocResourcePath);
            }
            doc = docBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Error loading table of contents from " + tocResourcePath, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            Element tocElement = doc.getDocumentElement();
            ReadonlyTableOfContents toc = new ReadonlyTableOfContents();
            toc.initFromXml(tocElement);
            myTimestamp = pdsTimestamp;
            return toc;
        } catch (Exception e) {
            throw new RuntimeException("Error creating toc from xml.", e);
        }
    }

    private Element getDocumentElement(String resourcePath) {
        InputStream is = cl.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resourcePath);
        }
        Document doc;
        try {
            doc = docBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse xml resource " + resourcePath, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close the input stream of the resource: " + resourcePath, e);
            }
        }
        Element element = doc.getDocumentElement();
        if (element == null) {
            throw new RuntimeException("Xml resource " + resourcePath + " hasn't got a document element.");
        }
        return element;
    }

    public boolean isExpired(long timestamp) {
        return getModificationStamp() != timestamp;
    }

    public long getModificationStamp() {
        return pdsTimestamp;
    }

    public void modify() {
        pdsTimestamp++;
    }

    private void checkForModifications(String name, long timestamp) throws DataModifiedException {
        if (myTimestamp != timestamp) {
            throw new DataModifiedException(name + " is expired.", this.myTimestamp, timestamp);
        }
    }
}
