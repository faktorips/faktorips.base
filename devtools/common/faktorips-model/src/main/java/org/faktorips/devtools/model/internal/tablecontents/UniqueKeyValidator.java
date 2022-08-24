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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;

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

    public static final boolean TRACE_VALIDATION_CACHE;

    static {
        TRACE_VALIDATION_CACHE = Boolean
                .parseBoolean(Abstractions.getDebugOption("org.faktorips.devtools.model/trace/tablecontentvalidation")); //$NON-NLS-1$
    }

    /** cache to validate simple column unique keys */
    private Map<IIndex, Map<AbstractKeyValue, Set<Row>>> uniqueKeyMapColumn = new HashMap<>();

    /**
     * caches to validate column ranges of type TWO_COLUMN_RANGE note that the unique key validation
     * of one column ranges (ONE_COLUMN_RANGE_FROM and ONE_COLUMN_RANGE_TO) are not necessary,
     * because the nature of this type of range is to return always one row this map contains a
     * special UniqueKeyValidatorRange object for each unique key with at least one two column range
     */
    private Map<IIndex, UniqueKeyValidatorRange> uniqueKeyValidatorForTwoColumnRange = new HashMap<>();

    private MessageList cachedMessageList;

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
    public boolean isEmpty() {
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
    public void handleRowChanged(ITableRows tableContentsGeneration, Row row, IIndex[] uniqueKeys) {
        updateAllUniqueKeysCache(tableContentsGeneration, row, HANDLE_UNIQUEKEY_ROW_CHANGED, uniqueKeys);
    }

    /**
     * If a row has removed then this method updates the underlying unique key cache for this row
     */
    public void handleRowRemoved(ITableRows tableContentsGeneration, Row row, IIndex[] uniqueKeys) {
        updateAllUniqueKeysCache(tableContentsGeneration, row, HANDLE_UNIQUEKEY_ROW_REMOVED, uniqueKeys);
    }

    /**
     * Updates the given row depending on the given operation in all unique key caches with the new
     * calculated key value of the given row.
     * 
     */
    private void updateAllUniqueKeysCache(ITableRows tableContentsGeneration,
            Row row,
            int operation,
            IIndex[] uniqueKeys) {
        cachedMessageList = null;
        if (cachedTableStructure == null) {
            return;
        }
        cacheTableStructureAndValueDatatypes(tableContentsGeneration);

        for (IIndex uniqueKey : uniqueKeys) {
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
    private void updateUniqueKeysCacheColumn(Row row, int operation, IIndex uniqueKey) {
        Map<AbstractKeyValue, Set<Row>> keyValueMap = uniqueKeyMapColumn.computeIfAbsent(uniqueKey,
                $ -> new HashMap<>());
        updateKeyValueInMap(keyValueMap, KeyValue.createKeyValue(cachedTableStructure, uniqueKey, row), row, operation);
    }

    /**
     * Updates the unique key cache for the given row. This method handles the update of all two
     * column range keys.
     */
    private void updateUniqueKeysColumnRange(Row row, int operation, IIndex uniqueKey) {
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
    static void updateKeyValueInMap(Map<AbstractKeyValue, Set<Row>> keyValueMap,
            AbstractKeyValue keyValue,
            Row row,
            int operation) {
        Set<Row> rowsForKeyValue = keyValueMap.computeIfAbsent(keyValue, $ -> new HashSet<>(2));
        if (operation == HANDLE_UNIQUEKEY_ROW_CHANGED) {
            if (!rowsForKeyValue.contains(row)) {
                // new row
                rowsForKeyValue.add(row);
            }
        } else if (operation == HANDLE_UNIQUEKEY_ROW_REMOVED) {
            rowsForKeyValue.remove(row);
        }
    }

    /**
     * Validate all unique keys in all caches.
     */
    public void validateAllUniqueKeys(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        if (cachedMessageList != null && !isInvalidUniqueKeyCache(tableStructure)) {
            list.add(cachedMessageList);
            return;
        }
        cachedValueDatatypes = datatypes;
        cachedTableStructure = tableStructure;

        if (TRACE_VALIDATION_CACHE) {
            printCachedContent();
        }

        MessageList validationErrors = new MessageList();
        validateUniqueKeys(validationErrors, uniqueKeyMapColumn);
        validateUniqueKeysRange(validationErrors, uniqueKeyValidatorForTwoColumnRange);

        list.add(validationErrors);
        currentErrorStatus = validationErrors.containsErrorMsg();
        cachedMessageList = list;
    }

    private void printCachedContent() {
        System.out.println("uniqueKeyValidatorForTwoColumnRange:"); //$NON-NLS-1$
        for (UniqueKeyValidatorRange entry : uniqueKeyValidatorForTwoColumnRange.values()) {
            entry.printCachedContent("  "); //$NON-NLS-1$
        }
    }

    private void validateUniqueKeysRange(MessageList list,
            Map<IIndex, UniqueKeyValidatorRange> uniqueKeyValidatorForTwoColumnRange) {

        for (UniqueKeyValidatorRange uniqueKeyValidatorRange : uniqueKeyValidatorForTwoColumnRange.values()) {
            uniqueKeyValidatorRange.validate(list);
        }

    }

    /**
     * Validates all unique key maps in the given map (cache). For each unique key there is separate
     * map of key value objects. This method validates the key values for column key values only -
     * not key value ranges (two column key value objects)
     */
    private void validateUniqueKeys(MessageList list, Map<IIndex, Map<AbstractKeyValue, Set<Row>>> uniqueKeyMap2) {
        // iterate all unique keys, specified in the table structure
        for (Map<AbstractKeyValue, Set<Row>> keyValuesForUniqueKeyCache : uniqueKeyMap2.values()) {
            List<AbstractKeyValue> invalidkeyValues = new ArrayList<>();

            // iterate all key values and check if there is a unique key violation
            for (Entry<AbstractKeyValue, Set<Row>> keyValueEntry : keyValuesForUniqueKeyCache.entrySet()) {
                validateKeyValue(list, keyValueEntry, invalidkeyValues, keyValuesForUniqueKeyCache);
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
    void validateKeyValue(MessageList list,
            Entry<AbstractKeyValue, Set<Row>> keyValueEntry,
            List<AbstractKeyValue> invalidkeyValues,
            Map<AbstractKeyValue, Set<Row>> keyValuesForUniqueKeyCache) {

        AbstractKeyValue keyValue = keyValueEntry.getKey();

        Set<Row> rows = keyValueEntry.getValue();
        Set<Row> rowsChecked = new HashSet<>(rows.size());

        /*
         * auto-fix invalid rows check if the row value matches the key value, if not remove the row
         * this could be happen, because a row change triggers only the creation of new key values,
         * the old key value will not be updated e.g. rowA has a key value 1, and rowB has key value
         * 1 rowA changed to key value 2, then a new key value 2 is created and related to rowA but
         * key value 1 still contains a relation to rowA and rowB so key value 1 must be fixed:
         * isValid(rowA) returns false
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

        // Issue FIPS-1386: setValue not supported, Problems using +XX:+AggressiveOpts
        // keyValueEntry.setValue(rowsChecked);
        keyValuesForUniqueKeyCache.put(keyValueEntry.getKey(), rowsChecked);

        // check unique key violation
        if (rowsChecked.size() > 1) {
            // more then one unique key exists, create validation error for each row
            for (Row row : rowsChecked) {
                createValidationErrorUniqueKeyViolation(list, keyValue.getUniqueKey(), row);
            }
        }
    }

    /**
     * Store (cache) the table structure and value datatype's of all columns
     */
    void cacheTableStructureAndValueDatatypes(ITableRows tableContentsGeneration) {
        if (cachedTableStructure == null || isInvalidUniqueKeyCache(cachedTableStructure)) {
            updateCachedTableStructure((TableRows)tableContentsGeneration);
        }
        if (cachedValueDatatypes == null) {
            updateCachedValueDatatypes((TableRows)tableContentsGeneration);
        }
    }

    private void updateCachedValueDatatypes(TableRows tableContentsGeneration) {
        if (cachedTableStructure == null) {
            return;
        }
        try {
            cachedValueDatatypes = ((TableContents)tableContentsGeneration.getTableContents()).findColumnDatatypes(
                    cachedTableStructure, tableContentsGeneration.getIpsProject());
        } catch (IpsException e) {
            IpsLog.log(new IpsStatus(
                    "Error searching value datatypes: " //$NON-NLS-1$
                            + tableContentsGeneration.getTableContents().getTableStructure()));
            return;
        }
    }

    private void updateCachedTableStructure(TableRows tableContentsGeneration) {
        try {
            cachedTableStructure = tableContentsGeneration.getTableContents().findTableStructure(
                    tableContentsGeneration.getIpsProject());
        } catch (IpsException e) {
            IpsLog.log(new IpsStatus(
                    "Error searching TableStructure: " //$NON-NLS-1$
                            + tableContentsGeneration.getTableContents().getTableStructure()));
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
    void createValidationErrorUniqueKeyViolation(MessageList list, IIndex uniqueKey, Row row) {
        String text = MessageFormat.format(Messages.UniqueKeyValidator_msgUniqueKeyViolation, row.getRowNumber() + 1,
                uniqueKey.getName());
        List<ObjectProperty> objectProperties = new ArrayList<>();
        createObjectProperties(uniqueKey, row, objectProperties);
        list.add(new Message(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION, text, Message.ERROR, objectProperties
                .toArray(new ObjectProperty[objectProperties.size()])));
    }

    private void createObjectProperties(IIndex uniqueKey, Row row, List<ObjectProperty> objectProperties) {
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
