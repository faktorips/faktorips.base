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

package org.faktorips.runtime.pds;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InstantProductDataProvider implements IProductDataProvider {

    private final ClassLoader cl;
    private final DocumentBuilder docBuilder;
    private String tocResourcePath;

    public InstantProductDataProvider(ClassLoader cl, String tocResourcePath, DocumentBuilder docBuilder) {
        this.cl = cl;
        this.tocResourcePath = tocResourcePath;
        this.docBuilder = docBuilder;
    }

    public long getModificationStamp() {
        return 0;
    }

    public Element getProductCmptData(IProductCmptTocEntry tocEntry) {
        String resourcePath = tocEntry.getXmlResourceName();
        return getDocumentElement(resourcePath);
    }

    public Element getTestcaseElement(ITestCaseTocEntry tocEntry) {
        String resourcePath = tocEntry.getXmlResourceName();
        return getDocumentElement(resourcePath);
    }

    public InputStream getXmlAsStream(IEnumContentTocEntry tocEntry) {
        return cl.getResourceAsStream(tocEntry.getXmlResourceName());
    }

    public InputStream getXmlAsStream(ITableContentTocEntry tocEntry) {
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

}
