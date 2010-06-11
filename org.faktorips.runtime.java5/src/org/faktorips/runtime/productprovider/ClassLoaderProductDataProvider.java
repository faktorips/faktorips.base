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
import java.net.URL;
import java.net.URLConnection;

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
 * {@link ClassloaderRuntimeRepository}. Modification is checked by checking the modification date
 * of the toc resource.
 * 
 * @author dirmeier
 */
public class ClassLoaderProductDataProvider implements IProductDataProvider {

    private final ClassLoader cl;
    private final DocumentBuilder docBuilder;
    private long lastModification = -1;
    private URL tocUrl;
    private ReadonlyTableOfContents toc;

    public ClassLoaderProductDataProvider(ClassLoader cl, String tocResourcePath, DocumentBuilder docBuilder) {
        this.cl = cl;
        tocUrl = cl.getResource(tocResourcePath);
        if (tocUrl == null) {
            throw new IllegalArgumentException("Can' find table of contents file " + tocResourcePath);
        }
        this.docBuilder = docBuilder;
    }

    public Element getProductCmptData(IProductCmptTocEntry tocEntry) throws DataModifiedException {
        String resourcePath = tocEntry.getXmlResourceName();
        checkForModifications(tocEntry.getIpsObjectId(), getModificationStamp());
        return getDocumentElement(resourcePath);
    }

    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getParent().getIpsObjectId(), getModificationStamp());
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
        checkForModifications(tocEntry.getIpsObjectId(), getModificationStamp());
        String resourcePath = tocEntry.getXmlResourceName();
        return getDocumentElement(resourcePath);
    }

    public InputStream getTableContentAsStream(ITableContentTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getIpsObjectId(), getModificationStamp());
        return cl.getResourceAsStream(tocEntry.getXmlResourceName());
    }

    public InputStream getEnumContentAsStream(IEnumContentTocEntry tocEntry) throws DataModifiedException {
        checkForModifications(tocEntry.getIpsObjectId(), getModificationStamp());
        return cl.getResourceAsStream(tocEntry.getXmlResourceName());
    }

    public ReadonlyTableOfContents loadToc() {
        if (toc != null && lastModification == getModificationStamp()) {
            return toc;
        }
        InputStream is = null;
        Document doc;
        try {
            lastModification = getModificationStamp();
            is = tocUrl.openStream();
            doc = docBuilder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException("Error loading table of contents from " + tocUrl.getFile(), e);
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
            toc = new ReadonlyTableOfContents();
            toc.initFromXml(tocElement);
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
        URLConnection connection;
        try {
            connection = tocUrl.openConnection();
            return connection.getLastModified();
        } catch (IOException e) {
            return 0;
        }
    }

    private void checkForModifications(String name, long timestamp) throws DataModifiedException {
        if (lastModification != timestamp) {
            throw new DataModifiedException(name + " is expired.", this.lastModification, timestamp);
        }
    }
}
