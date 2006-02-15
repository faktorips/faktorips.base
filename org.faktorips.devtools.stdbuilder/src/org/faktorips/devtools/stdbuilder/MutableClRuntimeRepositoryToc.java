package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.Iterator;

import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.ReadonlyTableOfContentsImpl;
import org.faktorips.runtime.TocEntryObject;
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
	public boolean addOrReplaceTocEntry(TocEntryObject entry) {
        if (entry==null) {
            return false;
        }
        
        if(entry.isProductCmptTypeTocEntry()){
            TocEntryObject currentEntry = (TocEntryObject)pcNameTocEntryMap.get(entry.getIpsObjectName());
            if(entry.equals(currentEntry)){
                return false;
            }
            pcNameTocEntryMap.put(entry.getIpsObjectName(), entry);
            ++modificationStamp;
            return true;
        }

        if(entry.isTableTocEntry()){
            TocEntryObject currentEntry = (TocEntryObject)tableImplClassTocEntryMap.get(entry.getImplementationClassName());

            if(entry.equals(currentEntry)){
                return false;
            }

            tableImplClassTocEntryMap.put(entry.getImplementationClassName(), entry);
            ++modificationStamp;
            return true;
        }
        
        throw new IllegalArgumentException("Unknown  toc entry type " + entry);
	}
	
	/**
	 * Removes the toc entry with the given qualified ips object name. Does nothing if the
	 * name does not identify an entry.
	 */
	public boolean removeEntry(String qName) {
	    if (pcNameTocEntryMap.remove(qName)!=null) {
	    	++modificationStamp;
	    	return true;
	    }
	    for (Iterator it=tableImplClassTocEntryMap.values().iterator(); it.hasNext();) {
	    	TocEntryObject entry = (TocEntryObject)it.next();
	    	if (entry.getIpsObjectName().equals(qName)) {
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
	    Element element = doc.createElement(ReadonlyTableOfContents.TOC_XML_ELEMENT);
	    ArrayList allEntries = new ArrayList(pcNameTocEntryMap.size() + tableImplClassTocEntryMap.size());
	    allEntries.addAll(pcNameTocEntryMap.values());
	    allEntries.addAll(tableImplClassTocEntryMap.values());
        for (Iterator it=allEntries.iterator(); it.hasNext(); ) {
            TocEntryObject entry = (TocEntryObject)it.next();
            element.appendChild(entry.toXml(doc));
        }
	    return element;
	}
}
