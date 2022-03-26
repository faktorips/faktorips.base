/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.runtime.internal.AbstractClassLoadingRuntimeRepository;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A runtime repository that loads the resources by calling
 * {@link ClassLoader#getResourceAsStream(String)}
 * 
 * @author Jan Ortmann
 */
public class ClassloaderRuntimeRepository extends AbstractClassLoadingRuntimeRepository {

    /**
     * Name of the xml element representing the product component registry.
     */
    public static final String REPOSITORY_XML_ELEMENT = "ProductComponentRegistry";

    /**
     * The default name of the file describing the registry's contents.
     */
    public static final String TABLE_OF_CONTENTS_FILE = "faktorips-repository-toc.xml";

    /**
     * This is a thread local variable because the document builder is not thread safe. For every
     * thread the method {@link #createDocumentBuilder()} is called automatically
     */
    private static ThreadLocal<DocumentBuilder> docBuilderHolder = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            return createDocumentBuilder();
        }
    };

    private static final InputStream EMPTY_INPUT_STREAM = new EmptyInputStream();

    /** Path to the resource containing the toc. **/
    private final String tocResourcePath;

    /**
     * Creates a new repository that loads its contents from the given classloader and the given
     * package. Uses the default toc resource name. Uses the default document builder available via
     * <code>DocumentBuilderFactory.newInstance()</code> to parse the xml files.
     * 
     * @throws NullPointerException if cl or basePackage are <code>null</code>.
     * @throws IllegalArgumentException if the basePackage does not contain the table of contents
     *             file.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     * 
     * @see #TABLE_OF_CONTENTS_FILE
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage) {
        this(cl, basePackage, TABLE_OF_CONTENTS_FILE, new DefaultCacheFactory(cl));
    }

    /**
     * Creates a new repository that loads its contents from the given classloader and the given
     * package and toc resource using the default document builder.
     * 
     * @param cl The classloader to use.
     * @param basePackage The name of the base package that contains the data.
     * @param pathToToc Path from the base package to the resource containing the toc, e.g.
     *            "faktorips-repository-toc.xml" or "motor/motor-repository-toc.xml"
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the basePackage does not contain the table of contents
     *             file.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage, String pathToToc) {
        this(cl, basePackage, pathToToc, new DefaultCacheFactory(cl));
    }

    /**
     * Creates a new repository that loads its contents from the given classloader and the given
     * package using the given xml document builder.
     * 
     * @param cl The classloader to use.
     * @param basePackage The name of the base package that contains the data.
     * @param pathToToc Path from the base package to the resource containing the toc, e.g.
     *            "faktorips-repository-toc.xml" or "motor/motor-repository-toc.xml"
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the basePackage does not contain the table of contents
     *             file.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage, String pathToToc,
            ICacheFactory cacheFactory) {
        super(basePackage, cacheFactory, cl);
        if (cl == null) {
            throw new NullPointerException();
        }
        if (basePackage == null) {
            throw new NullPointerException();
        }
        if (pathToToc == null) {
            throw new NullPointerException();
        }
        if ("".equals(basePackage)) {
            tocResourcePath = pathToToc;
        } else {
            tocResourcePath = basePackage.replace('.', '/') + '/' + pathToToc;
        }
        initialize();
    }

    /**
     * Creates a new repository that loads it's contents from the given classloader and and toc
     * resource using the given docuemnt builder to parse the xml data.
     * 
     * @param cl The classloader to use.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the tocResource does not contain a valid table of
     *             contents.
     * @throws RuntimeException if the table of contents can't be read.
     */
    private ClassloaderRuntimeRepository(String tocResource, ClassLoader cl, ICacheFactory cacheFactory) {
        super(tocResource, cacheFactory, cl);
        if (tocResource == null) {
            throw new NullPointerException();
        }
        if (cl == null) {
            throw new NullPointerException();
        }
        tocResourcePath = tocResource;
        initialize();
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource using this
     * class' classloader and the default xml document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public static final ClassloaderRuntimeRepository create(String tocResource) {
        return create(tocResource, ClassloaderRuntimeRepository.class.getClassLoader(),
                new DefaultCacheFactory(ClassloaderRuntimeRepository.class.getClassLoader()));
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource using this
     * class' classloader and the default xml document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @param cacheFactory The CacheFactory used to create the cache objects in the repository
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public static final ClassloaderRuntimeRepository create(String tocResource, ICacheFactory cacheFactory) {
        return create(tocResource, ClassloaderRuntimeRepository.class.getClassLoader(), cacheFactory);
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource and classloader
     * using the default document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @param cl The classloader to use.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public static final ClassloaderRuntimeRepository create(String tocResource, ClassLoader cl) {
        return create(tocResource, cl, new DefaultCacheFactory(cl));
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource and classloader
     * using the default document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @param cl The classloader to use.
     * @param cacheFactory The CacheFactory used to create the cache objects in the repository
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public static final ClassloaderRuntimeRepository create(String tocResource,
            ClassLoader cl,
            ICacheFactory cacheFactory) {
        return new ClassloaderRuntimeRepository(tocResource, cl, cacheFactory);
    }

    protected DocumentBuilder getDocumentBuilder() {
        return docBuilderHolder.get();
    }

    @Override
    protected ReadonlyTableOfContents loadTableOfContents() {
        InputStream is = null;
        Document doc;
        try {
            is = getClassLoader().getResourceAsStream(tocResourcePath);
            if (is == null) {
                throw new IllegalArgumentException("Can't find table of contents file " + tocResourcePath);
            }
            doc = getDocumentBuilder().parse(is);
            // CSOFF: IllegalCatch
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
            ReadonlyTableOfContents toc = new ReadonlyTableOfContents(getClassLoader());
            toc.initFromXml(tocElement);
            return toc;
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException("Error creating toc from xml.", e);
        }
    }

    @Override
    protected String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry) {
        return tocEntry.getImplementationClassName();
    }

    @Override
    protected Element getDocumentElement(ProductCmptTocEntry tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    @Override
    protected Element getDocumentElement(GenerationTocEntry tocEntry) {
        Element docElement = getDocumentElement(tocEntry.getParent());
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

    @Override
    protected Element getDocumentElement(TestCaseTocEntry tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    @Override
    protected InputStream getXmlAsStream(EnumContentTocEntry tocEntry) {
        String xmlResourceName = tocEntry.getXmlResourceName();
        if (IpsStringUtils.isBlank(xmlResourceName)) {
            return EMPTY_INPUT_STREAM;
        }
        InputStream is = getClassLoader().getResourceAsStream(xmlResourceName);
        if (is == null) {
            throw new RuntimeException("Cant't load the input stream for the enumeration content resource "
                    + xmlResourceName);
        }
        return is;
    }

    private Element getDocumentElementInternal(TocEntry tocEntry) {
        String resource = tocEntry.getXmlResourceName();
        InputStream is = getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resource + " for toc entry " + tocEntry);
        }
        Document doc;
        try {
            doc = getDocumentBuilder().parse(is);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException("Can't parse xml resource " + resource, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close the input stream of the resource: " + resource, e);
            }
        }
        Element element = doc.getDocumentElement();
        if (element == null) {
            throw new RuntimeException("Xml resource " + resource + " hasn't got a document element.");
        }
        return element;
    }

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

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    protected InputStream getXmlAsStream(TableContentTocEntry tocEntry) {
        InputStream is = getClassLoader().getResourceAsStream(tocEntry.getXmlResourceName());
        if (is == null) {
            throw new RuntimeException(
                    "Can't find resource " + tocEntry.getXmlResourceName() + " for toc entry " + tocEntry);
        }
        return is;
    }

    @Override
    protected <T> Element getDocumentElement(CustomTocEntryObject<T> tocEntry) {
        return getDocumentElementInternal(tocEntry);
    }

    private static final class EmptyInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }
    }
}
