/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.EnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.PolicyCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTypeTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
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

    /**
     * the name of the attribute specifying the actual xml version @see {@link #ACTUAL_XML_VERSION}
     */
    public final static String VERSION_XML_ATTRIBUTE = "xmlversion";

    /**
     * This is the version of the table of contents XML file. If change the XML format that old xml
     * files are incompatible with the newer format you have to increase this version. The version
     * is checked before loading the content of the xml file. If the version is incompatible (not
     * equals) the {@link TocFileBuilder} have to rebuild the whole {@link TableOfContent}.
     * <p>
     * The version is only interesting while building the XML file, especially in
     * {@link #initFromXml(Element)} for incremental build. It is not considered at runtime.
     */
    public final static String ACTUAL_XML_VERSION = "3.0";

    /**
     * Modified is true if there was any change since last initFromXml or toXml call (or
     * resetModified)
     */
    private boolean modified;

    private Map<QualifiedNameType, TocEntryObject> entriesMap = new HashMap<QualifiedNameType, TocEntryObject>(100);

    public TableOfContent() {
        super();
    }

    /**
     * Check if the table of content was modified since last {@link #initFromXml(Element)},
     * {@link #toXml(String, Document)} or {@link #resetModified()}
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
    public boolean addOrReplaceTocEntry(TocEntryObject entry) {
        if (entry != null) {
            TocEntryObject oldValue;
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
    public TocEntryObject removeEntry(QualifiedNameType id) {
        TocEntryObject oldValue = entriesMap.remove(id);
        if (oldValue != null) {
            modified = true;
            return oldValue;
        } else {
            return null;
        }
    }

    public Set<TocEntryObject> getEntries() {
        Comparator<TocEntryObject> c = new Comparator<TocEntryObject>() {

            @Override
            public int compare(TocEntryObject o1, TocEntryObject o2) {
                return getQualifiedNameType(o1).compareTo(getQualifiedNameType(o2));
            }

        };
        SortedSet<TocEntryObject> sortedEntries = new TreeSet<TocEntryObject>(c);
        sortedEntries.addAll(entriesMap.values());
        return sortedEntries;
    }

    public TocEntryObject getEntry(QualifiedNameType id) {
        return entriesMap.get(id);
    }

    /**
     * Transforms the table of contents to xml.
     * 
     * @param doc The xml document used to create new objects.
     * @throws NullPointerException if doc is <code>null</code>.
     */
    public Element toXml(String version, Document doc) {
        Element element = doc.createElement(AbstractReadonlyTableOfContents.TOC_XML_ELEMENT);
        element.setAttribute(VERSION_XML_ATTRIBUTE, ACTUAL_XML_VERSION);
        element.setAttribute(AbstractReadonlyTableOfContents.PRODUCT_DATA_VERSION_XML_ELEMENT, "" + version);
        for (TocEntryObject entry : getEntries()) {
            element.appendChild(entry.toXml(doc));
        }
        modified = false;
        return element;
    }

    public void initFromXml(Element tocElement) {
        String version = tocElement.getAttribute(VERSION_XML_ATTRIBUTE);
        if (!ACTUAL_XML_VERSION.equals(version)) {
            clear();
            modified = true;
            return;
        }
        NodeList nl = tocElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element entryElement = (Element)nl.item(i);
                addOrReplaceTocEntry(TocEntryObject.createFromXml(entryElement));
            }
        }
        modified = false;
    }

    private static IpsObjectType getIpsObjectType(TocEntryObject entry) {
        if (entry instanceof ProductCmptTocEntry) {
            return IpsObjectType.PRODUCT_CMPT;
        } else if (entry instanceof EnumContentTocEntry) {
            return IpsObjectType.ENUM_CONTENT;
        } else if (entry instanceof TestCaseTocEntry) {
            return IpsObjectType.TEST_CASE;
        } else if (entry instanceof TableContentTocEntry) {
            return IpsObjectType.TABLE_CONTENTS;
        } else if (entry instanceof PolicyCmptTypeTocEntry) {
            return IpsObjectType.POLICY_CMPT_TYPE;
        } else if (entry instanceof ProductCmptTypeTocEntry) {
            return IpsObjectType.PRODUCT_CMPT_TYPE;
        } else if (entry instanceof EnumXmlAdapterTocEntry) {
            return IpsObjectType.ENUM_TYPE;
        } else {
            return null;
        }
    }

    private static QualifiedNameType getQualifiedNameType(TocEntryObject entry) {
        IpsObjectType type = getIpsObjectType(entry);
        if (type != null) {
            return new QualifiedNameType(entry.getIpsObjectQualifiedName(), type);
        } else {
            throw new RuntimeException("Entry does not math an IpsObjectType! Could not insert to Table of Content");
        }
    }
}
