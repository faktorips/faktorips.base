/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ColumnRange extends AtomicIpsObjectPart implements IColumnRange {

    final static String TAG_NAME = "Range"; //$NON-NLS-1$

    private String from = ""; //$NON-NLS-1$
    private String to = ""; //$NON-NLS-1$
    private ColumnRangeType rangeType = ColumnRangeType.TWO_COLUMN_RANGE;
    private String parameterName = ""; //$NON-NLS-1$

    public ColumnRange(TableStructure parent, String id) {
        super(parent, id);
    }

    TableStructure getTableStructureImpl() {
        return (TableStructure)getParent();
    }

    @Override
    public ITableStructure getTableStructure() {
        return (ITableStructure)getParent();
    }

    @Override
    public String getName() {
        return from + '-' + to;
    }

    @Override
    public String getAccessParameterName() {
        return getName();
    }

    @Override
    public String getFromColumn() {
        return from;
    }

    @Override
    public void setFromColumn(String columnName) {
        ArgumentCheck.notNull(columnName);
        String oldColumnName = from;
        from = columnName;
        valueChanged(oldColumnName, from);
    }

    @Override
    public String getToColumn() {
        return to;
    }

    @Override
    public void setToColumn(String columnName) {
        ArgumentCheck.notNull(columnName);
        String oldColumnName = to;
        to = columnName;
        valueChanged(oldColumnName, to);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        String fromColumnDatatype = null;
        String toColumnDatatype = null;

        if (!StringUtils.isEmpty(from) && getTableStructureImpl().getColumn(from) != null) {
            fromColumnDatatype = getTableStructure().getColumn(from).getDatatype();
            if (fromColumnDatatype.equals(Datatype.BOOLEAN.getName())
                    || fromColumnDatatype.equals(Datatype.PRIMITIVE_BOOLEAN.getName())) {
                String msg = NLS.bind(Messages.ColumnRange_msgDatatypeInvalidForRange, fromColumnDatatype);
                list
                        .add(new Message(MSGCODE_INVALID_DATATYPE_FOR_FROM, msg, Message.ERROR, this,
                                PROPERTY_FROM_COLUMN));
            }
        }

        if (!StringUtils.isEmpty(to) && getTableStructureImpl().getColumn(to) != null) {
            toColumnDatatype = getTableStructure().getColumn(to).getDatatype();
            if (toColumnDatatype.equals(Datatype.BOOLEAN.getName())
                    || toColumnDatatype.equals(Datatype.PRIMITIVE_BOOLEAN.getName())) {
                String msg = NLS.bind(Messages.ColumnRange_msgDatatypeInvalidForRange, toColumnDatatype);
                list.add(new Message(MSGCODE_INVALID_DATATYPE_FOR_TO, msg, Message.ERROR, this, PROPERTY_TO_COLUMN));
            }
        }

        if ((rangeType.isTwoColumn() || rangeType.isOneColumnFrom())
                && ValidationUtils.checkStringPropertyNotEmpty(from,
                        "from column", this, PROPERTY_FROM_COLUMN, "", list)) { //$NON-NLS-1$ //$NON-NLS-2$
            if (getTableStructure().getColumn(from) == null) {
                String text = NLS.bind(Messages.ColumnRange_msgMissingColumn, from);
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_FROM_COLUMN)); //$NON-NLS-1$
            }
        }

        if ((rangeType.isTwoColumn() || rangeType.isOneColumnTo())
                && ValidationUtils.checkStringPropertyNotEmpty(to, "to column", this, PROPERTY_TO_COLUMN, "", list)) { //$NON-NLS-1$ //$NON-NLS-2$
            if (getTableStructure().getColumn(to) == null) {
                String text = NLS.bind(Messages.ColumnRange_msgMissingColumn, to);
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_TO_COLUMN)); //$NON-NLS-1$
            }
        }

        if (rangeType.isTwoColumn() && toColumnDatatype != null && fromColumnDatatype != null) {
            if (!toColumnDatatype.equals(fromColumnDatatype)) {
                String text = NLS.bind(Messages.ColumnRange_msgTwoColumnRangeFromToColumnWithDifferentDatatype, to);
                list.add(new Message(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE,
                        text, Message.ERROR, this));
            }
        }

        if (StringUtils.isEmpty(parameterName)) {
            String text = Messages.ColumnRange_msgParameterEmpty;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PARAMETER_NAME)); //$NON-NLS-1$
        } else if (!JavaConventions.validateIdentifier(parameterName, "1.5", "1.5").isOK()) { //$NON-NLS-1$ //$NON-NLS-2$
            // FS #1415
            String text = Messages.ColumnRange_msgNameInvalidJavaIdentifier;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PARAMETER_NAME)); //$NON-NLS-1$
        }
    }

    @Override
    public void setColumnRangeType(ColumnRangeType rangeType) {
        ArgumentCheck.notNull(rangeType);
        Object oldValue = this.rangeType;
        this.rangeType = rangeType;
        valueChanged(oldValue, rangeType);
    }

    @Override
    public ColumnRangeType getColumnRangeType() {
        return rangeType;
    }

    @Override
    public String getDatatype() {
        if (rangeType.isTwoColumn() || rangeType.isOneColumnFrom()) {
            return getDatatype(getFromColumn());
        }
        return getDatatype(getToColumn());
    }

    private String getDatatype(String columnName) {
        IColumn column = getTableStructure().getColumn(columnName);
        if (column != null) {
            return column.getDatatype();
        }
        return null;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        ArgumentCheck.notNull(parameterName);
        String oldParameterName = this.parameterName;
        this.parameterName = parameterName;
        valueChanged(oldParameterName, parameterName);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        rangeType = ColumnRangeType.getValueById(element.getAttribute(PROPERTY_RANGE_TYPE));
        from = element.getAttribute(PROPERTY_FROM_COLUMN);
        to = element.getAttribute(PROPERTY_TO_COLUMN);
        parameterName = element.getAttribute(PROPERTY_PARAMETER_NAME);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_RANGE_TYPE, rangeType.getId());
        element.setAttribute(PROPERTY_FROM_COLUMN, from);
        element.setAttribute(PROPERTY_TO_COLUMN, to);
        element.setAttribute(PROPERTY_PARAMETER_NAME, parameterName);
    }

    @Override
    public IColumn[] getColumns() {
        List<IColumn> columns = new ArrayList<IColumn>();
        if (!rangeType.isOneColumnTo()) {
            if (getTableStructure().getColumn(from) != null) {
                columns.add(getTableStructure().getColumn(from));
            }
        }
        if (!rangeType.isOneColumnFrom()) {
            if (getTableStructure().getColumn(to) != null) {
                columns.add(getTableStructure().getColumn(to));
            }
        }
        return columns.toArray(new IColumn[columns.size()]);
    }

}
