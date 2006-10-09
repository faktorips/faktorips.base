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

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.w3c.dom.Element;


/**
 *
 */
public class TableContentsGeneration extends IpsObjectGeneration implements ITableContentsGeneration {

    private List rows = new ArrayList(100);

    public TableContentsGeneration(TableContents parent, int id) {
        super(parent, id);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    public IIpsElement[] getChildren() {
        return getRows();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration#getRows()
     */
    public IRow[] getRows() {
        IRow[] r = new IRow[rows.size()];
        rows.toArray(r);
        return r;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration#getNumOfRows()
     */
    public int getNumOfRows() {
        return rows.size();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration#newRow()
     */
    public IRow newRow() {
        IRow newRow = newRowInternal(getNextPartId());
        objectHasChanged();
        return newRow;
    }
    
    /**
     * Creates a new row and inserts it into the list of rows. The rownumber of the
     * new row is its index in the list (respectively the number of rows before the insertion).
     * @param id
     * @return
     */
    private Row newRowInternal(int id) {
        int nextRowNumber= getNumOfRows();
        Row newRow = new Row(this, id);
        rows.add(newRow);
        newRow.setRowNumber(nextRowNumber);
        return newRow;
    }
    
    /**
     * Removes the given row from the list of rows and updates the rownumbers of all following rows.
     * @param row
     */
    void removeRow(IRow row) {
        int delIndex= rows.indexOf(row);
        if(delIndex != -1){
            rows.remove(delIndex);
            // update rownumbers after delete
            for(int i=delIndex; i<rows.size(); i++){
                Row updateRow= (Row) rows.get(i);
                updateRow.setRowNumber(i);
            }
        }
    }

    public void newColumn(int insertAt, String defaultValue) {
        for (Iterator it=rows.iterator(); it.hasNext(); ) {
            Row row = (Row)it.next();
            row.newColumn(insertAt, defaultValue);
        }
    }
    
    public void removeColumn(int column) {
        for (Iterator it=rows.iterator(); it.hasNext(); ) {
            Row row = (Row)it.next();
            row.removeColumn(column);
        }
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        // nothing else to do
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        // nothing else to do
    }
    
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
    	String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(Row.TAG_NAME)) {
            return newRowInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IRow) {
            rows.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reinitPartCollections()
     */
    protected void reinitPartCollections() {
        rows.clear();
    }

    /**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		if (partType.equals(IRow.class)) {
            return newRowInternal(this.getNextPartId());
		}
		
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
    
    /**
     * {@inheritDoc}
     */
    public void clear() {
        rows.clear();
        objectHasChanged();
    }
}
