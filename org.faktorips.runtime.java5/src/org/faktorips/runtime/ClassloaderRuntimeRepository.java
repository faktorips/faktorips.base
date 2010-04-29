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

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.internal.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.AbstractTocBasedRuntimeRepository;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.EnumSaxHandler;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.internal.TocEntry;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassloaderRuntimeRepository extends AbstractTocBasedRuntimeRepository {

    /**
     * Name of the xml element representing the product component registry.
     */
    public final static String REPOSITORY_XML_ELEMENT = "ProductComponentRegistry";

    /**
     * The default name of the file describing the registry's contents.
     */
    public final static String TABLE_OF_CONTENTS_FILE = "faktorips-repository-toc.xml";

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
    public final static ClassloaderRuntimeRepository create(String tocResource) {
        return create(tocResource, ClassloaderRuntimeRepository.class.getClassLoader(), createDocumentBuilder(),
                new DefaultCacheFactory());
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
    public final static ClassloaderRuntimeRepository create(String tocResource, ICacheFactory cacheFactory) {
        return create(tocResource, ClassloaderRuntimeRepository.class.getClassLoader(), createDocumentBuilder(),
                cacheFactory);
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
    public final static ClassloaderRuntimeRepository create(String tocResource, ClassLoader cl) {
        return create(tocResource, cl, createDocumentBuilder(), new DefaultCacheFactory());
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
    public final static ClassloaderRuntimeRepository create(String tocResource,
            ClassLoader cl,
            ICacheFactory cacheFactory) {
        return create(tocResource, cl, createDocumentBuilder(), cacheFactory);
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource and classloader
     * using the given xml document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     * @param cl The classloader to use.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @param docBuilder The document builder used to parse the xml data.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public final static ClassloaderRuntimeRepository create(String tocResource,
            ClassLoader cl,
            DocumentBuilder docBuilder) {
        return new ClassloaderRuntimeRepository(tocResource, cl, docBuilder, new DefaultCacheFactory());
    }

    /**
     * Creates a new repository that loads it's contents from the given toc resource and classloader
     * using the given xml document builder to parse the xml data.
     * 
     * @param tocResource Path to the resource containing the toc file. E.g.
     * @param cl The classloader to use.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @param docBuilder The document builder used to parse the xml data.
     * @param cacheFactory The CacheFactory used to create the cache objects in the repository
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public final static ClassloaderRuntimeRepository create(String tocResource,
            ClassLoader cl,
            DocumentBuilder docBuilder,
            ICacheFactory cacheFactory) {
        return new ClassloaderRuntimeRepository(tocResource, cl, docBuilder, cacheFactory);
    }

    // the classloader the data is loaded from
    private ClassLoader cl;

    // path to the resource containing the toc.
    private String tocResourcePath;

    // xml document builder
    private DocumentBuilder docBuilder = null;

    /**
     * Creates a new repository that loads it's contents from the given classloader and the given
     * package. Uses the default toc resource name. Uses the default document builder available via
     * <code>DocumentBuilderFactory.newInstance()</code> to parse the xml files.
     * 
     * @throws NullPointerException if cl or basePackage are <code>null</code>.
     * @throws IllegalArgumentException if the basePackage does not contain the table of contents
     *             file.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     * @throws ParserConfigurationException if the DocumentBuilderFactory.newInstance() method
     *             throws this exception.
     * 
     * @see #TABLE_OF_CONTENTS_FILE
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage) throws ParserConfigurationException {
        this(cl, basePackage, TABLE_OF_CONTENTS_FILE, createDocumentBuilder(), new DefaultCacheFactory());
    }

    /**
     * Creates a new repository that loads it's contents from the given classloader and the given
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
     * @throws ParserConfigurationException if the DocumentBuilderFactory.newInstance() method
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage, String pathToToc)
            throws ParserConfigurationException {
        this(cl, basePackage, pathToToc, createDocumentBuilder(), new DefaultCacheFactory());
    }

    /**
     * Creates a new repository that loads it's contents from the given classloader and the given
     * package using the given xml document builder.
     * 
     * @param cl The classloader to use.
     * @param basePackage The name of the base package that contains the data.
     * @param pathToToc Path from the base package to the resource containing the toc, e.g.
     *            "faktorips-repository-toc.xml" or "motor/motor-repository-toc.xml"
     * @param docBuilder The document builder used to parse the xml data.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the basePackage does not contain the table of contents
     *             file.
     * @throws RuntimeException if the registry's table of contents file can't be read.
     */
    public ClassloaderRuntimeRepository(ClassLoader cl, String basePackage, String pathToToc,
            DocumentBuilder docBuilder, ICacheFactory cacheFactory) {
        super(basePackage, cacheFactory);
        if (cl == null) {
            throw new NullPointerException();
        }
        if (basePackage == null) {
            throw new NullPointerException();
        }
        if (pathToToc == null) {
            throw new NullPointerException();
        }
        if (docBuilder == null) {
            throw new NullPointerException();
        }
        this.cl = cl;
        if (basePackage.equals("")) {
            tocResourcePath = pathToToc;
        } else {
            tocResourcePath = basePackage.replace('.', '/') + '/' + pathToToc;
        }
        this.docBuilder = docBuilder;
        reload();
    }

    /**
     * Creates a new repository that loads it's contents from the given classloader and and toc
     * resource using the given docuemnt builder to parse the xml data.
     * 
     * @param cl The classloader to use.
     * @param docBuilder The document builder used to parse the xml data.
     * 
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the tocResource does not contain a valid table of
     *             contents.
     * @throws RuntimeException if the table of contents can't be read.
     */
    private ClassloaderRuntimeRepository(String tocResource, ClassLoader cl, DocumentBuilder docBuilder,
            ICacheFactory cacheFactory) {
        super(tocResource, cacheFactory);
        if (tocResource == null) {
            throw new NullPointerException();
        }
        if (cl == null) {
            throw new NullPointerException();
        }
        if (docBuilder == null) {
            throw new NullPointerException();
        }
        this.cl = cl;
        tocResourcePath = tocResource;
        this.docBuilder = docBuilder;
        reload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
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
            AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();
            toc.initFromXml(tocElement);
            return toc;
        } catch (Exception e) {
            throw new RuntimeException("Error creating toc from xml.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IProductComponent> getAllProductComponents(Class<?> productCmptClass) {
        List<IProductComponent> result = new ArrayList<IProductComponent>();
        List<TocEntryObject> entries = toc.getProductCmptTocEntries();
        for (TocEntryObject entry : entries) {
            Class<?> clazz = getClass(entry.getImplementationClassName(), cl);
            if (productCmptClass.isAssignableFrom(clazz)) {
                result.add(getProductComponentInternal(entry));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponent createProductCmpt(TocEntryObject tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), cl);
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { IRuntimeRepository.class, String.class,
                    String.class, String.class });
            productCmpt = (ProductComponent)constructor.newInstance(new Object[] { this, tocEntry.getIpsObjectId(),
                    tocEntry.getKindId(), tocEntry.getVersionId() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element docElement = getDocumentElement(tocEntry);
        productCmpt.initFromXml(docElement);
        return productCmpt;
    }

    @Override
    protected <T> List<T> createEnumValues(TocEntryObject tocEntry, Class<T> clazz) {

        InputStream is = getClassLoader().getResourceAsStream(tocEntry.getXmlResourceName());
        if (is == null) {
            throw new RuntimeException("Cant't load the input stream for the enumeration content resource "
                    + tocEntry.getXmlResourceName());
        }
        EnumSaxHandler saxhandler = new EnumSaxHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxhandler);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName());
        }
        T enumValue = null;
        ArrayList<T> enumValues = new ArrayList<T>();
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<T> constructor = null;
            for (Constructor<?> currentConstructor : constructors) {
                if ((currentConstructor.getModifiers() & Modifier.PROTECTED) > 0) {
                    Class<?>[] parameterTypes = currentConstructor.getParameterTypes();
                    if (parameterTypes.length == 2 && parameterTypes[0] == List.class
                            && parameterTypes[1] == IRuntimeRepository.class) {
                        @SuppressWarnings("unchecked")
                        // neccessary as Class.getDeclaredConstructors() is of type Constructor<?>[]
                        // while returning Contructor<T>[]
                        // The Javaoc Class.getDeclaredConstructors() for more information
                        Constructor<T> castedConstructor = (Constructor<T>)currentConstructor;
                        constructor = castedConstructor;
                    }
                }
            }
            if (constructor == null) {
                throw new RuntimeException(
                        "No valid constructor found to create enumerations instances for the toc entry " + tocEntry);
            }
            for (List<String> enumValueAsStrings : saxhandler.getEnumValueList()) {
                constructor.setAccessible(true);
                enumValue = constructor.newInstance(new Object[] { enumValueAsStrings, this });
                enumValues.add(enumValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't create enumeration instance for toc entry " + tocEntry, e);
        }
        return enumValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProductComponentGeneration createProductCmptGeneration(TocEntryGeneration tocEntry) {
        ProductComponent productCmpt = (ProductComponent)getProductComponent(tocEntry.getParent().getIpsObjectId());
        if (productCmpt == null) {
            throw new RuntimeException("Can't get product component for toc entry " + tocEntry);
        }
        ProductComponentGeneration productCmptGen;
        try {
            Constructor<?> constructor = getConstructor(tocEntry);
            productCmptGen = (ProductComponentGeneration)constructor.newInstance(new Object[] { productCmpt });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element docElement = getDocumentElement(tocEntry);
        NodeList nl = docElement.getChildNodes();
        DateTime validFrom = tocEntry.getValidFrom();
        for (int i = 0; i < nl.getLength(); i++) {
            if ("Generation".equals(nl.item(i).getNodeName())) {
                Element genElement = (Element)nl.item(i);
                DateTime generationValidFrom = DateTime.parseIso(genElement.getAttribute("validFrom"));
                if (validFrom.equals(generationValidFrom)) {
                    productCmptGen.initFromXml(genElement);
                    return productCmptGen;
                }
            }
        }
        throw new RuntimeException("Can't find the generation for the toc entry " + tocEntry);
    }

    private Constructor<?> getConstructor(TocEntryGeneration tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), cl);
        try {
            String productCmptClassName = tocEntry.getParent().getImplementationClassName();
            Class<?> productCmptClass = getClass(productCmptClassName, cl);
            return implClass.getConstructor(new Class[] { productCmptClass });
        } catch (Exception e) {
            throw new RuntimeException("Can't get constructor for class " + implClass.getName() + " , toc entry "
                    + tocEntry, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITable createTable(TocEntryObject tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), cl);
        Table table;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[0]);
            table = (Table)constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table instance for toc entry " + tocEntry, e);
        }

        String resource = tocEntry.getXmlResourceName();
        InputStream is = cl.getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Can' find resource " + resource + " for toc entry " + tocEntry);
        }

        try {
            table.initFromXml(is, this);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse xml resource " + resource, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close the input stream of the resource: " + resource, e);
            }
        }
        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsTestCaseBase createTestCase(TocEntryObject tocEntry, IRuntimeRepository runtimeRepository) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), cl);
        IpsTestCaseBase test;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { String.class });
            test = (IpsTestCaseBase)constructor.newInstance(new Object[] { tocEntry.getIpsObjectQualifiedName() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create test case instance for toc entry " + tocEntry, e);
        }
        // sets the runtime repository which will be used to instantiate the test case,
        // this could be a different one (e.g. contains more dependence repositories) as the test
        // case belongs to,
        // because the test case itself could contain objects from different repositories, the
        // runtime repository
        // should contain all needed repositories
        test.setRepository(runtimeRepository);
        if (test instanceof IpsTestCase2) {
            // only classes of type ips test case 2 supports xml input
            Element docElement = getDocumentElement(tocEntry);
            ((IpsTestCase2)test).initFromXml(docElement);
        }
        test.setFullPath(tocEntry.getIpsObjectId());
        return test;
    }

    private Element getDocumentElement(TocEntry tocEntry) {
        String resource = tocEntry.getXmlResourceName();
        InputStream is = cl.getResourceAsStream(resource);
        if (is == null) {
            throw new RuntimeException("Can't find resource " + resource + " for toc entry " + tocEntry);
        }
        Document doc;
        try {
            doc = docBuilder.parse(is);
        } catch (Exception e) {
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

    private final static DocumentBuilder createDocumentBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            throw new RuntimeException("Error creaing document builder.", e1);
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

    /**
     * {@inheritDoc}
     */
    public boolean isModifiable() {
        return false;
    }

}
