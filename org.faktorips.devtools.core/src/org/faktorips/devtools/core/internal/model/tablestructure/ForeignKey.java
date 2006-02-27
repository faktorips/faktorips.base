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
    
    final static String TAG_NAME = "ForeignKey"; //$NON-NLS-1$
    
    // the table structure referenced by this fk.
    private String refTableStructure = ""; //$NON-NLS-1$
    
    // the unique key referenced by this fk.
    private String refUniqueKey = ""; //$NON-NLS-1$

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
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkIpsObjectReference(refTableStructure, 
                IpsObjectType.TABLE_STRUCTURE, true, "referenced table",  //$NON-NLS-1$
                this, PROPERTY_REF_TABLE_STRUCTURE, list);
        ITableStructure structure = (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, refTableStructure);
        if (structure!=null) {
            if (!ValidationUtils.checkStringPropertyNotEmpty(refUniqueKey, "referenced unique key", this, PROPERTY_REF_UNIQUE_KEY, list)) { //$NON-NLS-1$
                return;
            }
            IUniqueKey uk = structure.getUniqueKey(refUniqueKey);
            if (uk==null) {
                String text = Messages.ForeignKey_msgMissingUniqueKey + refTableStructure + Messages.ForeignKey_6 + refUniqueKey + Messages.ForeignKey_7;
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY)); //$NON-NLS-1$
            } else {
                if (uk.getNumOfKeyItems()!=this.getNumOfKeyItems()) {
                    String text = Messages.ForeignKey_msgMalformedForeignKey;
                    list.add(new Message("", text, Message.ERROR, this, PROPERTY_REF_UNIQUE_KEY)); //$NON-NLS-1$
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
        String text = Messages.ForeignKey_msgInvalidKeyItem + fkItem + Messages.ForeignKey_12;
        list.add(new Message("", text, Message.ERROR, fkItem)); //$NON-NLS-1$
        return;
    }
    
    private void validateRangeItem(IColumnRange item, ITableStructure refStructure, String refItem, MessageList list) throws CoreException {
        IColumn column = refStructure.getColumn(refItem);
        if (column!=null) {
            String text = Messages.ForeignKey_msgKeyItemMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumnRange refRange = refStructure.getRange(refItem);
        if (refRange==null) {
            String text = Messages.ForeignKey_msgNotARange + refItem + Messages.ForeignKey_17;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumn from = getTableStructure().getColumn(item.getFromColumn());
        IColumn to = getTableStructure().getColumn(item.getToColumn());
        if (from==null || to==null) {
            String text = Messages.ForeignKey_msgInvalidRange;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumn refFrom = refStructure.getColumn(refRange.getFromColumn());
        IColumn refTo = refStructure.getColumn(refRange.getToColumn());
        if (refFrom==null || refTo==null) {
            String text = Messages.ForeignKey_msgReferencedRangeInvalid;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (!from.getDatatype().equals(refFrom.getDatatype())) {
            String text = Messages.ForeignKey_msgForeignKeyDatatypeMismatch + from.getName() + Messages.ForeignKey_24 + refFrom + Messages.ForeignKey_25;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        }
        if (!to.getDatatype().equals(refTo.getDatatype())) {
            String text = Messages.ForeignKey_msgColumnDatatypeMismatch + to.getName() + Messages.ForeignKey_28 + refTo + Messages.ForeignKey_29;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
        }
    }
    
    private void validateColumnItem(IColumn item, ITableStructure refStructure, String refItem, MessageList list) throws CoreException {
        IColumnRange range = refStructure.getRange(refItem);
        if (range!=null) {
            String text = Messages.ForeignKey_msgKeyMissmatch;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
            return;
        }
        IColumn refColumn = refStructure.getColumn(refItem);
        if (refColumn==null) {
            String text = Messages.ForeignKey_msgNotAColumn + refItem + Messages.ForeignKey_34;
            list.add(new Message("", text, Message.WARNING, item.getName())); //$NON-NLS-1$
            return;
        }
        if (!item.getDatatype().equals(refColumn.getDatatype())) {
            String text = Messages.ForeignKey_msgKeyDatatypeMismatch + item.getName() + Messages.ForeignKey_37 + refItem + Messages.ForeignKey_38;
            list.add(new Message("", text, Message.ERROR, item.getName())); //$NON-NLS-1$
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
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
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
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
