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

package org.faktorips.runtime.internal.toc;

import java.util.List;
import java.util.Set;

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
     * Returns the toc entry representing enum contents for the specified implementation class.
     */
    public EnumContentTocEntry getEnumContentTocEntry(String className);

    /**
     * Returns all toc entries that link to an enumeration xml adapter.
     */
    public Set<EnumXmlAdapterTocEntry> getEnumXmlAdapterTocEntries();

}