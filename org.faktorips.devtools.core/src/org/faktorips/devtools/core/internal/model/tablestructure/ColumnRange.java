package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class ColumnRange extends IpsObjectPart implements IColumnRange {
    
    final static String TAG_NAME = "Range"; //$NON-NLS-1$

    private String from = ""; //$NON-NLS-1$
    private String to = ""; //$NON-NLS-1$
    private ColumnRangeType rangeType = ColumnRangeType.TWO_COLUMN_RANGE;
    private String parameterName = ""; //$NON-NLS-1$

    public ColumnRange(TableStructure parent, int id) {
        super(parent, id);
    }
    
    TableStructure getTableStructureImpl() {
        return (TableStructure)getParent();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getTableStructure()
     */
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }
    
    /**  
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        return from + '-' + to;
    }

    /**
     * Overridden.
     */
    public String getAccessParameterName() {
        return getName();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getFromColumn()
     */
    public String getFromColumn() {
        return from;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#setFromColumn(org.faktorips.devtools.core.model.tablestructure.IColumn)
     */
    public void setFromColumn(String columnName) {
        ArgumentCheck.notNull(columnName);
        String oldColumnName = from;
        this.from = columnName;
        valueChanged(oldColumnName, from);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getToColumn()
     */
    public String getToColumn() {
        return to;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#setToColumn(org.faktorips.devtools.core.model.tablestructure.IColumn)
     */
    public void setToColumn(String columnName) {
        ArgumentCheck.notNull(columnName);
        String oldColumnName = to;
        this.to = columnName;
        valueChanged(oldColumnName, to);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        getTableStructureImpl().removeRange(this);
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableRange.gif"); //$NON-NLS-1$
    }

    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        if ((rangeType.isTwoColumn() || rangeType.isOneColumnFrom()) && 
             ValidationUtils.checkStringPropertyNotEmpty(from, "from column", this, PROPERTY_FROM_COLUMN, list)) { //$NON-NLS-1$
            if (getTableStructure().getColumn(from)==null) {
                String text = NLS.bind(Messages.ColumnRange_msgMissingColumn, from);
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_FROM_COLUMN)); //$NON-NLS-1$
            }
        }
        
        if((rangeType.isTwoColumn() || rangeType.isOneColumnTo()) && 
            ValidationUtils.checkStringPropertyNotEmpty(to, "to column", this, PROPERTY_TO_COLUMN, list)){ //$NON-NLS-1$
            if (getTableStructure().getColumn(to)==null) {
                String text = NLS.bind(Messages.ColumnRange_msgMissingColumn, to);
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_TO_COLUMN)); //$NON-NLS-1$
            }
        }
        
        if (StringUtils.isEmpty(parameterName)) {
        	String text = Messages.ColumnRange_msgParameterEmpty;
        	list.add(new Message("", text, Message.ERROR, this, PROPERTY_PARAMETER_NAME)); //$NON-NLS-1$
        }
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#setColumnRangeType(org.faktorips.devtools.core.model.tablestructure.ColumnRangeType)
     */
    public void setColumnRangeType(ColumnRangeType rangeType) {
        ArgumentCheck.notNull(rangeType);
        Object oldValue = this.rangeType;
        this.rangeType = rangeType;
        valueChanged(oldValue, rangeType);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getColumnRangeType()
     */
    public ColumnRangeType getColumnRangeType() {
        return rangeType;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getDatatype()
     */
    public String getDatatype() {
        if (rangeType.isTwoColumn() || rangeType.isOneColumnFrom()) {
            return getDatatype(getFromColumn());
        }
        return getDatatype(getToColumn());
    }

    private String getDatatype(String columnName) {
        IColumn column  = getTableStructure().getColumn(columnName);
        if(column != null){
            return column.getDatatype();
        }
        return null;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.tablestructure.IColumnRange#getParameterName()
     */
    public String getParameterName() {
    	return parameterName;
    }

	/**
	 * @param parameterName Sets the parameterName
	 */
	public void setParameterName(String parameterName) {
		ArgumentCheck.notNull(parameterName);
		String oldParameterName = this.parameterName;
		this.parameterName = parameterName;
		valueChanged(oldParameterName, parameterName);
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
        rangeType = ColumnRangeType.getValueById(element.getAttribute(PROPERTY_RANGE_TYPE));
        from = element.getAttribute(PROPERTY_FROM_COLUMN);
        to = element.getAttribute(PROPERTY_TO_COLUMN);
        parameterName = element.getAttribute(PROPERTY_PARAMETER_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_RANGE_TYPE, rangeType.getId());
        element.setAttribute(PROPERTY_FROM_COLUMN, from);
        element.setAttribute(PROPERTY_TO_COLUMN, to);
        element.setAttribute(PROPERTY_PARAMETER_NAME, parameterName);
    }

    /**
     * Overridden.
     */
    public IColumn[] getColumns() {
        List columns = new ArrayList();
        if (!rangeType.isOneColumnTo()) {
            if (getTableStructure().getColumn(from)!=null) {
                columns.add(getTableStructure().getColumn(from));
            }
        }
        if (!rangeType.isOneColumnFrom()) {
            if (getTableStructure().getColumn(to)!=null) {
                columns.add(getTableStructure().getColumn(to));
            }
        }
        return (IColumn[])columns.toArray(new IColumn[columns.size()]);
    }

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
