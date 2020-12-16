/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.faktorips.runtime.IRuntimeObject;
import org.faktorips.runtime.util.StringBuilderJoiner;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A table of contents for the runtime repository.
 * <p>
 * The table of contents contains a list of toc entries that contain the information needed to
 * identify and load the objects stored in the repository.
 * </p>
 * <p>
 * <em>The table of contents can be extended to read toc entries for new object types by
 * implementing and registering a {@link ITocEntryFactory}.</em>
 * </p>
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractReadonlyTableOfContents implements IReadonlyTableOfContents {

    public static final String TOC_XML_ELEMENT = "FaktorIps-TableOfContents";
    public static final String PRODUCT_DATA_VERSION_XML_ELEMENT = "productDataVersion";

    private String productDataVersion;

    private Map<String, ITocEntryFactory<?>> tocEntryFactoriesByXmlTag;
    private final ClassLoader classLoader;

    /**
     * Creates a new TOC that uses the given {@link ClassLoader} to find {@link ITocEntryFactory}
     * implementations via {@link ServiceLoader}.
     * 
     * @param classLoader the {@link ClassLoader} used to find {@link ITocEntryFactory}
     *            implementations
     */
    public AbstractReadonlyTableOfContents(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private Map<String, ITocEntryFactory<?>> getTocEntryFactoriesByXmlTag() {
        if (tocEntryFactoriesByXmlTag == null) {
            synchronized (TocEntryObject.class) {
                if (tocEntryFactoriesByXmlTag == null) {
                    tocEntryFactoriesByXmlTag = new HashMap<String, ITocEntryFactory<?>>();
                    for (ITocEntryFactory<?> tocEntryFactory : AbstractTocEntryFactory.getBaseTocEntryFactories()) {
                        tocEntryFactoriesByXmlTag.put(tocEntryFactory.getXmlTag(), tocEntryFactory);
                    }
                    loadExtendedTocEntryFactories();
                }

            }
        }
        return tocEntryFactoriesByXmlTag;
    }

    /**
     * Loads additional TOC entry factories registered via META-INF/services. To register a
     * {@link ITocEntryFactory}, a file called
     * META-INF/services/org.faktorips.runtime.internal.toc.ITocEntryFactory must be created, with
     * the qualified class names of the respective implementations as a content (one implementation
     * per line).
     * <p>
     * The method is marked with <code>@SuppressWarnings("rawtypes")</code> because
     * {@link ITocEntryFactory} has a generic type which cannot not be inferred when using the
     * service loader.
     * 
     * @see ServiceLoader
     */
    @SuppressWarnings("rawtypes")
    private void loadExtendedTocEntryFactories() {
        ServiceLoader<ITocEntryFactory> serviceLoader = ServiceLoader.load(ITocEntryFactory.class, classLoader);
        for (ITocEntryFactory<?> tocEntryFactory : serviceLoader) {
            tocEntryFactoriesByXmlTag.put(tocEntryFactory.getXmlTag(), tocEntryFactory);
        }
    }

    /**
     * Initializes the table of contents with data stored in the xml element.
     */
    public void initFromXml(Element tocElement) {
        productDataVersion = tocElement.getAttribute(PRODUCT_DATA_VERSION_XML_ELEMENT);
        if (productDataVersion == null) {
            productDataVersion = "0";
        }
        NodeList nl = tocElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element entryElement = (Element)nl.item(i);
                internalAddEntry(
                        getTocEntryFactoriesByXmlTag().get(entryElement.getNodeName()).createFromXml(entryElement));
            }
        }
    }

    /**
     * Adds the entry to the table of contents.
     */
    protected abstract void internalAddEntry(TocEntryObject entry);

    /**
     * Returns the toc entry representing a product component for the given id or null if no entry
     * exists for the given id.
     */
    @Override
    public abstract ProductCmptTocEntry getProductCmptTocEntry(String id);

    /**
     * Returns the toc entry representing a product component for the given product component kind
     * id and versionId or null if no such entry exists.
     */
    @Override
    public abstract ProductCmptTocEntry getProductCmptTocEntry(String kindId, String versionId);

    /**
     * Returns all toc's entries representing product components.
     */
    @Override
    public abstract List<ProductCmptTocEntry> getProductCmptTocEntries();

    /**
     * Returns all toc's entries representing product components that belong to the indicated
     * product component kind.
     */
    @Override
    public abstract List<ProductCmptTocEntry> getProductCmptTocEntries(String kindId);

    /**
     * Returns all toc's entries representing tables.
     */
    @Override
    public abstract List<TableContentTocEntry> getTableTocEntries();

    /**
     * Returns all toc's entries representing test cases.
     */
    @Override
    public abstract List<TestCaseTocEntry> getTestCaseTocEntries();

    /**
     * Returns a toc entry representing a test case for the given qualified name.
     */
    @Override
    public abstract TestCaseTocEntry getTestCaseTocEntryByQName(String qName);

    /**
     * Returns a toc entry representing a table for the table's class object.
     */
    @Override
    public abstract TableContentTocEntry getTableTocEntryByClassname(String implementationClass);

    /**
     * Returns a toc entry representing a table for this table's qualified table name.
     */
    @Override
    public abstract TableContentTocEntry getTableTocEntryByQualifiedTableName(String qualifiedTableName);

    /**
     * Returns all toc's entries representing model types.
     */
    @Override
    public abstract Set<ModelTypeTocEntry> getModelTypeTocEntries();

    /**
     * Returns the toc entry representing enum contents for the specified implementation class.
     */
    @Override
    public abstract EnumContentTocEntry getEnumContentTocEntry(String className);

    /**
     * Returns all toc entries that link to an enumeration xml adapter.
     */
    @Override
    public abstract Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries();

    @Override
    public String getProductDataVersion() {
        return productDataVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TOC");
        sb.append(System.lineSeparator());
        StringBuilderJoiner.join(sb, getProductCmptTocEntries(), System.lineSeparator());
        sb.append(System.lineSeparator());
        StringBuilderJoiner.join(sb, getTableTocEntries(), System.lineSeparator());
        sb.append(System.lineSeparator());
        StringBuilderJoiner.join(sb, getTestCaseTocEntries(), System.lineSeparator());
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public abstract <T extends IRuntimeObject> List<CustomTocEntryObject<T>> getTypedTocEntries(Class<T> type);

}
