package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class ForeignKey extends Key implements IForeignKey {
    
    final static String TAG_NAME = "ForeignKey";
    
    // the table structure referenced by this fk.
    private String refTableStructure = "";
    
    // the unique key referenced by this fk.
    private String refUniqueKey = "";

    public ForeignKey(TableStructure tableStructure, int id) {
        super(tableStructure, id);
    }

    protected ForeignKey() {
        super();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        StringBuffer buffer = new StringBuffer(refTableStructure);
        buffer.append('(');
        buffer.append(refUniqueKey);
        buffer.append(')');
        return buffer.toString();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IForeignKey#getReferencedTableStructure()
     */
    public String getReferencedTableStructure() {
        return refTableStructure;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IForeignKey#setReferencedTableStructure(java.lang.String)
     */
    public void setReferencedTableStructure(String tableStructure) {
        String oldValue = refTableStructure;
        refTableStructure = tableStructure;
        valueChanged(oldValue, refTableStructure);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IForeignKey#findReferencedTableStructure()
     */
    public ITableStructure findReferencedTableStructure() throws CoreException {
        return (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, refTableStructure);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IForeignKey#getReferencedUniqueKey()
     */
    public String getReferencedUniqueKey() {
        return refUniqueKey;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IForeignKey#setReferencedUniqueKey(java.lang.String)
     */
    public void setReferencedUniqueKey(String uniqueKey) {
        String oldValue = refUniqueKey;
        refUniqueKey = uniqueKey;
        valueChanged(oldValue, refUniqueKey);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((TableStructure)getTableStructure()).removeForeignKey(this);
    }

    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkIpsObjectReference(refTableStructure, 
                IpsObjectType.TABLE_STRUCTURE, true, "referenced table", 
                this, PROPERTY_REF_TABLE_STRUCTURE, list);
        ITableStructure structure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, refTableStructure);
        if (structure!=null) {
            if (!ValidationUtils.checkStringPropertyNotEmpty(refUniqueKey, "referenced unique key", this, PROPERTY_REF_UNIQUE_KEY, list)) {
                return;
            }
            IUniqueKey uk = structure.getUniqueKey(refUniqueKey);
            if (uk==null) {
                String text = "The table " + refTableStructure + " does not contain an unique key " + refUniqueKey + ".";
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY));
            } else {
                if (uk.getNumOfKeyItems()!=this.getNumOfKeyItems()) {
                    String text = "The foreign key must have as many key items as the referenced unique key.";
                    list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY));
                } else {
                    String[] ukItems = uk.getKeyItemNames();
                    String[] fkItems = this.getKeyItemNames();
                    for (int i=0; i<fkItems.length; i++) {
                       validateKeyItem(fkItems[i], structure, ukItems[i], list); 
                    }
                }
            }
        }
    }
    
    private void validateKeyItem(String fkItem, ITableStructure refStructure, String refItem, MessageList list) throws CoreException {
        IColumnRange range = getTableStructure().getRange(fkItem);
        if (range!=null) {
            validateRangeItem(range, refStructure, refItem, list);
            return;
        }
        IColumn column = getTableStructure().getColumn(fkItem);
        if (column!=null) {
            validateColumnItem(column, refStructure, refItem, list);
            return;
        }
        String text = "The key item " + fkItem + " does not identify a column or range.";
        list.add(new Message("", text, Message.ERROR, fkItem));
        return;
    }
    
    private void validateRangeItem(IColumnRange item, ITableStructure refStructure, String refItem, MessageList list) throws CoreException {
        IColumn column = refStructure.getColumn(refItem);
        if (column!=null) {
            String text = "Foreign key item is a range but referenced key item is a column!";
            list.add(new Message("", text, Message.ERROR, item.getName()));
            return;
        }
        IColumnRange refRange = refStructure.getRange(refItem);
        if (refRange==null) {
            String text = "Can't check datatype validity because the item " + refItem + " in the referenced unique key does not identify a range.";
            list.add(new Message("", text, Message.WARNING, item.getName()));
            return;
        }
        IColumn from = getTableStructure().getColumn(item.getFromColumn());
        IColumn to = getTableStructure().getColumn(item.getToColumn());
        if (from==null || to==null) {
            String text = "Can't check datatype validity because the range is invalid.";
            list.add(new Message("", text, Message.WARNING, item.getName()));
            return;
        }
        IColumn refFrom = refStructure.getColumn(refRange.getFromColumn());
        IColumn refTo = refStructure.getColumn(refRange.getToColumn());
        if (refFrom==null || refTo==null) {
            String text = "Can't check datatype validity because the range in the referenced key is invalid.";
            list.add(new Message("", text, Message.WARNING, item.getName()));
            return;
        }
        if (!from.getDatatype().equals(refFrom.getDatatype())) {
            String text = "The foreign key column " + from.getName() + " has a different datatype than it's corresponding column " + refFrom + ".";
            list.add(new Message("", text, Message.ERROR, item.getName()));
        }
        if (!to.getDatatype().equals(refTo.getDatatype())) {
            String text = "The foreign key column " + to.getName() + " has a different datatype than it's corresponding column " + refTo + ".";
            list.add(new Message("", text, Message.ERROR, item.getName()));
        }
    }
    
    private void validateColumnItem(IColumn item, ITableStructure refStructure, String refItem, MessageList list) throws CoreException {
        IColumnRange range = refStructure.getRange(refItem);
        if (range!=null) {
            String text = "Foreign key item is a column but referenced key item is a range!";
            list.add(new Message("", text, Message.ERROR, item.getName()));
            return;
        }
        IColumn refColumn = refStructure.getColumn(refItem);
        if (refColumn==null) {
            String text = "Can't check datatype validity because the item " + refItem + " in the referenced unique key does not identify a column.";
            list.add(new Message("", text, Message.WARNING, item.getName()));
            return;
        }
        if (!item.getDatatype().equals(refColumn.getDatatype())) {
            String text = "The foreign key item " + item.getName() + " has a different datatype than it's corresponding item " + refItem + ".";
            list.add(new Message("", text, Message.ERROR, item.getName()));
            return;
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        refTableStructure = element.getAttribute(PROPERTY_REF_TABLE_STRUCTURE);
        refUniqueKey = element.getAttribute(PROPERTY_REF_UNIQUE_KEY);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_REF_TABLE_STRUCTURE, refTableStructure);
        element.setAttribute(PROPERTY_REF_UNIQUE_KEY, refUniqueKey);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
}
