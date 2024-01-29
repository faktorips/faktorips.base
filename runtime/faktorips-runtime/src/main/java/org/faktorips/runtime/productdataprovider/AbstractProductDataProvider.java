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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.IVersionChecker;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is the abstract implementation for product data providers. For a description of product data
 * providers @see {@link IProductDataProvider}
 * 
 * @author dirmeier
 */
public abstract class AbstractProductDataProvider implements IProductDataProvider {

    protected static final String MODIFIED_EXCEPTION_MESSAGE = "Data has changed: ";

    /**
     * This is a thread local variable because the document builder is not thread safe. For every
     * thread the method {@link #createDocumentBuilder()} is called automatically
     */
    private static ThreadLocal<DocumentBuilder> docBuilderHolder = new ThreadLocal<>() {
        @Override
        protected DocumentBuilder initialValue() {
            return createDocumentBuilder();
        }
    };

    private final IVersionChecker versionChecker;

    /**
     * This constructor needs a {@link IVersionChecker} that is used to verify the compatibility of
     * the product data versions
     * 
     * @param versionChecker the verison checker to check the product data version
     */
    public AbstractProductDataProvider(IVersionChecker versionChecker) {
        this.versionChecker = versionChecker;
    }

    /**
     * Creating a document builder. If you want to inject your own implementation of document
     * builder use the property specified in {@link DocumentBuilderFactory}.
     * 
     * @return a new {@link DocumentBuilder}
     */
    private static final DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new RuntimeException("Error creating document builder.", e1);
        }
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        return builder;
    }

    /**
     * Getting the thread local instance of {@link DocumentBuilder}
     * 
     * @return a thread local instance of {@link DocumentBuilder}
     */
    protected DocumentBuilder getDocumentBuilder() {
        return docBuilderHolder.get();
    }

    @Override
    public boolean isCompatibleToBaseVersion() {
        return getVersionChecker().isCompatibleVersion(getVersion(), getBaseVersion());
    }

    /**
     * Getting the really actual version of the product data. That means to look in the file, asking
     * your service or your database or what ever is your product data base.
     * 
     * @return the actual version of the product data
     */
    public abstract String getBaseVersion();

    /**
     * @return Returns the versionChecker.
     */
    public IVersionChecker getVersionChecker() {
        return versionChecker;
    }

}
