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
         updateSrcFile();
         return newRow;
    }
    
    private Row newRowInternal(int id) {
        Row newRow = new Row(this, id);
        rows.add(newRow);
        return newRow;
    }
    
    void removeRow(IRow row) {
        rows.remove(row);
        updateSrcFile();
    }

    void newColumn(String defaultValue) {
        for (Iterator it=rows.iterator(); it.hasNext(); ) {
            Row row = (Row)it.next();
            row.newColumn(defaultValue);
        }
    }
    
    void removeColumn(int column) {
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
    protected IIpsObjectPart newPart(String xmlTagName, int id) {
        if (xmlTagName.equals(Row.TAG_NAME)) {
            return newRowInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName);
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
        throw new RuntimeException("Unknown part type" + part.getClass());
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
		
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
}
