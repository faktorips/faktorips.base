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

import java.io.InputStream;

import org.faktorips.runtime.IVersionChecker;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link ClassLoaderProductDataProvider} is an implementation of {@link IProductDataProvider}
 * normally for testing purpose.
 * <p>
 * It loads data similar as the {@link org.faktorips.runtime.ClassloaderRuntimeRepository}.
 * Modification is checked by checking the modification date of the TOC resource.
 * 
 * @author dirmeier
 */
public class ClassLoaderProductDataProvider extends AbstractProductDataProvider {

    private final ClassLoaderDataSource dataSource;

    private final boolean checkTocModifications;

    private final String tocFileLastModified;

    private final ReadonlyTableOfContents toc;

    private final String tocResourcePath;

    public ClassLoaderProductDataProvider(ClassLoaderDataSource dataSource, String tocResourcePath,
            boolean checkTocModifications) {
        super(IVersionChecker.STRICT);

        this.dataSource = dataSource;
        this.tocResourcePath = tocResourcePath;
        this.checkTocModifications = checkTocModifications;

        toc = loadToc();
        tocFileLastModified = getBaseVersion();
    }

    private ReadonlyTableOfContents loadToc() {
        Element tocElement = getDocumentElement(tocResourcePath);
        ReadonlyTableOfContents rotoc = new ReadonlyTableOfContents(dataSource.getClassLoader());
        rotoc.initFromXml(tocElement);
        return rotoc;
    }

    @Override
    public String getBaseVersion() {
        return checkTocModifications ? dataSource.getLastModificationStamp(tocResourcePath)
                : toc.getProductDataVersion();
    }

    @Override
    public Element getProductCmptData(ProductCmptTocEntry tocEntry) throws DataModifiedException {
        return getDocumentElement(tocEntry);
    }

    @Override
    public Element getTestcaseElement(TestCaseTocEntry tocEntry) throws DataModifiedException {
        return getDocumentElement(tocEntry);
    }

    private Element getDocumentElement(TocEntryObject tocEntry) throws DataModifiedException {
        String resourcePath = tocEntry.getXmlResourceName();
        Element documentElement = getDocumentElement(resourcePath);
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return documentElement;
    }

    @Override
    public Element getProductCmptGenerationData(GenerationTocEntry tocEntry) throws DataModifiedException {
        Element docElement = getDocumentElement(tocEntry.getParent().getXmlResourceName());
        NodeList nl = docElement.getChildNodes();
        DateTime validFrom = tocEntry.getValidFrom();
        for (int i = 0; i < nl.getLength(); i++) {
            if (GenerationTocEntry.XML_TAG.equals(nl.item(i).getNodeName())) {
                Element genElement = (Element)nl.item(i);
                DateTime generationValidFrom = DateTime
                        .parseIso(genElement.getAttribute(GenerationTocEntry.PROPERTY_VALID_FROM));
                if (validFrom.equals(generationValidFrom)) {
                    throwExceptionIfModified(tocEntry.getParent().getIpsObjectId(), getBaseVersion());
                    return genElement;
                }
            }
        }
        throw new RuntimeException("Can't find the generation for the TOC entry '" + tocEntry + "'");
    }

    @Override
    public InputStream getTableContentAsStream(TableContentTocEntry tocEntry) throws DataModifiedException {
        return getResourceAsStream(tocEntry);
    }

    @Override
    public InputStream getEnumContentAsStream(EnumContentTocEntry tocEntry) throws DataModifiedException {
        return getResourceAsStream(tocEntry);
    }

    private InputStream getResourceAsStream(TocEntryObject tocEntry) throws DataModifiedException {
        InputStream resourceAsStream = dataSource.getResourceAsStream(tocEntry.getXmlResourceName());
        throwExceptionIfModified(tocEntry.getIpsObjectId(), getBaseVersion());
        return resourceAsStream;
    }

    private Element getDocumentElement(String resourcePath) {
        Document doc = dataSource.loadDocument(resourcePath, getDocumentBuilder());
        Element element = doc.getDocumentElement();
        if (element == null) {
            throw new RuntimeException("Xml resource '" + resourcePath + "' hasn't got a document element.");
        }
        return element;
    }

    @Override
    public ReadonlyTableOfContents getToc() {
        return toc;
    }

    @Override
    public String getVersion() {
        return tocFileLastModified;
    }

    private void throwExceptionIfModified(String name, String timestamp) throws DataModifiedException {
        if (checkTocModifications && !getVersionChecker().isCompatibleVersion(getVersion(), timestamp)) {
            throw new DataModifiedException(MODIFIED_EXCEPTION_MESSAGE + name, getVersion(), timestamp);
        }
    }

    @Override
    public <T> Element getTocEntryData(CustomTocEntryObject<T> tocEntry) throws DataModifiedException {
        return getDocumentElement(tocEntry);
    }

}
