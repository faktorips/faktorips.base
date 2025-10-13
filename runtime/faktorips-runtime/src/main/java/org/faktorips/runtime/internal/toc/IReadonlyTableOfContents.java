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
    ProductCmptTocEntry getProductCmptTocEntry(String id);

    /**
     * Returns the toc entry representing a product component for the given product component kind
     * id and versionId or null if no such entry exists.
     */
    ProductCmptTocEntry getProductCmptTocEntry(String kindId, String versionId);

    /**
     * Returns the toc entry representing a product component for the given qualified name or null
     * if no entry exists.
     */
    ProductCmptTocEntry getProductCmptTocEntryByQualifiedName(String qualifiedName);

    /**
     * Returns all toc's entries representing product components.
     */
    List<ProductCmptTocEntry> getProductCmptTocEntries();

    /**
     * Returns all toc's entries representing product components that belong to the indicated
     * product component kind.
     */
    List<ProductCmptTocEntry> getProductCmptTocEntries(String kindId);

    /**
     * Returns all toc's entries representing tables.
     */
    List<TableContentTocEntry> getTableTocEntries();

    /**
     * Returns all toc's entries representing test cases.
     */
    List<TestCaseTocEntry> getTestCaseTocEntries();

    /**
     * Returns a toc entry representing a test case for the given qualified name.
     */
    TestCaseTocEntry getTestCaseTocEntryByQName(String qName);

    /**
     * Returns a toc entry representing a table for the table's class object.
     */
    TableContentTocEntry getTableTocEntryByClassname(String implementationClass);

    /**
     * Returns a toc entry representing a table for this table's qualified table name.
     */
    TableContentTocEntry getTableTocEntryByQualifiedTableName(String qualifiedTableName);

    /**
     * Returns all toc's entries representing model types.
     */
    Set<ModelTypeTocEntry> getModelTypeTocEntries();

    /**
     * Returns all toc's entries representing enum contents.
     */
    List<EnumContentTocEntry> getEnumContentTocEntries();

    /**
     * Returns the toc entry representing enum contents for the specified implementation class.
     */
    EnumContentTocEntry getEnumContentTocEntry(String className);

    /**
     * Returns the toc entry representing enum contents for the specified qualified name or null if
     * no entry exists.
     */
    EnumContentTocEntry getEnumContentTocEntryByQualifiedName(String qualifiedName);

    /**
     * Returns all toc entries that link to an enumeration xml adapter.
     */
    Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries();

    /**
     * Return the version of the product data in this table of content. This may be a version numbe
     * or a timestamp
     *
     * @return The version of the table of content
     */
    String getProductDataVersion();

    <T> CustomTocEntryObject<T> getCustomTocEntry(Class<T> type, String ipsObjectQualifiedName);

}
