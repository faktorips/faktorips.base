/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testutil;

import java.io.FileWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.faktorips.runtime.productdataprovider.AbstractProductDataProvider;
import org.faktorips.runtime.productdataprovider.ClassLoaderProductDataProviderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TocModifyUtil {

    private final AbstractProductDataProvider productDataProvider;
    private final DocumentBuilder docBuilder;
    private final URL tocResource;

    public TocModifyUtil(String tocResourcePath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        tocResource = cl.getResource(tocResourcePath);
        productDataProvider = (AbstractProductDataProvider)new ClassLoaderProductDataProviderFactory(tocResourcePath)
                .newInstance();
        docBuilder = XmlUtil.getDocumentBuilder();
    }

    public void setLastModified(long time) throws Exception {
        ReadonlyTableOfContents toc = (ReadonlyTableOfContents)productDataProvider.getToc();
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
            transformerFactory.setAttribute("indent-number", Integer.valueOf(4));
        } catch (IllegalArgumentException e) {
            // no problem, we're using a older version
        }
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // both settings are necessary, to accommodate versions in Java 1.4 and 1.5
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        writer.close();
    }

}
