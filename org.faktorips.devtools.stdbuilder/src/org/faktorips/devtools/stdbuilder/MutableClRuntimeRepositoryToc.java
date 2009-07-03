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

package org.faktorips.devtools.stdbuilder;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.runtime.internal.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.TocEntryObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Table of contents of a mutable classloader runtime repository.
 * 
 * @author Jan Ortmann
 */
public class MutableClRuntimeRepositoryToc extends ReadonlyTableOfContents {

    // a value that is increased every time the registry content's is changed.
    private long modificationStamp;

    public MutableClRuntimeRepositoryToc() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    protected void internalAddEntry(TocEntryObject entry) {
        super.internalAddEntry(entry);
        ++modificationStamp;
    }

    /**
     * Returns a non-negative modification stamp.
     * <p>
     * A table of content's modification stamp gets updated each time the table of content is
     * modified. If a table of content's modification stamp is the same, the table of content has
     * not changed. Conversely, if a table of content's modification stamp is different, it's
     * contents has been modified at least once (possibly several times).
     */
    public long getModificationStamp() {
        return modificationStamp;
    }

    /**
     * Removes all entries and updated the modification stamp.
     */
    public void clear() {
        pcIdTocEntryMap.clear();
        pcNameTocEntryMap.clear();
        tableImplClassTocEntryMap.clear();
        tableContentNameTocEntryMap.clear();
        modelTypeNameTocEntryMap.clear();
        enumContentNameTocEntryMap.clear();
        enumXmlAdapterTocEntryMap.clear();
        ++modificationStamp;
    }

    /**
     * Adds the entry to the table of contents or replaces the existing entry with the same product
     * component id. If entry is <code>null</code> the table of contents remains unchanged.
     * 
     * @return <code>true</code> if the operation has changed the table of contents, either the
     *         entry was added or the entry has replcaed an existing entry with a differnt content.
     *         Returns <code>false</code> if the operation hasn't changed the table of contents.
     */
    public boolean addOrReplaceTocEntry(TocEntryObject entry) {
        if (entry == null) {
            return false;
        }

        if (entry.isProductCmptTypeTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)pcIdTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            pcIdTocEntryMap.put(entry.getIpsObjectId(), entry);
            pcNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            ++modificationStamp;
            return true;
        }

        if (entry.isTableTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)tableContentNameTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            // can't decide here if new toc entry is multi-content table or not, so put the entry in
            // both maps
            // client must know whether to ask for qualified table name (single and multi content
            // tables) or class name (single content tables only)
            tableContentNameTocEntryMap.put(entry.getIpsObjectId(), entry);
            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), entry);
            ++modificationStamp;
            return true;
        }

        if (entry.isTestCaseTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)testCaseNameTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            ++modificationStamp;
            return true;
        }

        if (entry.isModelTypeTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)modelTypeNameTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            modelTypeNameTocEntryMap.put(entry.getIpsObjectId(), entry);
            ++modificationStamp;
            return true;
        }

        if (entry.isFormulaTestTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)testCaseNameTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            // formula test a kind of test cases therefore the same entry map will be used
            // the formula test will be stored with the qualified name as key, only so it could be
            // removed if a product cmpt will be removed
            testCaseNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            ++modificationStamp;
            return true;
        }

        if (entry.isEnumContentTypeTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)enumContentNameTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }

            enumContentNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            ++modificationStamp;

            return true;
        }
        if (entry.isEnumXmlAdapterTocEntry()) {
            TocEntryObject currentEntry = (TocEntryObject)enumXmlAdapterTocEntryMap.get(entry.getIpsObjectId());
            if (entry.equals(currentEntry)) {
                return false;
            }
            
            enumXmlAdapterTocEntryMap.put(entry.getIpsObjectId(), entry);
            ++modificationStamp;
            
            return true;
        }

        throw new IllegalArgumentException("Unknown toc entry type " + entry); //$NON-NLS-1$
    }

    /**
     * Removes the toc entry with the given object id (full qualified name for tables, runtime id or
     * full qualified name for product components). Does nothing if the id does not identify an
     * entry.
     */
    public boolean removeEntry(String objectId) {
        TocEntryObject removed = (TocEntryObject)pcNameTocEntryMap.remove(objectId);
        if (removed == null) {
            removed = (TocEntryObject)pcIdTocEntryMap.remove(objectId);
        }

        if (removed != null) {
            pcIdTocEntryMap.remove(removed.getIpsObjectId());
            pcNameTocEntryMap.remove(removed.getIpsObjectQualifiedName());
            // remove formula test, based on product cmpt, the objectId of the product cmpt is the
            // identifier for the test case object
            testCaseNameTocEntryMap.remove(objectId);
            ++modificationStamp;
            return true;
        }

        for (Iterator<TocEntryObject> it = tableContentNameTocEntryMap.values().iterator(); it.hasNext();) {
            TocEntryObject entry = (TocEntryObject)it.next();
            if (entry.getIpsObjectId().equals(objectId)) {
                it.remove();
                ++modificationStamp;
                break;
            }
        }

        for (Iterator<TocEntryObject> it = tableImplClassTocEntryMap.values().iterator(); it.hasNext();) {
            TocEntryObject entry = (TocEntryObject)it.next();
            if (entry.getImplementationClassName().equals(objectId)) {
                it.remove();
                ++modificationStamp;
                return true;
            }
        }
        for (Iterator<TocEntryObject> it = testCaseNameTocEntryMap.values().iterator(); it.hasNext();) {
            TocEntryObject entry = (TocEntryObject)it.next();
            if (entry.getIpsObjectQualifiedName().equals(objectId)) {
                it.remove();
                ++modificationStamp;
                return true;
            }
        }

        for (Iterator<TocEntryObject> it = modelTypeNameTocEntryMap.values().iterator(); it.hasNext();) {
            TocEntryObject entry = (TocEntryObject)it.next();
            if (entry.getIpsObjectQualifiedName().equals(objectId)) {
                it.remove();
                ++modificationStamp;
                return true;
            }
        }

        return false;
    }

    /**
     * Transforms the table of contents to xml.
     * 
     * @param doc The xml document used to create new objects.
     * @throws NullPointerException if doc is <code>null</code>.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(AbstractReadonlyTableOfContents.TOC_XML_ELEMENT);
        Comparator<TocEntryObject> c = new Comparator<TocEntryObject>() {

            public int compare(TocEntryObject o1, TocEntryObject o2) {
                return o1.getIpsObjectId().compareTo(o2.getIpsObjectId());
            }

        };
        SortedSet<TocEntryObject> sortedEntries = new TreeSet<TocEntryObject>(c);
        sortedEntries.addAll(pcIdTocEntryMap.values());
        sortedEntries.addAll(tableContentNameTocEntryMap.values());
        sortedEntries.addAll(testCaseNameTocEntryMap.values());
        sortedEntries.addAll(modelTypeNameTocEntryMap.values());
        sortedEntries.addAll(enumContentNameTocEntryMap.values());
        sortedEntries.addAll(enumXmlAdapterTocEntryMap.values());
        for (TocEntryObject entry : sortedEntries) {
            element.appendChild(entry.toXml(doc));
        }
        return element;
    }
}
