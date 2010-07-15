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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link ClassLoaderProductDataProvider} is an implementation of {@link IProductDataProvider}
 * normally for testing purpose. It loads data similar as the
 * {@link org.faktorips.runtime.ClassloaderRuntimeRepository}. Modification is checked by checking
 * the modification date of the toc resource.
 * 
 * @author dirmeier
 */
public class ClassLoaderProductDataProvider extends AbstractProductDataProvider {

    private final ClassLoader cl;
    private final boolean checkTocModifications;
    private final String tocFileLastModified;
    private final ReadonlyTableOfContents toc;
    private final URL tocUrl;

    public ClassLoaderProductDataProvider(ClassLoader classLoader, String tocResourcePath, boolean checkTocModifications) {
        this.cl = classLoader;
        tocUrl = cl.getResource(tocResourcePath);
        if (tocUrl == null) {
            throw new IllegalArgumentException("Can' find table of contents file " + tocResourcePath);
        }
        this.checkTocModifications = checkTocModifications;
        toc = loadToc();
        tocFileLastModified = getBaseVersion();
    }

    private ReadonlyTableOfContents loadToc() {
        InputStream is = null;
        Document doc;
        try {
            is = tocUrl.openStream();
            doc = getDocumentBuilder().parse(is);
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
            ReadonlyTableOfContents toc = new ReadonlyTableOfContents();
            toc.initFromXml(tocElement);
            return toc;
        } catch (Exception e) {
            throw new RuntimeException("Error creating toc from xml.", e);
        }
    }

    public Element getProductCmptData(ProductCmptTocEntry tocEntry) throws DataModifiedException {
        String resourcePath = tocEntry.getXmlResourceName();
        Element documentElement = getDocumentElement(resourcePath);
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return documentElement;
    }

    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException {
        Element docElement = getDocumentElement(tocEntry.getParent().getXmlResourceName());
        NodeList nl = docElement.getChildNodes();
        DateTime validFrom = tocEntry.getValidFrom();
        for (int i = 0; i < nl.getLength(); i++) {
            if ("Generation".equals(nl.item(i).getNodeName())) {
                Element genElement = (Element)nl.item(i);
                DateTime generationValidFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
                if (validFrom.equals(generationValidFrom)) {
                    throwExceptionIfModified(tocEntry.getParent().getIpsObjectId(), getBaseVersion());
                    return genElement;
                }
            }
        }
        throw new RuntimeException("Can't find the generation for the toc entry " + tocEntry);
    }

    public Element getTestcaseElement(TestCaseTocEntry tocEntry) throws DataModifiedException {
        String resourcePath = tocEntry.getXmlResourceName();
        Element documentElement = getDocumentElement(resourcePath);
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return documentElement;
    }

    public InputStream getTableContentAsStream(TableContentTocEntry tocEntry) throws DataModifiedException {
        InputStream resourceAsStream = cl.getResourceAsStream(tocEntry.getXmlResourceName());
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return resourceAsStream;
    }

    public InputStream getEnumContentAsStream(EnumContentTocEntry tocEntry) throws DataModifiedException {
        InputStream resourceAsStream = cl.getResourceAsStream(tocEntry.getXmlResourceName());
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return resourceAsStream;
    }

    public synchronized ReadonlyTableOfContents getToc() {
        return toc;
    }

    private Element getDocumentElement(String resourcePath) {
        InputStream is = cl.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resourcePath);
        }
        Document doc;
        try {
            doc = getDocumentBuilder().parse(is);
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

    public String getVersion() {
        return tocFileLastModified;
    }

    @Override
    public String getBaseVersion() {
        if (checkTocModifications) {
            String lastMod = "";
            try {
                URLConnection connection = tocUrl.openConnection();
                if (connection instanceof JarURLConnection) {
                    JarURLConnection jarUrlConnection = (JarURLConnection)connection;
                    URL jarUrl = jarUrlConnection.getJarFileURL();
                    File jarFile = new File(jarUrl.toURI());
                    lastMod = "" + jarFile.lastModified();
                } else {
                    File tocFile = new File(tocUrl.getFile());
                    lastMod = "" + tocFile.lastModified();
                }
            } catch (Exception e) {
                throw new RuntimeException("Cannot get last modification stamp of toc url", e);
            }
            return lastMod;
        }
        if (toc != null) {
            return toc.getProductDataVersion();
        } else {
            return "";
        }
    }

    private void throwExceptionIfModified(String name, String timestamp) throws DataModifiedException {
        if (checkTocModifications && !isCompatibleVersion(getVersion(), timestamp)) {
            throw new DataModifiedException(MODIFIED_EXCEPTION_MESSAGE + name, getVersion(), timestamp);
        }
    }

}
