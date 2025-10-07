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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.faktorips.runtime.internal.AbstractClassLoadingRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * A runtime repository that loads the resources by calling
 * {@link ClassLoader#getResourceAsStream(String)}
 *
 * @author Jan Ortmann
 */
public class ClassloaderRuntimeRepository extends AbstractClassLoadingRuntimeRepository {

    /**
     * Name of the XML element representing the product component registry.
     */
    public static final String REPOSITORY_XML_ELEMENT = "ProductComponentRegistry";

    /**
     * The default name of the file describing the registry's contents.
     */
    public static final String TABLE_OF_CONTENTS_FILE = "faktorips-repository-toc.xml";

    private static final InputStream EMPTY_INPUT_STREAM = new EmptyInputStream();

    /** Path to the resource containing the toc. **/
    private final String tocResourcePath;

    /**
     * Creates a new repository that loads its contents from the given classloader and the given
     * package. Uses the default ToC resource name. Uses the default document builder available via
     * {@link XmlUtil#getDocumentBuilder()} to parse the XML files.
     *
     * @throws NullPointerException if {@code cl} or {@code basePackage} are {@code null}.
     * @throws IllegalArgumentException if the base package does not contain the table of contents
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
     * package and ToC resource using the default document builder.
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
     * package using the given XML document builder.
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
        super(Objects.requireNonNull(basePackage), cacheFactory, Objects.requireNonNull(cl));
        Objects.requireNonNull(pathToToc);
        if ("".equals(basePackage)) {
            tocResourcePath = pathToToc;
        } else {
            tocResourcePath = basePackage.replace('.', '/') + '/' + pathToToc;
        }
        initialize();
    }

    /**
     * Creates a new repository that loads it's contents from the given classloader and and toc
     * resource using the given document builder to parse the XML data.
     *
     * @param cl The classloader to use.
     *
     * @throws NullPointerException if any argument is <code>null</code>.
     * @throws IllegalArgumentException if the tocResource does not contain a valid table of
     *             contents.
     * @throws RuntimeException if the table of contents can't be read.
     */
    private ClassloaderRuntimeRepository(String tocResource, ClassLoader cl, ICacheFactory cacheFactory) {
        super(Objects.requireNonNull(tocResource), cacheFactory, Objects.requireNonNull(cl));
        tocResourcePath = tocResource;
        initialize();
    }

    /**
     * Creates a new repository that loads it's contents from the given ToC resource using this
     * class' classloader and the default XML document builder to parse the XML data.
     *
     * @param tocResource Path to the resource containing the ToC file. E.g.
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
     * Creates a new repository that loads it's contents from the given ToC resource using this
     * class' classloader and the default XML document builder to parse the XML data.
     *
     * @param tocResource Path to the resource containing the ToC file. E.g.
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
     * Creates a new repository that loads it's contents from the given ToC resource and classloader
     * using the default document builder to parse the XML data.
     *
     * @param tocResource Path to the resource containing the ToC file. E.g.
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
     * Creates a new repository that loads it's contents from the given ToC resource and classloader
     * using the default document builder to parse the XML data.
     *
     * @param tocResource Path to the resource containing the ToC file. E.g.
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

    @Override
    protected ReadonlyTableOfContents loadTableOfContents() {
        Document doc;
        try (InputStream is = getClassLoader().getResourceAsStream(tocResourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Can't find table of contents file " + tocResourcePath);
            }
            doc = XmlUtil.getDocumentBuilder()
                    .parse(new InputSource(new InputStreamReader(is, StandardCharsets.UTF_8)));
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new RuntimeException("Error loading table of contents from " + tocResourcePath, e);
        }
        try {
            Element tocElement = doc.getDocumentElement();
            ReadonlyTableOfContents toc = new ReadonlyTableOfContents(getClassLoader());
            toc.initFromXml(tocElement);
            return toc;
        } catch (Exception e) {
            // CSON: IllegalCatch
            throw new RuntimeException("Error creating ToC from XML.", e);
        }
    }

    @Override
    protected String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry) {
        return tocEntry.getImplementationClassName();
    }

    @Override
    protected InputStream getXmlAsStream(TableContentTocEntry tocEntry) {
        InputStream is = getClassLoader().getResourceAsStream(tocEntry.getXmlResourceName());
        if (is == null) {
            throw new RuntimeException(
                    "Can't find resource " + tocEntry.getXmlResourceName() + " for ToC entry " + tocEntry);
        }
        return is;
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

    @Override
    public boolean isModifiable() {
        return false;
    }

    /**
     * Registers this {@link ClassloaderRuntimeRepository} with an {@link IRuntimeRepositoryLookup}
     * that will always return this repository on this machine and recreate it if necessary when
     * deserialized on other machines.
     *
     * @since 25.7
     */
    public void withLookup() {
        setRuntimeRepositoryLookup(new IRuntimeRepositoryLookup.RuntimeRepositoryLookupByToC(tocResourcePath, this));
    }

    private static final class EmptyInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }
    }
}
