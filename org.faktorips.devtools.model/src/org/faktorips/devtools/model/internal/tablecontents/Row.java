/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.util.ListElementMover;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Row extends AtomicIpsObjectPart implements IRow {

    static final String TAG_NAME = "Row"; //$NON-NLS-1$
    static final String VALUE_TAG_NAME = "Value"; //$NON-NLS-1$

    private ArrayList<String> values;

    private int rowNumber = 0;

    Row(TableRows parent, String id) {
        super(parent, id);
        initValues();
    }

    @Override
    public ITableContents getTableContents() {
        return (ITableContents)getParent().getParent();
    }

    /**
     * Returns the table contents generation this row belongs to
     */
    public TableRows getTableRows() {
        return (TableRows)getParent();
    }

    private int getNumOfColumnsViaTableContents() {
        return ((ITableContents)getParent().getParent()).getNumOfColumns();
    }

    private void initValues() {
        values = new ArrayList<>(Arrays.asList(new String[getNumOfColumnsViaTableContents()]));
    }

    @Override
    public String getName() {
        return "" + (rowNumber + 1); //$NON-NLS-1$
    }

    @Override
    public String getCaption(Locale locale) {
        return MessageFormat.format(Messages.Row_caption, rowNumber + 1);
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Sets the row number of this row. To keep row numbers up to date the tableContents object
     * calls this method every time the list of rows changes.
     */
    void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    @Override
    public String getValue(int column) {
        return values.get(column);
    }

    @Override
    public void setValue(int column, String newValue) {
        setValueInternal(column, newValue);
        ITableStructure tableStructure = findTableStructure();
        if (tableStructure != null) {
            getTableRows().updateUniqueKeyCacheFor(this, tableStructure.getUniqueKeys());
        }
        objectHasChanged();
    }

    @Override
    public int moveValue(int columnIndex, boolean up) {
        ArgumentCheck.notNull(values);
        int index = columnIndex;
        // Return if element is already the first / last one.
        if (up) {
            if (index == 0) {
                return index;
            }
        } else {
            if (index == values.size() - 1) {
                return index;
            }
        }

        // Perform the moving.

        int[] newIndex = moveParts(new int[] { index }, up);

        return newIndex[0];

    }

    @Override
    public void swapValue(int firstColumnIndex, int secondColumnIndex) {
        if (firstColumnIndex == secondColumnIndex || firstColumnIndex < 0 || secondColumnIndex < 0) {
            throw new IllegalArgumentException();
        }
        int positionOfFirstAttribute = firstColumnIndex;
        int positionOfSecondAttribute = secondColumnIndex;
        // firstIndex is always the higher index. If not swap both indexes
        if (firstColumnIndex < secondColumnIndex) {
            positionOfFirstAttribute = secondColumnIndex;
            positionOfSecondAttribute = firstColumnIndex;
        }
        // move the first Value to the desired position
        int currentIndex = positionOfFirstAttribute;
        while (currentIndex != positionOfSecondAttribute) {
            currentIndex = moveValue(currentIndex, true);
        }
        // move the second Value to the desired position
        currentIndex = positionOfSecondAttribute + 1;
        while (currentIndex != positionOfFirstAttribute) {
            currentIndex = moveValue(currentIndex, false);
        }
    }

    private int[] moveParts(int[] indeces, boolean up) {
        ListElementMover<String> mover = new ListElementMover<>(values);
        return mover.move(indeces, up);
    }

    protected ITableStructure findTableStructure() {
        return getTableContents().findTableStructure(getIpsProject());
    }

    public void setValueInternal(int column, String newValue) {
        values.set(column, newValue);
    }

    @Override
    public void newColumn(int insertAfter, String defaultValue) {
        if (insertAfter < values.size()) {
            values.add(Math.max(0, insertAfter), defaultValue);
        } else {
            values.add(defaultValue);
        }
        objectHasChanged();
    }

    void removeColumn(int column) {
        values.remove(column);
    }

    /**
     * Validates the values in this row against unique-keys and datatypes defined by the
     * TableStructure of this row's TableContents.
     * <p>
     * For every unique key the TableStructure defines all columns that are part of the unique key
     * are processed. If a column of this row does not contain a value as dictated by unique keys, a
     * new <code>ERROR</code>-<code>Message</code> is added to the given <code>MessageList</code>.
     * <p>
     * The datatype for every column is retrieved and the corresponding value is tested. If the
     * value does not match the datatype (is not parsable) a new <code>ERROR</code>-
     * <code>Message</code> is added to the given <code>MessageList</code>. {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        ITableStructure tableStructure = findTableStructure();
        if (tableStructure == null) {
            return;
        }
        ValueDatatype[] datatypes = ((TableContents)getTableContents()).findColumnDatatypesByReferences(tableStructure,
                ipsProject);
        validateThis(list, tableStructure, datatypes);
    }

    private void validateThis(MessageList result, ITableStructure tableStructure, ValueDatatype[] datatypes) {

        List<IIndex> indices = tableStructure.getIndices();
        validateMissingAndInvalidIndexValue(result, datatypes, tableStructure, indices);
        validateRowValue(result, tableStructure, datatypes);
        if (!result.containsErrorMsg()) {
            validateUniqueKey(result, tableStructure, datatypes);
        }
    }

    private void validateUniqueKey(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        MessageList validationMessageList = new MessageList();
        if (tableStructure.getUniqueKeys().length > 0) {
            getTableRows().validateUniqueKeys(validationMessageList, tableStructure, datatypes);
        }
        list.add(validationMessageList.getMessagesFor(this));
    }

    /**
     * Validates this row using the given list of datatypes.
     */
    private void validateMissingAndInvalidIndexValue(MessageList list,
            ValueDatatype[] datatypes,
            ITableStructure structure,
            List<IIndex> indices) {

        /*
         * this validation can only be applied if the column sizes of the structure and content are
         * consistent. there must be a different rule that validates this consistency
         */
        if (structure.getNumOfColumns() != getTableContents().getNumOfColumns()) {
            return;
        }
        for (IIndex indexKey : indices) {
            IKeyItem[] keyItems = indexKey.getKeyItems();
            for (IKeyItem keyItem : keyItems) {
                if (keyItem instanceof IColumn) {
                    IColumn column = (IColumn)keyItem;
                    validateUniqueKeyValue(list, structure, column.getName());
                } else if (keyItem instanceof IColumnRange) {
                    IColumnRange columnRange = (IColumnRange)keyItem;
                    if (columnRange.getColumnRangeType().isOneColumnFrom()) {
                        validateUniqueKeyValue(list, structure, columnRange.getFromColumn());
                    } else if (columnRange.getColumnRangeType().isOneColumnTo()) {
                        validateUniqueKeyValue(list, structure, columnRange.getToColumn());
                    } else if (columnRange.getColumnRangeType().isTwoColumn()) {
                        validateUniqueKeyValue(list, structure, columnRange.getFromColumn());
                        validateUniqueKeyValue(list, structure, columnRange.getToColumn());
                        validateFromAndToColumnCombination(list, datatypes, structure, columnRange.getFromColumn(),
                                columnRange.getToColumn());
                    }
                }
            }
        }
    }

    private void validateFromAndToColumnCombination(MessageList list,
            ValueDatatype[] datatypes,
            ITableStructure structure,
            String fromColumnName,
            String toColumnName) {

        try {
            int fromColumnIndex = structure.getColumnIndex(fromColumnName);
            int toColumnIndex = structure.getColumnIndex(toColumnName);
            ValueDatatype valueDatatypeFrom = datatypes[fromColumnIndex];
            ValueDatatype valueDatatypeTo = datatypes[toColumnIndex];
            if (valueDatatypeFrom == null || valueDatatypeTo == null || !valueDatatypeFrom.equals(valueDatatypeTo)) {
                // Error the 'from'-column and the 'to'-column datatypes are different!
                // ignored, will be another validation error
                return;
            }

            String valueFrom = getValue(fromColumnIndex);
            String valueTo = getValue(toColumnIndex);
            if (valueFrom == null || valueTo == null || !valueDatatypeFrom.isParsable(valueFrom)
                    || !valueDatatypeTo.isParsable(valueTo)) {
                // ignored, will be another validation error
                return;
            }
            if (valueDatatypeFrom.compare(getValue(fromColumnIndex), valueTo) > 0) {
                IColumn fromColumn = structure.getColumn(fromColumnIndex);
                IColumn toColumn = structure.getColumn(toColumnIndex);
                String localizedFromLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(fromColumn);
                String localizedToLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(toColumn);
                String text = MessageFormat.format(Messages.Row_FromValueGreaterThanToValue,
                        localizedFromLabel + '-' + localizedToLabel);
                list.add(new Message(MSGCODE_UNIQUE_KEY_FROM_COLUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE, text,
                        Message.ERROR, new ObjectProperty(this, IRow.PROPERTY_VALUE, fromColumnIndex)));
                list.add(new Message(MSGCODE_UNIQUE_KEY_FROM_COLUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE, text,
                        Message.ERROR, new ObjectProperty(this, IRow.PROPERTY_VALUE, toColumnIndex)));
            }
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            // ignored, will be another validation error
        }
        // CSON: IllegalCatch
    }

    private void validateUniqueKeyValue(MessageList list, ITableStructure structure, String columnName) {
        try {
            int columnIndex = structure.getColumnIndex(columnName);
            String localizedLabel = IIpsModel.get().getMultiLanguageSupport()
                    .getLocalizedLabel(structure.getColumn(columnIndex));
            String value = getValue(columnIndex);
            if (value != null && StringUtils.isEmpty(value.trim()) || value == null) {
                String text = MessageFormat.format(Messages.Row_MissingValueForUniqueKey, localizedLabel);
                Message message = new Message(MSGCODE_UNDEFINED_UNIQUEKEY_VALUE, text, Message.ERROR,
                        new ObjectProperty(this, IRow.PROPERTY_VALUE, columnIndex));
                list.add(message);
            }
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            // ignored, can't validate key value because of missing column
            // will be another validation error
        }
        // CSON: IllegalCatch
    }

    private void validateRowValue(MessageList list, ITableStructure structure, ValueDatatype[] datatypes) {
        int numOfColumnsInStructure = structure.getNumOfColumns();
        for (int i = 0; i < getNumOfColumnsViaTableContents(); i++) {
            if (i >= numOfColumnsInStructure) {
                // datatypes couldn't be checked because structure contains no more columns
                return;
            }
            IColumn column = structure.getColumn(i);

            ValueDatatype dataType = datatypes[i];
            String value = getValue(i);
            if (dataType == null || !dataType.isParsable(value)) {
                String localizedLabel = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(column);
                String datatypeName = dataType == null ? column.getDatatype() : dataType.toString();
                String text = MessageFormat.format(Messages.Row_ValueNotParsable,
                        value, datatypeName, localizedLabel);
                Message message = new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR,
                        new ObjectProperty(this, IRow.PROPERTY_VALUE, i));
                list.add(message);
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        NodeList nl = element.getElementsByTagName(VALUE_TAG_NAME);
        initValues();
        for (int i = 0; i < values.size(); i++) {
            if (i < nl.getLength()) {
                Element valueElement = (Element)nl.item(i);
                String content = ValueToXmlHelper.getValueFromElement(valueElement);
                values.set(i, content);
            }
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.removeAttribute(IpsObjectPart.PROPERTY_ID);
        for (String value : values) {
            ValueToXmlHelper.addValueToElement(value, element, VALUE_TAG_NAME);
        }
    }

    /**
     * Returns the number of columns in this row.
     */
    public int getNoOfColumns() {
        return values.size();
    }
}
