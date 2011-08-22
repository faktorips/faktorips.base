/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Row extends AtomicIpsObjectPart implements IRow {

    final static String TAG_NAME = "Row"; //$NON-NLS-1$
    final static String VALUE_TAG_NAME = "Value"; //$NON-NLS-1$

    private ArrayList<String> values;

    private int rowNumber = 0;

    Row(TableContentsGeneration parent, String id) {
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
    public TableContentsGeneration getTableContentsGeneration() {
        return (TableContentsGeneration)getParent();
    }

    private int getNumOfColumnsViaTableContents() {
        return ((ITableContents)getParent().getParent()).getNumOfColumns();
    }

    private void initValues() {
        initValues(getNumOfColumnsViaTableContents());
    }

    /**
     * Initializes the row's values with blanks
     */
    private void initValues(int numOfColumns) {
        values = new ArrayList<String>(numOfColumns + 5);
        for (int i = 0; i < numOfColumns; i++) {
            values.add(null);
        }
    }

    @Override
    public String getName() {
        return "" + rowNumber; //$NON-NLS-1$
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
        getTableContentsGeneration().updateUniqueKeyCacheFor(this);
        objectHasChanged();
    }

    public void setValueInternal(int column, String newValue) {
        values.set(column, newValue);
    }

    void newColumn(int insertAfter, String defaultValue) {
        if (insertAfter < values.size()) {
            values.add(Math.max(0, insertAfter), defaultValue);
        } else {
            values.add(defaultValue);
        }
        objectHasChanged();
    }

    void removeColumn(int column) {
        column = Math.max(0, column);
        column = Math.min(values.size(), column);
        values.remove(column);
    }

    /**
     * Returns the element's first text node.
     */
    private Text getTextNode(Element valueElement) {
        NodeList nl = valueElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Text) {
                return (Text)nl.item(i);
            }
        }
        return null;
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ITableStructure tableStructure = ((TableContents)getTableContents()).findTableStructure(ipsProject);
        if (tableStructure == null) {
            return;
        }
        ValueDatatype[] datatypes = ((TableContents)getTableContents()).findColumnDatatypes(tableStructure, ipsProject);
        validateThis(list, tableStructure, datatypes, true);
    }

    MessageList validateThis(ITableStructure tableStructure, ValueDatatype[] datatypes, IIpsProject ipsProject)
            throws CoreException {
        MessageList result = beforeValidateThis();
        if (result != null) {
            return result;
        }

        result = new MessageList();
        // method was invoked by the table contents generation
        // the table contents generation validates the unique key separately
        validateThis(result, tableStructure, datatypes, false);

        afterValidateThis(result, ipsProject);

        return result;
    }

    private void validateThis(MessageList result,
            ITableStructure tableStructure,
            ValueDatatype[] datatypes,
            boolean uniqueKeyCheck) {

        IUniqueKey[] uniqueKeys = tableStructure.getUniqueKeys();
        validateMissingAndInvalidUniqueKeyValue(result, datatypes, tableStructure, uniqueKeys);
        validateRowValue(result, tableStructure, datatypes);
        if (uniqueKeyCheck) {
            // validateUniqueKey(result, tableStructure, datatypes);
        }
    }

    private void validateUniqueKey(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        getTableContentsGeneration().validateUniqueKeys(list, tableStructure, datatypes);
    }

    /**
     * Validates this row using the given list of datatypes.
     */
    private void validateMissingAndInvalidUniqueKeyValue(MessageList list,
            ValueDatatype[] datatypes,
            ITableStructure structure,
            IUniqueKey[] uniqueKeys) {

        /*
         * this validation can only be applied if the colum sizes of the structure and content are
         * consistent. there must be a different rule that validates this consistency
         */
        if (structure.getNumOfColumns() != getTableContents().getNumOfColumns()) {
            return;
        }
        for (IUniqueKey uniqueKey : uniqueKeys) {
            IKeyItem[] keyItems = uniqueKey.getKeyItems();
            for (IKeyItem keyItem : keyItems) {
                if (keyItem instanceof IColumn) {
                    IColumn column = (IColumn)keyItem;
                    validateUniqueKeyValue(list, structure, column);
                } else if (keyItem instanceof IColumnRange) {
                    IColumnRange columnRange = (IColumnRange)keyItem;
                    IColumn fromColumn = structure.getColumn(columnRange.getFromColumn());
                    IColumn toColumn = structure.getColumn(columnRange.getToColumn());
                    if (columnRange.getColumnRangeType().isOneColumnFrom()) {
                        validateUniqueKeyValue(list, structure, fromColumn);
                    } else if (columnRange.getColumnRangeType().isOneColumnTo()) {
                        validateUniqueKeyValue(list, structure, toColumn);
                    } else if (columnRange.getColumnRangeType().isTwoColumn()) {
                        validateUniqueKeyValue(list, structure, fromColumn);
                        validateUniqueKeyValue(list, structure, toColumn);
                        validateFromAndToColumnCombination(list, datatypes, structure, columnRange, fromColumn,
                                toColumn);
                    }
                }
            }
        }
    }

    private void validateFromAndToColumnCombination(MessageList list,
            ValueDatatype[] datatypes,
            ITableStructure structure,
            IColumnRange columnRange,
            IColumn fromColumn,
            IColumn toColumn) {

        if (fromColumn == null || toColumn == null) {
            // ignored, will be another validation error
            return;
        }
        int fromColumnIndex = structure.getColumnIndex(fromColumn);
        int toColumnIndex = structure.getColumnIndex(toColumn);
        ValueDatatype valueDatatypeFrom = datatypes[fromColumnIndex];
        ValueDatatype valueDatatypeTo = datatypes[toColumnIndex];
        if (valueDatatypeFrom == null || valueDatatypeTo == null) {
            // Error 'from'-column or the 'to'-column datatype is null!
            // ignored, will be another validation error
            return;
        }
        if (!valueDatatypeFrom.equals(valueDatatypeTo)) {
            // Error the 'from'-column and the 'to'-column datatypes are different!
            // ignored, will be another validation error
            return;
        }

        String valueFrom = getValue(fromColumnIndex);
        String valueTo = getValue(toColumnIndex);
        if (valueFrom == null || valueTo == null) {
            // ignored, will be another validation error
            return;
        }

        if (!valueDatatypeFrom.isParsable(valueFrom) || !valueDatatypeTo.isParsable(valueTo)) {
            // ignored, will be another validation error
            return;
        }
        if (valueDatatypeFrom.compare(getValue(fromColumnIndex), valueTo) > 0) {
            String text = NLS.bind(Messages.Row_FromValueGreaterThanToValue, columnRange.getName());
            list.add(new Message(MSGCODE_UNIQUE_KEY_FROM_COlUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE, text, Message.ERROR,
                    new ObjectProperty(this, IRow.PROPERTY_VALUE, fromColumnIndex)));
            list.add(new Message(MSGCODE_UNIQUE_KEY_FROM_COlUMN_VALUE_IS_GREATER_TO_COLUMN_VALUE, text, Message.ERROR,
                    new ObjectProperty(this, IRow.PROPERTY_VALUE, toColumnIndex)));
        }
    }

    private void validateUniqueKeyValue(MessageList list, ITableStructure structure, IColumn column) {
        if (column == null) {
            // ignored, can't validate key value because of missing column
            // will be another validation error
            return;
        }
        int columnIndex = structure.getColumnIndex(column);
        String value = getValue(columnIndex);
        if (value != null && StringUtils.isEmpty(value.trim()) || value == null) {
            String text = NLS.bind(Messages.Row_MissingValueForUniqueKey, column.getName());
            Message message = new Message(MSGCODE_UNDEFINED_UNIQUEKEY_VALUE, text, Message.ERROR, new ObjectProperty(
                    this, IRow.PROPERTY_VALUE, columnIndex));
            list.add(message);
        }
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
                String text = NLS.bind(Messages.Row_ValueNotParsable,
                        new Object[] { value, dataType, column.getName() });
                Message message = new Message(MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, new ObjectProperty(this,
                        IRow.PROPERTY_VALUE, i));
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
                String isNull = valueElement.getAttribute("isNull"); //$NON-NLS-1$
                if (Boolean.valueOf(isNull).booleanValue()) {
                    values.set(i, null);
                } else {
                    Text textNode = getTextNode(valueElement);
                    if (textNode == null) {
                        values.set(i, ""); //$NON-NLS-1$
                    } else {
                        values.set(i, textNode.getNodeValue());
                    }
                }
            }
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        Document doc = element.getOwnerDocument();
        for (int i = 0; i < values.size(); i++) {
            Element valueElement = doc.createElement(VALUE_TAG_NAME);
            valueElement.setAttribute("isNull", values.get(i) == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (values.get(i) != null) {
                valueElement.appendChild(doc.createTextNode(values.get(i)));
            }
            element.appendChild(valueElement);
        }
    }

    /**
     * Returns the number of columns in this row.
     */
    public int getNoOfColumns() {
        return values.size();
    }

}
