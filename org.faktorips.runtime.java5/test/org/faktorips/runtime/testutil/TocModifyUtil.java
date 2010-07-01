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

package org.faktorips.runtime.testutil;

import java.io.FileWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.runtime.productprovider.ClassLoaderProductDataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TocModifyUtil {

    private ClassLoaderProductDataProvider productDataProvider;
    private DocumentBuilder docBuilder;
    private URL tocResource;

    public TocModifyUtil(String tocResourcePath) throws ParserConfigurationException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        tocResource = cl.getResource(tocResourcePath);
        productDataProvider = new ClassLoaderProductDataProvider(cl, tocResourcePath);
        docBuilder = createDocumentBuilder();
    }

    public void setLastModified(long time) throws Exception {
        ReadonlyTableOfContents toc = productDataProvider.loadToc();
        saveXml(toc, time);
    }

    private void saveXml(ReadonlyTableOfContents toc, long lastModified) throws Exception {
        Document doc = docBuilder.newDocument();
        Element element = doc.createElement(AbstractReadonlyTableOfContents.TOC_XML_ELEMENT);
        element.setAttribute(AbstractReadonlyTableOfContents.PRODUCT_DATA_VERSION_XML_ELEMENT, "" + lastModified);
        for (TocEntryObject entry : toc.getEntries()) {
            element.appendChild(entry.toXml(doc));
        }

        FileWriter writer = new FileWriter(tocResource.getFile());

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute("indent-number", new Integer(4)); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // no problem, we're using a older version
        }
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        // both settings are necessary, to accommodate versions in Java 1.4 and 1.5
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        writer.close();
    }

    protected final static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return builder;
    }
}
