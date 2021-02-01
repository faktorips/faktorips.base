/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import java.util.List;
import java.util.Set;

/**
 * This is the interface for readonly table of contents used by the runtime repositories. With the
 * methods in this interface you could find a single entry in the table of contents or the list of
 * entries of a special type
 * 
 * @author dirmeier
 */
public interface IReadonlyTableOfContents {

    /**
     * Returns the toc entry representing a product component for the given id or null if no entry
     * exists for the given id.
     */
    public ProductCmptTocEntry getProductCmptTocEntry(String id);

    /**
     * Returns the toc entry representing a product component for the given product component kind
     * id and versionId or null if no such entry exists.
     */
    public ProductCmptTocEntry getProductCmptTocEntry(String kindId, String versionId);

    /**
     * Returns all toc's entries representing product components.
     */
    public List<ProductCmptTocEntry> getProductCmptTocEntries();

    /**
     * Returns all toc's entries representing product components that belong to the indicated
     * product component kind.
     */
    public List<ProductCmptTocEntry> getProductCmptTocEntries(String kindId);

    /**
     * Returns all toc's entries representing tables.
     */
    public List<TableContentTocEntry> getTableTocEntries();

    /**
     * Returns all toc's entries representing test cases.
     */
    public List<TestCaseTocEntry> getTestCaseTocEntries();

    /**
     * Returns a toc entry representing a test case for the given qualified name.
     */
    public TestCaseTocEntry getTestCaseTocEntryByQName(String qName);

    /**
     * Returns a toc entry representing a table for the table's class object.
     */
    public TableContentTocEntry getTableTocEntryByClassname(String implementationClass);

    /**
     * Returns a toc entry representing a table for this table's qualified table name.
     */
    public TableContentTocEntry getTableTocEntryByQualifiedTableName(String qualifiedTableName);

    /**
     * Returns all toc's entries representing model types.
     */
    public Set<ModelTypeTocEntry> getModelTypeTocEntries();

    /**
     * Returns all toc's entries representing enum contents.
     */
    public List<EnumContentTocEntry> getEnumContentTocEntries();

    /**
     * Returns the toc entry representing enum contents for the specified implementation class.
     */
    public EnumContentTocEntry getEnumContentTocEntry(String className);

    /**
     * Returns all toc entries that link to an enumeration xml adapter.
     */
    public Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries();

    /**
     * Return the version of the product data in this table of content. This may be a version numbe
     * or a timestamp
     * 
     * @return The version of the table of content
     */
    public String getProductDataVersion();

    <T> CustomTocEntryObject<T> getCustomTocEntry(Class<T> type, String ipsObjectQualifiedName);

}
