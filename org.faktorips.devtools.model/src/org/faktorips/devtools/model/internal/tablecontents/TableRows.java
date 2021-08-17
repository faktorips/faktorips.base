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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableRows extends IpsObjectPart implements ITableRows {

    private List<Row> rows = new ArrayList<>(100);

    private UniqueKeyValidator uniqueKeyValidator;

    private MessageList lastUniqueKeyValidationResult;

    public TableRows(TableContents parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public IRow[] getRows() {
        return rows.toArray(new IRow[0]);
    }

    protected List<Row> getRowsAsList() {
        return Collections.unmodifiableList(rows);
    }

    @Override
    public IRow getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumOfRows()) {
            return null;
        }
        return rows.get(rowIndex);
    }

    @Override
    public int getNumOfRows() {
        return rows.size();
    }

    @Override
    public IRow newRow() {
        IRow newRow = newRowInternal(getNextPartId());
        objectHasChanged();
        return newRow;
    }

    @Override
    public Row newRow(ITableStructure tableStructure, Optional<String> id, List<String> columns) {
        Row newRow = newRowInternal(id.orElseGet(this::getNextPartId));
        for (int i = 0; i < columns.size(); i++) {
            newRow.setValueInternal(i, columns.get(i));
        }
        if (tableStructure != null) {
            updateUniqueKeyCacheFor(newRow, tableStructure.getUniqueKeys());
        }

        return newRow;
    }

    /**
     * Public variant of {@link #objectHasChanged()} for mass operations like table import that use
     * {@link #newRow(ITableStructure, Optional, List)}.
     */
    public void markAsChanged() {
        objectHasChanged();
    }

    protected ITableStructure findTableStructure() {
        try {
            return getTableContents().findTableStructure(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Creates a new row and inserts it into the list of rows. The row number of the new row is its
     * index in the list (respectively the number of rows before the insertion).
     */
    private Row newRowInternal(String id) {
        int nextRowNumber = getNumOfRows();
        Row newRow = new Row(this, id);
        rows.add(newRow);
        newRow.setRowNumber(nextRowNumber);
        return newRow;
    }

    public void newColumn(int insertAt, String defaultValue) {
        for (Row row : rows) {
            row.newColumn(insertAt, defaultValue);
        }
        clearUniqueKeyValidator();
    }

    public void removeColumn(int column) {
        for (Row row : rows) {
            row.removeColumn(column);
        }
        clearUniqueKeyValidator();
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return getRows();
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (Row.TAG_NAME.equals(xmlTagName)) {
            return newRowInternal(id);
        }
        return null;
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.equals(IRow.class)) {
            return newRowInternal(getNextPartId());
        }
        return null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IRow) {
            rows.add((Row)part);
            return true;
        }
        return false;
    }

    /**
     * Removes the given row from the list of rows and updates the row numbers of all following
     * rows.
     */
    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IRow) {
            Row row = (Row)part;
            int delIndex = rows.indexOf(row);
            if (delIndex != -1) {
                rows.remove(delIndex);
                // update rownumbers after delete
                for (int i = delIndex; i < rows.size(); i++) {
                    Row updateRow = rows.get(i);
                    updateRow.setRowNumber(i);
                }
                ITableStructure tableStructure = findTableStructure();
                if (tableStructure != null) {
                    removeUniqueKeyCacheFor(row, tableStructure.getUniqueKeys());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        rows.clear();
    }

    @Override
    public void clear() {
        rows.clear();
        clearUniqueKeyValidator();
        objectHasChanged();
    }

    public ITableContents getTableContents() {
        return (ITableContents)getParent();
    }

    @Override
    public IRow insertRowAfter(int rowIndex) {
        int newRowIndex = (rowIndex + 1) > getNumOfRows() ? getNumOfRows() : rowIndex + 1;
        Row newRow = new Row(this, getNextPartId());
        rows.add(newRowIndex, newRow);
        newRow.setRowNumber(newRowIndex);
        refreshRowNumbers();
        objectHasChanged();
        return newRow;
    }

    /**
     * Refreshs the row number of all rows.
     */
    private void refreshRowNumbers() {
        int index = 0;
        for (Row row : rows) {
            row.setRowNumber(index++);
        }
    }

    //
    // Methods to return the xml representation constants for the extension properties
    // The constants are necessary because the TableContents supports the initialisation via SAX.
    // And the corresponding SAX handler uses this constants for node identification.
    //
    static String getXmlExtPropertiesElementName() {
        return XML_EXT_PROPERTIES_ELEMENT;
    }

    static String getXmlValueElement() {
        return XML_VALUE_ELEMENT;
    }

    static String getXmlAttributeExtpropertyid() {
        return XML_ATTRIBUTE_EXTPROPERTYID;
    }

    static String getXmlAttributeIsnull() {
        return XML_ATTRIBUTE_ISNULL;
    }

    /**
     * Updates the whole unique key cache. The unique key(s) of all rows will be updated.
     */
    private void updateUniqueKeyCache(ITableStructure tableStructure) {
        uniqueKeyValidator.clearUniqueKeyCache();
        IIndex[] uniqueKeys = tableStructure.getUniqueKeys();
        for (IRow row : rows) {
            updateUniqueKeyCacheFor((Row)row, uniqueKeys);
        }
        uniqueKeyValidator.cacheTableStructureAndValueDatatypes(this);
        // store the last table structure modification time to detect changes of the table structure
        uniqueKeyValidator.setTableStructureModificationTimeStamp(tableStructure);
    }

    /**
     * Returns <code>true</code> if there was an unique key error state change, e.g. current state
     * is error previous state was error free
     */
    public boolean wasUniqueKeyErrorStateChange() {
        return uniqueKeyValidator.wasErrorStateChange();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ITableStructure tableStructure = getTableContents().findTableStructure(ipsProject);
        ValueDatatype[] datatypes = ((TableContents)getTableContents()).findColumnDatatypesByReferences(tableStructure,
                ipsProject);
        if (tableStructure != null && datatypes != null && tableStructure.getUniqueKeys().length > 0) {
            MessageList validationMessageList = new MessageList();

            validateUniqueKeys(validationMessageList, tableStructure, datatypes);
            list.add(validationMessageList.getMessageByCode(ITableContents.MSGCODE_TOO_MANY_UNIQUE_KEY_VIOLATIONS));
        }
    }

    /**
     * Validates the unique keys of all rows.
     */
    void validateUniqueKeys(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        validateUniqueKeys(list, tableStructure, datatypes, isUniqueKeyValidatedAutomatically(tableStructure));
    }

    /**
     * Returns whether the unique key validation is run automatically every time the table rows are
     * validated. If the table structure cannot be found or has a lot of defined ranges and/or the
     * table contents are very large, this will be false. Validation can then be triggered manually
     * via {@link #validateUniqueKeysManually()} if table structure is present.
     */
    public boolean isUniqueKeyValidatedAutomatically() {
        ITableStructure tableStructure = findTableStructure();
        return tableStructure != null && isUniqueKeyValidatedAutomatically(tableStructure);
    }

    private boolean isUniqueKeyValidatedAutomatically(ITableStructure tableStructure) {
        // 5000 seems to be a good threshold currently, as sample contents with 11 keys and < 450
        // lines validate in less than 2 seconds on my machine. Might be necessary to increase with
        // growing performance or allow setting by customers to fit their hardware...
        return tableStructure == null || tableStructure.getRanges().length * rows.size() <= 5000;
    }

    private void validateUniqueKeys(MessageList list,
            ITableStructure tableStructure,
            ValueDatatype[] datatypes,
            boolean validate) {
        if (isUniqueKeyValidationEnabled()) {
            if (validate) {
                // check if the unique key cache needs to be updated
                if (uniqueKeyValidator.isEmpty() || uniqueKeyValidator.isInvalidUniqueKeyCache(tableStructure)) {
                    // could be happen if a new column was added to the table generation
                    // or the structure has changed, e.g. a new unique key was added
                    updateUniqueKeyCache(tableStructure);
                }
                uniqueKeyValidator.validateAllUniqueKeys(list, tableStructure, datatypes);
            } else {
                list.add(lastUniqueKeyValidationResult);
            }
        }
    }

    /**
     * Runs the unique key validation, even if it is disabled by
     * {@link #isUniqueKeyValidatedAutomatically()}. Caches the validation result to return it with
     * future validations until {@link #validateUniqueKeysManually()} is run again.
     */
    public void validateUniqueKeysManually() {
        ITableStructure tableStructure = findTableStructure();
        if (tableStructure != null) {
            lastUniqueKeyValidationResult = new MessageList();
            IIpsProject ipsProject = getTableContents().getIpsProject();
            ValueDatatype[] datatypes = ((TableContents)getTableContents())
                    .findColumnDatatypesByReferences(tableStructure, ipsProject);
            validateUniqueKeys(lastUniqueKeyValidationResult, tableStructure, datatypes, true);
            boolean dirty = getIpsSrcFile().isDirty();
            objectHasChanged();
            if (!dirty) {
                getIpsSrcFile().markAsClean();
            }
        }
    }

    /**
     * returns <code>true</code> if the unique key validation is enabled
     */
    public boolean isUniqueKeyValidationEnabled() {
        return findTableStructure() != null && uniqueKeyValidator != null;
    }

    /**
     * Clears the unique key cache, e.g. if a column was added or removed or the table contents
     * generation will cleared
     */
    public void clearUniqueKeyValidator() {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.clearUniqueKeyCache();
        }
    }

    /**
     * Updates the unique key cache for the given row
     *
     */
    void updateUniqueKeyCacheFor(Row row, IIndex[] uniqueKeys) {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.handleRowChanged(this, row, uniqueKeys);
        }
    }

    /**
     * Removes the row in the cached unique keys
     */
    private void removeUniqueKeyCacheFor(Row row, IIndex[] uniqueKeys) {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.handleRowRemoved(this, row, uniqueKeys);
        }
    }

    /**
     * Initializes the unique key cache.
     */
    public void initUniqueKeyValidator(ITableStructure tableStructure, UniqueKeyValidator uniqueKeyValidator) {
        this.uniqueKeyValidator = uniqueKeyValidator;
        if (tableStructure != null && uniqueKeyValidator != null) {
            updateUniqueKeyCache(tableStructure);
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.removeAttribute(IpsObjectPart.PROPERTY_ID);
        if (usesCsv()) {
            element.setAttribute(PROPERTY_FORMAT, FORMAT_CSV);
        }
    }

    private boolean usesCsv() {
        return FORMAT_CSV.equals(getIpsProject().getReadOnlyProperties().getTableContentFormat().getId());
    }

    @Override
    protected void partsToXml(Document doc, Element element) {
        if (usesCsv()) {
            new TableRowsCsvHelper(this).partsToCsv(doc, element);
        } else {
            super.partsToXml(doc, element);
        }
    }

    @Override
    public void initFromXml(Element element) {
        super.initFromXml(element);
        if (element.hasAttribute(PROPERTY_FORMAT)
                && FORMAT_CSV.equals(element.getAttribute(PROPERTY_FORMAT))) {
            initFromCsv(element.getTextContent());
        }
    }

    public void initFromCsv(String text) {
        new TableRowsCsvHelper(this).initFromCsv(text);
    }

}
