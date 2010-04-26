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

package org.faktorips.runtime.internal;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A read-only table contents for the runtime repository.
 * <p>
 * The table of contents contains a list of toc entries that contain the information needed to
 * identify and load the objects stored in the repository.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractReadonlyTableOfContents {

    public final static String TOC_XML_ELEMENT = "FaktorIps-TableOfContents";
    protected final static String TOC_ENTRY_XML_ELEMENT = "Entry";

    public AbstractReadonlyTableOfContents() {

    }

    /**
     * Initializes the table of contents with data stored in the xml element.
     */
    public final void initFromXml(Element tocElement) {
        NodeList nl = tocElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(TOC_ENTRY_XML_ELEMENT)) {
                Element entryElement = (Element)nl.item(i);
                internalAddEntry(TocEntryObject.createFromXml(entryElement));
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
    public abstract TocEntryObject getProductCmptTocEntry(String id);

    /**
     * Returns the toc entry representing a product component for the given product component kind
     * id and versionId or null if no such entry exists.
     */
    public abstract TocEntryObject getProductCmptTocEntry(String kindId, String versionId);

    /**
     * Returns all toc's entries representing product components.
     */
    public abstract List<TocEntryObject> getProductCmptTocEntries();

    /**
     * Returns all toc's entries representing product components that belong to the indicated
     * product component kind.
     */
    public abstract List<TocEntryObject> getProductCmptTocEntries(String kindId);

    /**
     * Returns all toc's entries representing tables.
     */
    public abstract List<TocEntryObject> getTableTocEntries();

    /**
     * Returns all toc's entries representing test cases.
     */
    public abstract List<TocEntryObject> getTestCaseTocEntries();

    /**
     * Returns a toc entry representing a test case for the given qualified name.
     */
    public abstract TocEntryObject getTestCaseTocEntryByQName(String qName);

    /**
     * Returns a toc entry representing a table for the table's class object.
     */
    public abstract TocEntryObject getTableTocEntryByClassname(String implementationClass);

    /**
     * Returns a toc entry representing a table for this table's qualified table name.
     */
    public abstract TocEntryObject getTableTocEntryByQualifiedTableName(String qualifiedTableName);

    /**
     * Returns all toc's entries representing model types.
     */
    public abstract Set<TocEntryObject> getModelTypeTocEntries();

    /**
     * Returns all toc's entries representing enum contents.
     */
    public abstract Set<TocEntryObject> getEnumContentTocEntries();

    /**
     * Returns all toc entries that link to an enumeration xml adapter.
     */
    public abstract Set<TocEntryObject> getEnumXmlAdapterTocEntries();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("TOC");
        buf.append(System.getProperty("line.separator"));
        List<TocEntryObject> entries = getProductCmptTocEntries();
        for (TocEntryObject entry : entries) {
            buf.append(entry.toString());
            buf.append(System.getProperty("line.separator"));
        }

        entries = getTableTocEntries();
        for (TocEntryObject entry : entries) {
            buf.append(entry.toString());
            buf.append(System.getProperty("line.separator"));
        }

        entries = getTestCaseTocEntries();
        for (TocEntryObject entry : entries) {
            buf.append(entry.toString());
            buf.append(System.getProperty("line.separator"));
        }

        return buf.toString();
    }

}
