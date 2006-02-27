package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableAccessFunction extends IpsObjectPart implements ITableAccessFunction {

    private String accessedColumn;
    private String type;
    private String[] argTypes = new String[0];
    
    public TableAccessFunction(IIpsObject parent, int id) {
        super(parent, id);
    }

    public TableAccessFunction(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    public TableAccessFunction() {
        super();
    }
    
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    protected Element createElement(Document doc) {
        return null;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getAccessedColumn() {
        return accessedColumn;
    }

    public void setAccessedColumn(String columnName) {
        this.accessedColumn = columnName;
    }
    
    /**
     * Overridden.
     */
    public IColumn findAccessedColumn() {
        return getTableStructure().getColumn(accessedColumn);
    }

    public String getType() {
        return type;
    }
    
    public void setType(String newType) {
        this.type = newType;
    }
    
    public void setArgTypes(String[] types) {
        // make a defensive copy.
        argTypes = new String[types.length];
        System.arraycopy(types, 0, argTypes, 0, types.length);
    }

    public String[] getArgTypes() {
        String[] types = new String[argTypes.length];
        System.arraycopy(argTypes, 0, types, 0, argTypes.length);
        return types; // return defensive copy
    }

    public void delete() {
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    public Image getImage() {
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
