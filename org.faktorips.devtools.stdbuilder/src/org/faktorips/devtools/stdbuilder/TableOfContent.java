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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IEnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.ITocEntryObject;
import org.faktorips.runtime.internal.toc.PolicyCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Table of contents to create the toc-file - used by {@link TocFileBuilder}
 * 
 * @author dirmeier
 */
public class TableOfContent {

    /*
     * Modified is true if there was any change since last initFromXml or toXml call (or
     * resetModified)
     */
    private boolean modified;

    private Map<QualifiedNameType, ITocEntryObject> entriesMap = new HashMap<QualifiedNameType, ITocEntryObject>(100);

    public TableOfContent() {
        super();
    }

    /**
     * Check if the table of content was modified since last {@link #initFromXml(Element)},
     * {@link #toXml(Document)} or {@link #resetModified()}
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Setting modified to false.
     */
    public void resetModified() {
        modified = false;
    }

    /**
     * Removes all entriesMap and updated the modification stamp.
     */
    public void clear() {
        entriesMap.clear();
        modified = true;
    }

    /**
     * Adds the entry to the table of contents or replaces the existing entry with the same product
     * component id. If entry is <code>null</code> the table of contents remains unchanged.
     * 
     * @return true if the table of content was changed
     */
    public boolean addOrReplaceTocEntry(ITocEntryObject entry) {
        if (entry != null) {
            ITocEntryObject oldValue;
            oldValue = entriesMap.put(getQualifiedNameType(entry), entry);
            if (entry.equals(oldValue)) {
                return false;
            } else {
                modified = true;
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Removes the toc entry with the given object id (full qualified name for tables, runtime id or
     * full qualified name for product components). Does nothing if the id does not identify an
     * entry.
     */
    public ITocEntryObject removeEntry(QualifiedNameType id) {
        ITocEntryObject oldValue = entriesMap.remove(id);
        if (oldValue != null) {
            modified = true;
            return oldValue;
        } else {
            return null;
        }
    }

    public Set<ITocEntryObject> getEntries() {
        Comparator<ITocEntryObject> c = new Comparator<ITocEntryObject>() {

            @Override
            public int compare(ITocEntryObject o1, ITocEntryObject o2) {
                return getQualifiedNameType(o1).compareTo(getQualifiedNameType(o2));
            }

        };
        SortedSet<ITocEntryObject> sortedEntries = new TreeSet<ITocEntryObject>(c);
        sortedEntries.addAll(entriesMap.values());
        return sortedEntries;
    }

    public ITocEntryObject getEntry(QualifiedNameType id) {
        return entriesMap.get(id);
    }

    /**
     * Transforms the table of contents to xml.
     * 
     * @param doc The xml document used to create new objects.
     * @throws NullPointerException if doc is <code>null</code>.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(AbstractReadonlyTableOfContents.TOC_XML_ELEMENT);
        long lastModified = new Date().getTime();
        element.setAttribute(AbstractReadonlyTableOfContents.LASTMOD_XML_ELEMENT, "" + lastModified);
        for (ITocEntryObject entry : getEntries()) {
            element.appendChild(entry.toXml(doc));
        }
        modified = false;
        return element;
    }

    public void initFromXml(Element tocElement) {
        NodeList nl = tocElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element entryElement = (Element)nl.item(i);
                addOrReplaceTocEntry(TocEntryObject.createFromXml(entryElement));
            }
        }
        modified = false;
    }

    private static IpsObjectType getIpsObjectType(ITocEntryObject entry) {
        if (entry instanceof IProductCmptTocEntry) {
            return IpsObjectType.PRODUCT_CMPT;
        } else if (entry instanceof IEnumContentTocEntry) {
            return IpsObjectType.ENUM_CONTENT;
        } else if (entry instanceof ITestCaseTocEntry) {
            return IpsObjectType.TEST_CASE;
        } else if (entry instanceof ITableContentTocEntry) {
            return IpsObjectType.TABLE_CONTENTS;
        } else if (entry instanceof PolicyCmptTypeTocEntry) {
            return IpsObjectType.POLICY_CMPT_TYPE;
        } else if (entry instanceof ProductCmptTypeTocEntry) {
            return IpsObjectType.PRODUCT_CMPT_TYPE;
        } else if (entry instanceof IEnumXmlAdapterTocEntry) {
            return IpsObjectType.ENUM_TYPE;
        } else {
            return null;
        }
    }

    private static QualifiedNameType getQualifiedNameType(ITocEntryObject entry) {
        IpsObjectType type = getIpsObjectType(entry);
        if (type != null) {
            return new QualifiedNameType(entry.getIpsObjectQualifiedName(), type);
        } else {
            throw new RuntimeException("Entry does not math an IpsObjectType! Could not insert to Table of Content");
        }
    }
}
