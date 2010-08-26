/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * Unique key validator. Validates all rows of a table contents if there are unique key violations.
 * This class caches all key values and the corresponding row for which the value was evaluated. The
 * key value is the value of an unique key for specific row. If there are more than one row with the
 * same key value then a list of rows (with the same key value) will be stored in the cache. During
 * the validation all cached key values will be checked if there are more than one row.
 * <p>
 * If an unique key contains at least one two column range key item, then a further cache will be
 * used to check if there are unique key violations inside the range(s).
 * <p>
 * The table contents (which uses this unique key validator) is responsible to call the methods
 * handleRowChanged and handleRowRemoved to keep the cache up to date. If the table structure has
 * changed (e.g. if the unique key definition changes) then the clearUniqueKeyCache method must be
 * called to remove all cached key values. Furthermore the methods isInvalidUniqueKeyCache and
 * setTableStructureModificationTimeStamp must be used to keep the the cached table structure and
 * columns ValueDatatypes up to date.
 * 
 * @author Joerg Ortmann
 */
public class UniqueKeyValidator {

    public static final int HANDLE_UNIQUEKEY_ROW_REMOVED = 1;
    public static final int HANDLE_UNIQUEKEY_ROW_CHANGED = 2;

    public final static boolean TRACE_VALIDATION_CACHE;

    static {
        TRACE_VALIDATION_CACHE = Boolean.valueOf(
                Platform.getDebugOption("org.faktorips.devtools.core/trace/tablecontentvalidation")).booleanValue(); //$NON-NLS-1$
    }

    /** cache to validate simple column unique keys */
    private Map<IUniqueKey, Map<AbstractKeyValue, Object>> uniqueKeyMapColumn = new HashMap<IUniqueKey, Map<AbstractKeyValue, Object>>();

    /**
     * caches to validate column ranges of type TWO_COLUMN_RANGE note that the unique key validation
     * of one column ranges (ONE_COLUMN_RANGE_FROM and ONE_COLUMN_RANGE_TO) are not necessary,
     * because the nature of this type of range is to return always one row this map contains a
     * special UniqueKeyValidatorRange object for each unique key with at least one two column range
     */
    private Map<IUniqueKey, UniqueKeyValidatorRange> uniqueKeyValidatorForTwoColumnRange = new HashMap<IUniqueKey, UniqueKeyValidatorRange>();

    // cached TableStructure and ValueDatatypes (cached because of performance reasons)
    // the client is responsible to keep this fields up to date using the methods
    // isInvalidUniqueKeyCache and setTableStructureModificationTimeStamp
    private long tableStrunctureModificationStamp;
    private ITableStructure cachedTableStructure;
    private ValueDatatype[] cachedValueDatatypes;

    // contains the previous error status
    // used to indicate an error status change
    private boolean currentErrorStatus;
    private boolean previousErrorStatus;

    /**
     * Returns the cached table structure
     */
    ITableStructure getCachedTableStructure() {
        return cachedTableStructure;
    }

    /**
     * Returns the cached value datatypes of all columns
     */
    ValueDatatype[] getCachedValueDatatypes() {
        return cachedValueDatatypes;
    }

    /**
     * Clear all cached unique key values.
     */
    public void clearUniqueKeyCache() {
        uniqueKeyMapColumn.clear();
        uniqueKeyValidatorForTwoColumnRange.clear();
    }

    /**
     * Returns <code>true</code> if the caches of unique key values are empty.
     */
    public boolean isEmtpy() {
        return uniqueKeyMapColumn.isEmpty() && uniqueKeyValidatorForTwoColumnRange.isEmpty();
    }

    /**
     * Returns <code>true</code> if there was an unique key error state change, e.g. current state
     * is error previous state was error free
     */
    public boolean wasErrorStateChange() {
        if (currentErrorStatus != previousErrorStatus) {
            previousErrorStatus = currentErrorStatus;
            return true;
        }
        return false;
    }

    /**
     * If a row has changed then this method updates the underlying unique key cache for this row
     */
    public void handleRowChanged(ITableContentsGeneration tableContentsGeneration, Row row) {
        updateAllUniqueKeysCache(tableContentsGeneration, row, HANDLE_UNIQUEKEY_ROW_CHANGED);
    }

    /**
     * If a row has removed then this method updates the underlying unique key cache for this row
     */
    public void handleRowRemoved(ITableContentsGeneration tableContentsGeneration, Row row) {
        updateAllUniqueKeysCache(tableContentsGeneration, row, HANDLE_UNIQUEKEY_ROW_REMOVED);
    }

    /**
     * Updates the given row depending on the given operation in all unique key caches with the new
     * calculated key value of the given row.
     */
    private void updateAllUniqueKeysCache(ITableContentsGeneration tableContentsGeneration, Row row, int operation) {
        if (cachedTableStructure == null) {
            return;
        }
        cacheTableStructureAndValueDatatypes(tableContentsGeneration);

        IUniqueKey[] uniqueKeys = cachedTableStructure.getUniqueKeys();
        for (IUniqueKey uniqueKey : uniqueKeys) {
            if (uniqueKey.containsTwoColumnRanges()) {
                updateUniqueKeysColumnRange(row, operation, uniqueKey);
            } else {
                updateUniqueKeysCacheColumn(row, operation, uniqueKey);
            }
        }
    }

    /**
     * Updates the unique key cache for the given row. This method handles the update of non two
     * column range keys.
     */
    private void updateUniqueKeysCacheColumn(Row row, int operation, IUniqueKey uniqueKey) {
        Map<AbstractKeyValue, Object> keyValueMap = uniqueKeyMapColumn.get(uniqueKey);

        // if not exist, create a new cache (map) for the given unique key first
        if (keyValueMap == null) {
            keyValueMap = new HashMap<AbstractKeyValue, Object>();
            uniqueKeyMapColumn.put(uniqueKey, keyValueMap);
        }

        updateKeyValueInMap(keyValueMap, KeyValue.createKeyValue(cachedTableStructure, uniqueKey, row), row, operation);
    }

    /**
     * Updates the unique key cache for the given row. This method handles the update of all two
     * column range keys.
     */
    private void updateUniqueKeysColumnRange(Row row, int operation, IUniqueKey uniqueKey) {
        UniqueKeyValidatorRange uniqueKeyValidatorRange = uniqueKeyValidatorForTwoColumnRange.get(uniqueKey);
        if (uniqueKeyValidatorRange == null) {
            uniqueKeyValidatorRange = new UniqueKeyValidatorRange(this, uniqueKey);
            uniqueKeyValidatorForTwoColumnRange.put(uniqueKey, uniqueKeyValidatorRange);
        }
        uniqueKeyValidatorRange.updateUniqueKeysCache(row, operation);
    }

    /**
     * Updates the key value and the given row in the given map (cache)
     */
    @SuppressWarnings("unchecked")
    static void updateKeyValueInMap(Map<AbstractKeyValue, Object> keyValueMap,
            AbstractKeyValue keyValue,
            Row row,
            int operation) {

        Object rowOrRowsForKeyValue = keyValueMap.get(keyValue);

        // key value dosn't exists in cache
        if (rowOrRowsForKeyValue == null) {
            // in case of a row change, add the new key value
            // do nothing if the row was removed
            if (operation == HANDLE_UNIQUEKEY_ROW_CHANGED) {
                keyValueMap.put(keyValue, row);
            }
            return;
        }

        // key value exists
        // update the cache
        if (rowOrRowsForKeyValue instanceof Row) {
            // exact one row found for the given key value
            updateKeyValueRowInMap(keyValueMap, keyValue, row, operation, (Row)rowOrRowsForKeyValue);
            return;
        } else if (rowOrRowsForKeyValue instanceof List) {
            // more than one rows found for the given key value
            updateKeyValueListInMap(keyValueMap, keyValue, row, operation, (List<Row>)rowOrRowsForKeyValue);
            return;
        }
        throw new RuntimeException("Unsupported key value found !" + rowOrRowsForKeyValue.getClass().getName()); //$NON-NLS-1$
    }

    /**
     * Update the row of for given key value in the map of cached key values 1) operation=update: a)
     * if the row found is the current row then nothing to do b) if the row found is another row add
     * a new list containing the two rows 2) operation=remove: remove the key value from the map of
     * cached key items
     */
    private static void updateKeyValueRowInMap(Map<AbstractKeyValue, Object> keyValueMap,
            AbstractKeyValue keyValue,
            Row row,
            int operation,
            Row rowOrRowsForKeyValue) {

        if (operation == HANDLE_UNIQUEKEY_ROW_CHANGED) {
            if (row == rowOrRowsForKeyValue) {
                // same row nothing to to
                return;
            }
            // new row with same key, add a new List
            List<Row> rows = new ArrayList<Row>(2);
            rows.add(rowOrRowsForKeyValue);
            rows.add(row);
            keyValueMap.put(keyValue, rows);
        } else if (operation == HANDLE_UNIQUEKEY_ROW_REMOVED) {
            if (!(rowOrRowsForKeyValue == row) && keyValue.isValid(rowOrRowsForKeyValue)) {
                // normally this can never be happen, because if this is not the current row
                // then there must be a list of rows or the update has not worked correctly before
                // (inconsistent cache)
                // but sometimes while scrolling the table contents this state could be occur, thus
                // we just ignore it here
            }
            keyValueMap.remove(keyValue);
        } else {
            throw new RuntimeException("Unsupported operation: " + operation); //$NON-NLS-1$
        }
    }

    /**
     * Update the list of rows for the give key value in the map of cached key values 1)
     * operation=update: add the row to the list of rows 2) operation=remove: removes the row from
     * the list of rows, if there is only one row left, add the row directly without a list
     */
    private static void updateKeyValueListInMap(Map<AbstractKeyValue, Object> keyValueMap,
            AbstractKeyValue keyValue,
            Row row,
            int operation,
            List<Row> rows) {

        if (operation == HANDLE_UNIQUEKEY_ROW_CHANGED) {
            if (!rows.contains(row)) {
                // new row
                rows.add(row);
            }
        } else if (operation == HANDLE_UNIQUEKEY_ROW_REMOVED) {
            rows.remove(row);
            if (rows.size() == 1) {
                // only one row is left
                // store the row instead the list
                keyValueMap.put(keyValue, rows.get(0));
            }
        }
    }

    /**
     * Validate all unique keys in all caches.
     */
    public void validateAllUniqueKeys(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        cachedValueDatatypes = datatypes;
        cachedTableStructure = tableStructure;

        if (TRACE_VALIDATION_CACHE) {
            printCachedContent();
        }

        MessageList validationErrors = new MessageList();
        validateUniqueKeys(validationErrors, uniqueKeyMapColumn);
        validateUniqueKeysRange(validationErrors, uniqueKeyValidatorForTwoColumnRange);

        list.add(validationErrors);
        currentErrorStatus = validationErrors.getNoOfMessages() > 0;
    }

    private void printCachedContent() {
        System.out.println("uniqueKeyValidatorForTwoColumnRange:"); //$NON-NLS-1$
        for (UniqueKeyValidatorRange entry : uniqueKeyValidatorForTwoColumnRange.values()) {
            entry.printCachedContent("  "); //$NON-NLS-1$
        }
    }

    private void validateUniqueKeysRange(MessageList list,
            Map<IUniqueKey, UniqueKeyValidatorRange> uniqueKeyValidatorForTwoColumnRange) {

        for (UniqueKeyValidatorRange uniqueKeyValidatorRange : uniqueKeyValidatorForTwoColumnRange.values()) {
            uniqueKeyValidatorRange.validate(list);
        }

    }

    /**
     * Validates all unique key maps in the given map (cache). For each unique key there is separate
     * map of key value objects. This method validates the key values for column key values only -
     * not key value ranges (two column key value objects)
     */
    private void validateUniqueKeys(MessageList list, Map<IUniqueKey, Map<AbstractKeyValue, Object>> uniqueKeyMap2) {
        // iterate all unique keys, specified in the table structure
        for (Map<AbstractKeyValue, Object> keyValuesForUniqueKeyCache : uniqueKeyMap2.values()) {
            List<AbstractKeyValue> invalidkeyValues = new ArrayList<AbstractKeyValue>();

            // iterate all key values and check if there is a unique key violation
            for (Entry<AbstractKeyValue, Object> keyValueEntry : keyValuesForUniqueKeyCache.entrySet()) {
                validateKeyValue(list, keyValueEntry, invalidkeyValues);
            }

            // remove invalid (obsolete key items)
            for (AbstractKeyValue abstractKeyValue : invalidkeyValues) {
                keyValuesForUniqueKeyCache.remove(abstractKeyValue);
            }
        }
    }

    /**
     * Validates the given key value entry. The key value entry (cache entry) contains the key value
     * and either the row or a list of rows which matches the key value. If there is a list of rows,
     * then there is a unique key violation and a validation error will be created and added to the
     * given message list.
     * 
     * Additional the key value will be checked if the stored value is up to date for the stored
     * row, otherwise the key value is invalid and must be removed from the list; if no more rows
     * are left then the key value is invalid.
     */
    @SuppressWarnings("unchecked")
    void validateKeyValue(MessageList list,
            Entry<AbstractKeyValue, Object> keyValueEntry,
            List<AbstractKeyValue> invalidkeyValues) {

        AbstractKeyValue keyValue = keyValueEntry.getKey();

        if (keyValueEntry.getValue() instanceof List) {
            List<Row> rows = (List<Row>)keyValueEntry.getValue();
            List<Row> rowsChecked = new ArrayList<Row>(rows.size());

            /*
             * auto-fix invalid rows check if the row value matches the key value, if not remove the
             * row this could be happen, because a row change triggers only the creation of new key
             * values, the old key value will not be updated e.g. rowA has a key value 1, and rowB
             * has key value 1 rowA changed to key value 2, then a new key value 2 is created and
             * related to rowA but key value 1 still contains a relation to rowA and rowB so key
             * value 1 must be fixed: isValid(rowA) returns false
             */
            for (Row currentRow : rows) {
                if (keyValue.isValid(currentRow)) {
                    rowsChecked.add(currentRow);
                }
            }

            // check if the key value has more then one row
            if (rowsChecked.size() == 0) {
                // key value has no more rows
                // will be removed later
                invalidkeyValues.add(keyValue);
                return;
            }

            // update key value object
            keyValueEntry.setValue(rowsChecked);

            // check unique key violation
            if (rowsChecked.size() > 1) {
                // more then one unique key exists, create validation error for each row
                for (Row row : rowsChecked) {
                    createValidationErrorUniqueKeyViolation(list, keyValue.getUniqueKey(), row);
                }
            }
        }
    }

    /**
     * Store (cache) the table structure and value datatype's of all columns
     */
    void cacheTableStructureAndValueDatatypes(ITableContentsGeneration tableContentsGeneration) {
        if (cachedTableStructure == null) {
            updateCachedTableStructure((TableContentsGeneration)tableContentsGeneration);
        }
        if (cachedValueDatatypes == null) {
            updateCachedValueDatatypes((TableContentsGeneration)tableContentsGeneration);
        }
    }

    private void updateCachedValueDatatypes(TableContentsGeneration tableContentsGeneration) {
        if (cachedTableStructure == null) {
            return;
        }
        try {
            cachedValueDatatypes = ((TableContents)tableContentsGeneration.getTableContents()).findColumnDatatypes(
                    cachedTableStructure, tableContentsGeneration.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin
                    .log(new IpsStatus(
                            "Error searching value datatypes: " + tableContentsGeneration.getTableContents().getTableStructure())); //$NON-NLS-1$
            return;
        }
    }

    private void updateCachedTableStructure(TableContentsGeneration tableContentsGeneration) {
        try {
            cachedTableStructure = tableContentsGeneration.getTableContents().findTableStructure(
                    tableContentsGeneration.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin
                    .log(new IpsStatus(
                            "Error searching TableStructure: " + tableContentsGeneration.getTableContents().getTableStructure())); //$NON-NLS-1$
            return;
        }
    }

    /**
     * Returns <code>true</code> if the cache needs to be updated, e.g. if the table structure
     * changed
     */
    public boolean isInvalidUniqueKeyCache(ITableStructure tableStructure) {
        return tableStrunctureModificationStamp != getTableStructureModTimeStamp(tableStructure);
    }

    /**
     * Stores the new modification time stamp of the table structure
     */
    public void setTableStructureModificationTimeStamp(ITableStructure tableStructure) {
        if (tableStructure == null) {
            return;
        }
        tableStrunctureModificationStamp = getTableStructureModTimeStamp(tableStructure);
    }

    private long getTableStructureModTimeStamp(ITableStructure tableStructure) {
        return tableStructure.getIpsSrcFile().getEnclosingResource().getModificationStamp();
    }

    /**
     * Creates a unique key validation error and adds it to the give message list.
     */
    void createValidationErrorUniqueKeyViolation(MessageList list, IUniqueKey uniqueKey, Row row) {
        String text = NLS.bind(Messages.UniqueKeyValidator_msgUniqueKeyViolation, row.getRowNumber(), uniqueKey
                .getName());
        List<ObjectProperty> objectProperties = new ArrayList<ObjectProperty>();
        createObjectProperties(uniqueKey, row, objectProperties);
        list.add(new Message(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION, text, Message.ERROR, objectProperties
                .toArray(new ObjectProperty[objectProperties.size()])));
    }

    private void createObjectProperties(IUniqueKey uniqueKey, Row row, List<ObjectProperty> objectProperties) {
        IKeyItem[] items = uniqueKey.getKeyItems();
        for (IKeyItem item : items) {
            IColumn[] columns = item.getColumns();
            for (IColumn column : columns) {
                objectProperties.add(new ObjectProperty(row, IRow.PROPERTY_VALUE, cachedTableStructure
                        .getColumnIndex(column)));
            }
        }
    }
}
