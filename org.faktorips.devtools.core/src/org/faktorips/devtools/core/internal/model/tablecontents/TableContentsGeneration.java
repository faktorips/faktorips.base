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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
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
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return getRows();
    }

    /**
     * {@inheritDoc}
     */
    public IRow[] getRows() {
        IRow[] r = new IRow[rows.size()];
        rows.toArray(r);
        return r;
    }
    
    /**
     * {@inheritDoc}
     */
    public IRow getRow(int rowIndex) {
        if(rowIndex<0 || rowIndex>=getNumOfRows()){
            return null;
        }
        return (IRow) rows.get(rowIndex);
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNumOfRows() {
        return rows.size();
    }
    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        // nothing else to do
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        // nothing else to do
    }
    
    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
    	String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(Row.TAG_NAME)) {
            return newRowInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IRow) {
            rows.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
     * Removes the given row from the list of rows and updates the rownumbers of all following rows.
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IRow) {
            Row row = (Row)part;
            int delIndex= rows.indexOf(row);
            if(delIndex != -1){
                rows.remove(delIndex);
                // update rownumbers after delete
                for(int i=delIndex; i<rows.size(); i++){
                    Row updateRow= (Row) rows.get(i);
                    updateRow.setRowNumber(i);
                }
            }
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
        
    }
    
    /**
     * {@inheritDoc}
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

    public ITableContents getTableContents(){
        return (ITableContents)parent;
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        if(!list.isEmpty()){
            return;
        }
        ITableStructure structure = getTableContents().findTableStructure();
        if(structure.isModelEnumType()){
            IColumn column = getTableContents().findTableStructure().getColumn(0);
            ValueDatatype idDatatype = column.findValueDatatype();
            IRow[] rows = getRows();
            for (int i = 0; i < rows.length; i++) {
                for (int j = i + 1; j < rows.length; j++) {
                    if(idDatatype.areValuesEqual(rows[i].getValue(0), rows[j].getValue(0))){
                        list.add(new Message("", "There is already a value defined with the same id.", 
                                Message.ERROR, new ObjectProperty[]{new ObjectProperty(rows[i], IRow.PROPERTY_VALUE, 0), 
                                new ObjectProperty(rows[j], IRow.PROPERTY_VALUE, 0)}));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IRow insertRowAfter(int rowIndex) {
        int newRowIndex = (rowIndex+1)>getNumOfRows()?getNumOfRows():rowIndex+1;
        Row newRow = new Row(this, getNextPartId());
        rows.add(newRowIndex, newRow);
        newRow.setRowNumber(newRowIndex);
        refreshRowNumbers();
        return newRow;
    }
    
    /*
     * Refreshs the row number of all rows.
     */
    private void refreshRowNumbers(){
        int index=0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row)iter.next();
            row.setRowNumber(index++);
        }
    }
    
}
