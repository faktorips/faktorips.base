/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaConventions;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ColumnRange extends AtomicIpsObjectPart implements IColumnRange {

    static final String TAG_NAME = "Range"; //$NON-NLS-1$

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
    public boolean isRange() {
        return true;
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        if (IpsStringUtils.isEmpty(parameterName)) {
            String text = Messages.ColumnRange_msgParameterEmpty;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PARAMETER_NAME)); //$NON-NLS-1$
        } else if (!PlainJavaConventions.validateName(parameterName)) {
            // FS #1415
            String text = Messages.ColumnRange_msgNameInvalidJavaIdentifier;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PARAMETER_NAME)); //$NON-NLS-1$
        }

        String fromColumnDatatype = null;
        String toColumnDatatype = null;

        if (getTableStructureImpl().getColumn(from) != null) {
            fromColumnDatatype = getTableStructure().getColumn(from).getDatatype();
        }

        if (getTableStructureImpl().getColumn(to) != null) {
            toColumnDatatype = getTableStructure().getColumn(to).getDatatype();
        }

        if ((rangeType.isTwoColumn() || rangeType.isOneColumnFrom())) {
            validateColumn(from, PROPERTY_FROM_COLUMN, "from column", list); //$NON-NLS-1$
        }

        if ((rangeType.isTwoColumn() || rangeType.isOneColumnTo())) {
            validateColumn(to, PROPERTY_TO_COLUMN, "to column", list); //$NON-NLS-1$
        }

        validateTwoColumnSameDatatype(list, fromColumnDatatype, toColumnDatatype);
    }

    protected void validateColumn(String column, String propertyName, String propertyDisplayName, MessageList list) {
        if (ValidationUtils.checkStringPropertyNotEmpty(column, propertyDisplayName, this, propertyName, "", list)) { //$NON-NLS-1$
            if (getTableStructure().getColumn(column) == null) {
                String text = MessageFormat.format(Messages.ColumnRange_msgMissingColumn, column);
                list.add(new Message("", text, Message.ERROR, this, propertyName)); //$NON-NLS-1$
            }
        }
    }

    protected void validateTwoColumnSameDatatype(MessageList list, String fromColumnDatatype, String toColumnDatatype) {
        if (rangeType.isTwoColumn() && toColumnDatatype != null && fromColumnDatatype != null) {
            if (!toColumnDatatype.equals(fromColumnDatatype)) {
                String text = MessageFormat
                        .format(Messages.ColumnRange_msgTwoColumnRangeFromToColumnWithDifferentDatatype, to);
                list.add(new Message(IColumnRange.MSGCODE_TWO_COLUMN_RANGE_FROM_TO_COLUMN_WITH_DIFFERENT_DATATYPE,
                        text, Message.ERROR, this));
            }
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
        if (IpsStringUtils.isNotBlank(from)) {
            element.setAttribute(PROPERTY_FROM_COLUMN, from);
        }
        if (IpsStringUtils.isNotBlank(to)) {
            element.setAttribute(PROPERTY_TO_COLUMN, to);
        }
        element.setAttribute(PROPERTY_PARAMETER_NAME, parameterName);
    }

    @Override
    public IColumn[] getColumns() {
        List<IColumn> columns = new ArrayList<>();
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
