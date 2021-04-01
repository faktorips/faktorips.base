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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.model.internal.tablestructure.ColumnRange;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Unique key validator, validates two-column-range key items. Objects of this kind of class,
 * contains a cache of multiple key values (unique key value objects without two column range key
 * items), each key value has itself a cache of multiple column ranges (one for each two column
 * range key item), this column range cache contains a sorted map (sorted using the 'from'-value) of
 * all key value range objects inside the table contents row. Each key value range object can be
 * related to one or more rows (analog to the UniqueKeyValidator).
 * 
 * @author Joerg Ortmann
 */
public class UniqueKeyValidatorRange {

    private static final int MAX_NO_OF_UNIQUE_KEY_VALIDATION_ERRORS = 10;

    private UniqueKeyValidator uniqueKeyValidator;

    /** the unique key this validator is for */
    private IIndex uniqueKey;

    /**
     * cache contains for each key value a map of column ranges (key) and sorted maps (value) each
     * map contains the KeyValueRange ('from'-value) and the rows which matches this key value range
     */
    private Map<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>>> keyValueRanges = new HashMap<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>>>();

    private List<ColumnRange> twoColumnRanges;

    public UniqueKeyValidatorRange(UniqueKeyValidator uniqueKeyValidator, IIndex uniqueKey) {
        this.uniqueKeyValidator = uniqueKeyValidator;
        this.uniqueKey = uniqueKey;
        twoColumnRanges = AbstractKeyValue.getTwoColumnRanges(uniqueKey);
    }

    /**
     * Updates the unique key values cache with the new calculated key value of the given row.
     */
    public void updateUniqueKeysCache(Row row, int operation) {
        // update the key value (non two-column-key-item-value)
        KeyValue keyValue = KeyValue.createKeyValue(uniqueKeyValidator.getCachedTableStructure(), uniqueKey, row);
        Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> listOfRangeMaps = keyValueRanges.computeIfAbsent(keyValue, $ -> new HashMap<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>>());

        // update the sorted maps for each two-column-range key item
        for (ColumnRange columnRange : twoColumnRanges) {
            SortedMap<AbstractKeyValue, Set<Row>> sortedMap = listOfRangeMaps.computeIfAbsent(columnRange, $ -> new TreeMap<AbstractKeyValue, Set<Row>>());

            updateUniqueKeysColumnRange(uniqueKeyValidator.getCachedTableStructure(), row, operation, columnRange,
                    sortedMap);
        }
    }

    /**
     * Update the key value range of the given row and column range in the given sorted map
     */
    private void updateUniqueKeysColumnRange(ITableStructure tableStructure,
            Row row,
            int operation,
            ColumnRange columnRange,
            SortedMap<AbstractKeyValue, Set<Row>> keyValueRangeMap) {

        KeyValueRange keyValueRange = KeyValueRange.createKeyValue(tableStructure,
                uniqueKeyValidator.getCachedValueDatatypes(), uniqueKey, row, columnRange);
        /*
         * add the key value range, if the value is not parsable then the key value are not added to
         * the cache, otherwise the sorted map can not work correctly (compareTo method fails)
         */
        if (keyValueRange.isParsable()) {
            UniqueKeyValidator.updateKeyValueInMap(keyValueRangeMap, keyValueRange, row, operation);
        }
    }

    /**
     * Validates the unique keys values in the cache.
     */
    public void validate(MessageList list) {
        // update twoColumnRange (maybe uniqueKey has changed)
        twoColumnRanges = AbstractKeyValue.getTwoColumnRanges(uniqueKey);

        // list to auto-fix invalid key values
        List<KeyValue> invalidKeyValues = new ArrayList<KeyValue>();

        for (Entry<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>>> entry : keyValueRanges.entrySet()) {
            // check if the key value is valid
            KeyValue keyValue = entry.getKey();
            if (!keyValue.isValid()) {
                invalidKeyValues.add(keyValue);
                continue;
            }

            Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> columnRangeMaps = entry.getValue();

            validateAllRanges(list, keyValue, columnRangeMaps);

            if (list.getMessageByCode(ITableContents.MSGCODE_TOO_MANY_UNIQUE_KEY_VIOLATIONS) != null) {
                // abort validation if there to many unique key violations
                // because of performance reasons
                break;
            }
        }

        removeInvalidKeyValues(invalidKeyValues);
    }

    /**
     * Validate all two column ranges, the given map contains all column ranges with their key value
     * range objects and the related row (rows), the given key value contains the key value of all
     * non two column range key items. The sorted list contains only entries with the same key value
     * (non two column range key value).
     */
    private void validateAllRanges(MessageList list,
            KeyValue keyValue,
            Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> columnRangeMaps) {

        Set<Row> rowsUniqueKeyViolation = new HashSet<Row>(2);
        Map<KeyValueRange, Set<Row>> allRangesRowsSameFromValue = null;
        for (Entry<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> entry2 : columnRangeMaps.entrySet()) {
            SortedMap<AbstractKeyValue, Set<Row>> keyValueRangeMap = entry2.getValue();
            Map<KeyValueRange, Set<Row>> rowsSameFromValue = validateUniqueKeysRange(rowsUniqueKeyViolation, keyValue,
                    keyValueRangeMap);

            /*
             * store entries with same 'from'-value, these entries must be handled separately,
             * because the sorted map couldn't take care of those key value ranges note that these
             * could be a performance bottleneck if there are many column ranges with same from
             * values
             */
            allRangesRowsSameFromValue = mergeRowsInMap(allRangesRowsSameFromValue, rowsSameFromValue);
        }

        if (allRangesRowsSameFromValue != null && allRangesRowsSameFromValue.size() > 0) {
            for (Set<Row> rows : allRangesRowsSameFromValue.values()) {
                rowsUniqueKeyViolation.addAll(rows);
            }
        }

        MessageList uniqueKeyValidationErrors = new MessageList();
        for (Row row : rowsUniqueKeyViolation) {
            uniqueKeyValidator.createValidationErrorUniqueKeyViolation(uniqueKeyValidationErrors,
                    keyValue.getUniqueKey(), row);
            if (isMaxNoOfUniqueKeyViolationsReached(uniqueKeyValidationErrors)) {
                break;
            }
        }
        list.add(uniqueKeyValidationErrors);
    }

    /**
     * Returns a map of key value range objects with rows from the first map which exists at least
     * two times in the second map. The map are grouped by key value objects.
     * <p>
     * This method is used to cleanup the sets of rows with same 'from'-values in several column
     * ranges. As result we get a map with rows which have an unique key violation, because these
     * rows exists multiple times in different column ranges.
     */
    private Map<KeyValueRange, Set<Row>> mergeRowsInMap(Map<KeyValueRange, Set<Row>> prevRangesRowsSameFromValue,
            Map<KeyValueRange, Set<Row>> rowsSameFromValue) {

        if (prevRangesRowsSameFromValue == null) {
            return rowsSameFromValue;
        } else {
            Map<KeyValueRange, Set<Row>> result = new HashMap<KeyValueRange, Set<Row>>();

            Set<Row> rowsViolation = new HashSet<Row>(2);

            for (Entry<KeyValueRange, Set<Row>> entry : prevRangesRowsSameFromValue.entrySet()) {
                KeyValueRange keyValue = entry.getKey();
                Set<Row> rows = entry.getValue();
                // add all rows to the set rowsViolation which are collision with second set
                for (Entry<KeyValueRange, Set<Row>> entryInSecondSet : rowsSameFromValue.entrySet()) {
                    addOverlappingRows(rowsViolation, rows, entryInSecondSet.getValue());
                }
                if (rowsViolation.size() > 0) {
                    result.put(keyValue, rowsViolation);
                }
            }
            return result;
        }
    }

    /**
     * Check all row to each other if there are overlapping rows
     */
    private void addOverlappingRows(Set<Row> rowsViolation, Set<Row> rows, Set<Row> rowsInSecondSet) {

        for (Row row : rows) {
            boolean collision = false;
            for (Row rowSecond : rowsInSecondSet) {
                if (row == rowSecond) {
                    continue;
                }
                collision = collisionInAllRanges(row, rowSecond);
                if (collision) {
                    rowsViolation.add(row);
                    break;
                }
            }
            if (collision) {
                break;
            }
        }
    }

    private void removeInvalidKeyValues(List<KeyValue> invalidKeyValues) {
        for (KeyValue invalidKeyValue : invalidKeyValues) {
            keyValueRanges.remove(invalidKeyValue);
        }
    }

    /**
     * Validates all unique key maps in the given map (cache). For each unique key there is separate
     * map of key value range objects. This method validates the key value ranges for key value
     * ranges (two column key value objects) only
     */
    private Map<KeyValueRange, Set<Row>> validateUniqueKeysRange(Set<Row> rowsUniqueKeyViolation,
            KeyValue keyValue,
            SortedMap<AbstractKeyValue, Set<Row>> keyValueRangeMap) {

        List<AbstractKeyValue> invalidkeyValues = new ArrayList<AbstractKeyValue>();
        Map<KeyValueRange, Set<Row>> mapRowsSameFrom = new HashMap<KeyValueRange, Set<Row>>();

        // iterate all key value range (from-column) objects of the unique key
        KeyValueRange prevKeyValueFrom = null;

        updateInvalidKeyValues(keyValue, invalidkeyValues);

        for (Entry<AbstractKeyValue, Set<Row>> entry : keyValueRangeMap.entrySet()) {
            KeyValueRange keyValueFrom = (KeyValueRange)entry.getKey();
            Set<Row> keyValueObject = entry.getValue();

            if (!isValid(keyValue, keyValueFrom, keyValueFrom.getRow())) {
                continue;
            }

            // there are rows with same 'from'-value
            // cleanup list, maybe there are rows which are not valid anymore
            List<Row> validRows = updateValidRows(keyValue, keyValueFrom, keyValueObject);

            if (validRows.size() > 1) {
                Set<Row> rowsSameFrom = mapRowsSameFrom.computeIfAbsent(keyValueFrom, $ -> new HashSet<Row>(2));
                rowsSameFrom.addAll(validRows);
            } else if (validRows.size() == 0) {
                invalidkeyValues.add(keyValueFrom);
                continue;
            }

            // abort validation of current key if there are to many unique key violations
            if (rowsUniqueKeyViolation.size() >= MAX_NO_OF_UNIQUE_KEY_VALIDATION_ERRORS) {
                break;
            }

            if (prevKeyValueFrom != null) {

                Set<Row> rowsChecked = validateKeyValueRange(rowsUniqueKeyViolation, prevKeyValueFrom, keyValueFrom,
                        keyValueObject, invalidkeyValues);

                if (rowsChecked != null) {
                    // update the valid rows for the key value entry

                    // Issue FIPS-1386: setValue not supported, Problems using +XX:+AggressiveOpts
                    // entry.setValue(rowsChecked);

                    // Note that put with the same key is a valid operation - see JavaDoc
                    // put with a non-existing key results in a ModificationException
                    keyValueRangeMap.put(entry.getKey(), rowsChecked);
                }
            }

            prevKeyValueFrom = keyValueFrom;
        }

        // remove invalid (obsolete key items)
        for (AbstractKeyValue abstractKeyValue : invalidkeyValues) {
            keyValueRangeMap.remove(abstractKeyValue);
        }

        return mapRowsSameFrom;
    }

    private List<Row> updateValidRows(KeyValue keyValue, KeyValueRange keyValueFrom, Set<Row> keyValueObject) {
        List<Row> validRows = new ArrayList<Row>();
        for (Row row : keyValueObject) {
            // check if the key value is valid for the row
            // and check if the key value from is valid
            if (isValid(keyValue, keyValueFrom, row)) {
                validRows.add(row);
            }
        }
        return validRows;
    }

    /**
     * check if the key value object is valid, if one key item column has changed then the current
     * key value object could be invalid ignore invalid key values check if the KeyValueRange row is
     * valid for the given key value (non two column range key value)
     */
    private boolean isValid(KeyValue keyValue, KeyValueRange keyValueFrom, Row row) {
        return keyValueFrom.isValid(row) && keyValue.isValid(row);
    }

    private void updateInvalidKeyValues(KeyValue keyValue, List<AbstractKeyValue> invalidkeyValues) {
        if (!keyValue.isValid()) {
            invalidkeyValues.add(keyValue);
        }
    }

    private boolean isMaxNoOfUniqueKeyViolationsReached(MessageList list) {
        if (list.size() >= MAX_NO_OF_UNIQUE_KEY_VALIDATION_ERRORS) {
            createValidationErrorTooManyUniqueKeyViolations(list, MAX_NO_OF_UNIQUE_KEY_VALIDATION_ERRORS);
            return true;
        }
        return false;
    }

    /**
     * Validates the given key value range object entry against the given 'from'- and 'to'-value key
     * value range objects. The key value entry (cache entry) contains the key value and list of
     * rows which matches (are greater or less) the key value range. Each matched key value range
     * object must checked against the given 'to' or 'from' key value range object.
     * <p>
     * Additional the key value range object will be checked if the stored value is up to date for
     * the stored row, otherwise the key value range object is invalid and must be removed from the
     * list if no more rows are left then the key value range object is invalid.
     */
    private Set<Row> validateKeyValueRange(Set<Row> rowsUniqueKeyViolation,
            KeyValueRange prevFrom,
            KeyValueRange keyValue,
            Set<Row> rows,
            List<AbstractKeyValue> invalidkeyValues) {
        Set<Row> rowsChecked = new HashSet<Row>(rows.size());

        // first check if all rows are 'valid' for the key value
        for (Row currentRow : rows) {
            if (!keyValue.isValid(currentRow)) {
                /*
                 * note that the key value cannot be removed because there is at least one other row
                 * with the same from value (the hashcode of the keyValue range object is only the
                 * from value)
                 */
                continue;
            }
            rowsChecked.add(currentRow);
        }

        if (rowsChecked.size() == 0) {
            // key value has no more rows (invalid)
            // will be removed later
            invalidkeyValues.add(keyValue);
            return rowsChecked;
        }

        // validate all valid rows
        for (Row currentRow : rowsChecked) {

            // validate each row against the previous row
            if (collisionInAllRanges(prevFrom.getRow(), currentRow)) {
                rowsUniqueKeyViolation.add(prevFrom.getRow());
                rowsUniqueKeyViolation.add(currentRow);
            }
        }
        return rowsChecked;
    }

    private boolean collisionInAllRanges(Row row1, Row row2) {
        for (ColumnRange columnRange : twoColumnRanges) {
            if (!KeyValueRange.isRangeCollision(uniqueKeyValidator.getCachedTableStructure(),
                    uniqueKeyValidator.getCachedValueDatatypes(), columnRange, row1, row2)) {
                return false;
            }

        }
        return true;
    }

    private void createValidationErrorTooManyUniqueKeyViolations(MessageList list, int numberOfValidationErrors) {
        String text = NLS.bind(Messages.UniqueKeyValidatorRange_msgTooManyUniqueKeyViolations,
                numberOfValidationErrors, uniqueKey.getName());
        list.add(new Message(ITableContents.MSGCODE_TOO_MANY_UNIQUE_KEY_VIOLATIONS, text, Message.ERROR));
    }

    public void printCachedContent(String offset) {
        System.out.println(offset + " UniqueKeyRange:" + uniqueKey.getName()); //$NON-NLS-1$
        for (Entry<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>>> entry : keyValueRanges.entrySet()) {
            System.out.println(offset + " KeyValue:" + entry.getKey()); //$NON-NLS-1$
            printCachedColumnRange(offset + " ", entry.getValue()); //$NON-NLS-1$
        }
    }

    private void printCachedColumnRange(String offset, Map<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> sortedMap) {
        for (Entry<ColumnRange, SortedMap<AbstractKeyValue, Set<Row>>> entry : sortedMap.entrySet()) {
            System.out.println(offset + " ColumnRange:" + entry.getKey().getName()); //$NON-NLS-1$
            printCachedSordetMap(offset + " ", entry.getValue()); //$NON-NLS-1$
        }
    }

    private void printCachedSordetMap(String offset, SortedMap<AbstractKeyValue, Set<Row>> map) {
        for (Entry<AbstractKeyValue, Set<Row>> entry : map.entrySet()) {
            System.out.println(offset + " KeyValue:"); //$NON-NLS-1$
            printCachedEntryOrList(offset + " ", entry.getValue()); //$NON-NLS-1$
        }
    }

    private void printCachedEntryOrList(String offset, Set<Row> entry) {
        System.out.println(offset + " " + entry.size() + " Rows colision candidates, same from column:"); //$NON-NLS-1$ //$NON-NLS-2$
        for (Row object : entry) {
            System.out.println(offset + "   Row:" + printRow(object)); //$NON-NLS-1$
        }
    }

    private String printRow(Row row) {
        int noOfColumns = row.getNoOfColumns();
        String result = " [" + row.getRowNumber() + "] "; //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 0; i < noOfColumns; i++) {
            result += row.getValue(i) + ", "; //$NON-NLS-1$
        }
        return result.replaceAll(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
