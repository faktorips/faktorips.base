/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.Iterator;

import org.faktorips.runtime.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.TocEntryObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Table of contents of a mutable classloader runtime repository.
 * 
 * @author Jan Ortmann
 */
public class MutableClRuntimeRepositoryToc extends ReadonlyTableOfContents{

    // a value that is increased every time the registry content's is changed.
    private long modificationStamp;
    
    public MutableClRuntimeRepositoryToc() {
        super();
    }

    /**
     * Overridden.
     */
    protected void internalAddEntry(TocEntryObject entry) {
        super.internalAddEntry(entry);
        ++modificationStamp;
    }
    
	/**
	 * Returns a non-negative modification stamp.
	 * <p>
	 * A table of content's modification stamp gets updated each time the table of content is modified.
	 * If a table of content's modification stamp is the same, the table of content has not changed.
	 * Conversely, if a table of content's modification stamp is different, it's contents has been
	 * modified at least once (possibly several times).
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
        ++modificationStamp;
    }
    
	/**
	 * Adds the entry to the table of contents or replaces the existing entry with the same product component id.
	 * If entry is <code>null</code> the table of contents remains unchanged.
	 * 
	 * @return <code>true</code> if the operation has changed the table of contents, either the entry was added
	 * or the entry has replcaed an existing entry with a differnt content. Returns <code>false</code> if the operation
	 * hasn't changed the table of contents.
	 */
	public boolean addOrReplaceTocEntry(TocEntryObject entry) {
        if (entry==null) {
            return false;
        }
        
        if(entry.isProductCmptTypeTocEntry()){
            TocEntryObject currentEntry = (TocEntryObject)pcIdTocEntryMap.get(entry.getIpsObjectId());
            if(entry.equals(currentEntry)){
                return false;
            }
            pcIdTocEntryMap.put(entry.getIpsObjectId(), entry);
            pcNameTocEntryMap.put(entry.getIpsObjectQualifiedName(), entry);
            ++modificationStamp;
            return true;
        }

        if(entry.isTableTocEntry()){
            TocEntryObject currentEntry = (TocEntryObject)tableContentNameTocEntryMap.get(entry.getIpsObjectId());

            if(entry.equals(currentEntry)){
                return false;
            }

            // can't decide here if new toc entry is multi-content table or not, so put the entry in both maps
            // client must know whether to ask for qualified table name (single and multi content tables) or class name (single content tables only)
            tableContentNameTocEntryMap.put(entry.getIpsObjectId(), entry);
            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), entry);
            ++modificationStamp;
            return true;
        }
        
        throw new IllegalArgumentException("Unknown  toc entry type " + entry);
	}
	
	/**
     * Removes the toc entry with the given object id (full qualified name for tables, runtime id
     * or full qualified name for product components). Does nothing if the id does not identify an entry.
     */
	public boolean removeEntry(String objectId) {
        TocEntryObject removed = (TocEntryObject)pcNameTocEntryMap.remove(objectId);
        if (removed == null) {
            removed = (TocEntryObject)pcIdTocEntryMap.remove(objectId);
        }
        
        if (removed != null) {
            pcIdTocEntryMap.remove(removed.getIpsObjectId());
            pcNameTocEntryMap.remove(removed.getIpsObjectQualifiedName());
            
            ++modificationStamp;
            return true;
        }
        
	    for (Iterator it=tableContentNameTocEntryMap.values().iterator(); it.hasNext();) {
	    	TocEntryObject entry = (TocEntryObject)it.next();
	    	if (entry.getIpsObjectId().equals(objectId)) {
                it.remove();
                ++modificationStamp;
                break;
            }
        }
        for (Iterator it=tableImplClassTocEntryMap.values().iterator(); it.hasNext();) {
            TocEntryObject entry = (TocEntryObject)it.next();
            if (entry.getImplementationClassName().equals(objectId)) {
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
	    ArrayList allEntries = new ArrayList(pcIdTocEntryMap.size() + tableContentNameTocEntryMap.size());
	    allEntries.addAll(pcIdTocEntryMap.values());
	    allEntries.addAll(tableContentNameTocEntryMap.values());
        for (Iterator it=allEntries.iterator(); it.hasNext(); ) {
            TocEntryObject entry = (TocEntryObject)it.next();
            element.appendChild(entry.toXml(doc));
        }
	    return element;
	}
}
