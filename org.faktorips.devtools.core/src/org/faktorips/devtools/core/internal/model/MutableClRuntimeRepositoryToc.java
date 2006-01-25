package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.ReadonlyTableOfContentsImpl;
import org.faktorips.runtime.TocEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Table of contents of a mutable classloader runtime repository.
 * 
 * @author Jan Ortmann
 */
public class MutableClRuntimeRepositoryToc extends ReadonlyTableOfContentsImpl{

    // a value that is increased every time the registry content's is changed.
    private long modificationStamp;
    
    public MutableClRuntimeRepositoryToc() {
        super();
    }

    /**
     * Overridden.
     */
    protected void internalAddEntry(TocEntry entry) {
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
        pcNameTocEntryMap.clear();
        tableImplClassTocEntryMap.clear();
        ++modificationStamp;
    }
    
	/**
	 * Adds the entry to the table of contents or replaces the existing entry with the same product component name.
	 * If entry is <code>null</code> the table of contents remains unchanged.
	 * 
	 * @return <code>true</code> if the operation has changed the table of contents, either the entry was added
	 * or the entry has replcaed an existing entry with a differnt contents. Returns <code>false</code> if the operation
	 * hasn't changed the table of contents.
	 */
	public boolean addOrReplaceTocEntry(TocEntry entry) {
        if (entry==null) {
            return false;
        }
        
        if(entry.isProductCmptTypeTocEntry()){
            TocEntry currentEntry = (TocEntry)pcNameTocEntryMap.get(entry.getIpsObjectName());
            
            if(entry.equals(currentEntry)){
                return false;
            }

            pcNameTocEntryMap.put(entry.getIpsObjectName(), entry);
            ++modificationStamp;
            return true;
        }

        if(entry.isTableTocEntry()){
            TocEntry currentEntry = (TocEntry)tableImplClassTocEntryMap.get(entry.getImplementationClassName());

            if(entry.equals(currentEntry)){
                return false;
            }

            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), entry);
            ++modificationStamp;
            return true;
        }
        
        throw new IllegalArgumentException("A toc entry of an unexpected type has been provided to this method: " + entry);
	}
	
	/**
	 * Removes the toc entry for the given fully qualified product component name from the table of contents.
	 * Does nothing if productCmpName is <code>null</code>.
	 * 
	 * @param productCmptName The fully qualified product component name.
	 */
	public void removeEntry(TocEntry entry) {
	    
	    Object oldValue = null;
	    if(entry.isProductCmptTypeTocEntry()){
	        oldValue = pcNameTocEntryMap.remove(entry.getIpsObjectName());
	    }
	    
	    if(entry.isTableTocEntry()){
	        oldValue = tableImplClassTocEntryMap.remove(entry.getImplementationClassName());
	    }
	    
	    //check of the return value is save since it is guaranteed that null is never associated with a key
        if(oldValue != null){
            ++modificationStamp;
        }
	}
    
	/**
	 * Transforms the table of contents to xml.
	 * 
	 * @param doc The xml document used to create new objects.
	 * @throws NullPointerException if doc is <code>null</code>.
	 */
	public Element toXml(Document doc) {
	    Element element = doc.createElement(ReadonlyTableOfContents.TOC_XML_ELEMENT);
	    ArrayList allEntries = new ArrayList(pcNameTocEntryMap.size() + tableImplClassTocEntryMap.size());
	    allEntries.addAll(pcNameTocEntryMap.values());
	    allEntries.addAll(tableImplClassTocEntryMap.values());
        for (Iterator it=allEntries.iterator(); it.hasNext(); ) {
            TocEntry entry = (TocEntry)it.next();
            element.appendChild(entry.toXml(doc));
        }
	    return element;
	}
}
