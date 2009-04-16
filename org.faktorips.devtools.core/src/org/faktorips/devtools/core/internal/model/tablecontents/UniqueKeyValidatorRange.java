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

package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.tablestructure.ColumnRange;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
    private UniqueKeyValidator uniqueKeyValidator;
    
    // the unique key this validator is for
    private IUniqueKey uniqueKey;
    
    // cache contains for each key value a map of column ranges (key) and sorted maps (value)
    // each map contains the KeyValueRange ('from'-value) and the rows which matches this key value range
    private Map<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Object>>> keyValueRanges = new HashMap<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Object>>>(); 
    
    public UniqueKeyValidatorRange(UniqueKeyValidator uniqueKeyValidator, IUniqueKey uniqueKey) {
        this.uniqueKeyValidator = uniqueKeyValidator;
        this.uniqueKey = uniqueKey;
    }

    /**
     * Updates the unique key values cache with the new calculated key value of the given row.
     */
    public void updateUniqueKeysCache(Row row, int operation) {
        // update the key value (non two-column-key-item-value)
        KeyValue keyValue = KeyValue.createKeyValue(uniqueKeyValidator.getCachedTableStructure(), uniqueKey, row);
        Map<ColumnRange, SortedMap<AbstractKeyValue, Object>> listOfRangeMaps = keyValueRanges.get(keyValue);
        if (listOfRangeMaps == null){
            listOfRangeMaps = new HashMap<ColumnRange, SortedMap<AbstractKeyValue, Object>>();
            keyValueRanges.put(keyValue, listOfRangeMaps);
        }
        
        // update the sorted maps for each two-column-range key item
        List<ColumnRange> twoColumnRanges = AbstractKeyValue.getTwoColumnRanges(uniqueKey);
        for (Iterator<ColumnRange> iterator = twoColumnRanges.iterator(); iterator.hasNext();) {
            ColumnRange columnRange = iterator.next();
            SortedMap<AbstractKeyValue, Object> sortedMap = listOfRangeMaps.get(columnRange);
            if (sortedMap == null){
                sortedMap = new TreeMap<AbstractKeyValue, Object>();
                listOfRangeMaps.put(columnRange, sortedMap);
            }

            updateUniqueKeysColumnRange(uniqueKeyValidator.getCachedTableStructure(), row, operation, columnRange, sortedMap);
        }
    }
    
    /*
     * Update the key value range of the given row and column range in the given sorted map
     */
    private void updateUniqueKeysColumnRange(ITableStructure tableStructure,
            Row row,
            int operation,
            ColumnRange columnRange,
            SortedMap<AbstractKeyValue, Object> keyValueRangeMap) {

        KeyValueRange keyValueRange = KeyValueRange.createKeyValue(tableStructure, uniqueKeyValidator
                .getCachedValueDatatypes(), uniqueKey, row, columnRange);
        // add the key value range
        //   if the value is not parsable then the key value are not added to the cache, 
        //   otherwise the sorted map can not work correctly (compareTo method fails) 
        if (keyValueRange.isParsable()){
            UniqueKeyValidator.updateKeyValueInMap(keyValueRangeMap, keyValueRange, row, operation);
        }
    }
    
    /**
     * Validates the unique keys values in the cache.
     */
    public void validate(MessageList list) throws CoreException{
        // list to auto-fix invalid key values
        List<KeyValue> invalidKeyValues = new ArrayList<KeyValue>();
        
        for (Iterator<Map.Entry<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Object>>>> iterator = keyValueRanges.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<KeyValue, Map<ColumnRange, SortedMap<AbstractKeyValue, Object>>> entry = iterator.next();
            // check if the key value is valid
            KeyValue keyValue = entry.getKey();
            if (!keyValue.isValid()){
                invalidKeyValues.add(keyValue);
                continue;
            }
            
            Map<ColumnRange, SortedMap<AbstractKeyValue, Object>> columnRangeMaps = entry.getValue();
            
            // validate all ranges, 
            //   the ranges will be validate separately, therefore
            //   all ranges must contains at least an unique key validation error
            MessageList allUniqueKeyValidationErrors = new MessageList();
            for (Iterator<Map.Entry<ColumnRange, SortedMap<AbstractKeyValue, Object>>> iterator2 = columnRangeMaps.entrySet().iterator(); iterator2.hasNext();) {
                Map.Entry<ColumnRange, SortedMap<AbstractKeyValue, Object>> entry2 = iterator2.next();
                ColumnRange columRange = entry2.getKey();
                SortedMap<AbstractKeyValue, Object> keyValueRangeMap = entry2.getValue();
                validateUniqueKeysRange(allUniqueKeyValidationErrors, keyValue, columRange, keyValueRangeMap);
            }
            list.add(allUniqueKeyValidationErrors);
        }
        
        removeInvalidKeyValues(invalidKeyValues);
    }
    
    private void removeInvalidKeyValues(List<KeyValue> invalidKeyValues) {
        for (Iterator<KeyValue> iterator = invalidKeyValues.iterator(); iterator.hasNext();) {
            KeyValue invalidKeyValue = iterator.next();
            keyValueRanges.remove(invalidKeyValue);
        }
    }

    /*
     * Validates all unique key maps in the given map (cache). For each unique key there is separate map of key value range objects.
     * This method validates the key value ranges for key value ranges (two column key value objects) only
     */
    @SuppressWarnings("unchecked")
    private boolean validateUniqueKeysRange(MessageList uniqueKeyValidationErrors, KeyValue keyValue, ColumnRange columnRange, SortedMap<AbstractKeyValue, Object> keyValueRangeMap) throws CoreException {
            List<AbstractKeyValue> invalidkeyValues = new ArrayList<AbstractKeyValue>();
            
            // iterate all key value range (from-column) objects of the unique key
            KeyValueRange prevKeyValueFrom = null;
            for (Iterator<Map.Entry<AbstractKeyValue, Object>> iter = keyValueRangeMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<AbstractKeyValue, Object> entry = iter.next();
                
                KeyValueRange keyValueFrom = (KeyValueRange)entry.getKey();
                Object keyValueObject = entry.getValue();
                
                // check if the key value object is valid, if one key item column has changed then the current key value object could be invalid
                // ignore invalid key values
                if (!keyValueFrom.isValid()){
                    invalidkeyValues.add(keyValueFrom);
                    continue;
                }
                
                // check if the KeyValueRange row is valid for the given key value (non two column range key value)
                if (!keyValue.isValid(keyValueFrom.getRow())){
                    invalidkeyValues.add(keyValueFrom);
                    continue;
                }
                
                // same from value
                if (keyValueObject instanceof List){
                    validateUniqueKeyRangeRowList(uniqueKeyValidationErrors, keyValue, (List<Row>)keyValueObject, columnRange,
                            invalidkeyValues);
                }

                // abort validation of current key if there are to many unique key violations
                if (uniqueKeyValidationErrors.getNoOfMessages() >= 10){
                    createValidationErrorToManyUniqueKeyViolations(uniqueKeyValidationErrors, uniqueKey, 10);
                    break;
                }

                if (prevKeyValueFrom == null){
                    prevKeyValueFrom = keyValueFrom;
                    continue;
                }
                
                List rowsChecked = validateKeyValueRange(uniqueKeyValidationErrors, uniqueKey, 
                        (KeyValueRange)prevKeyValueFrom,
                        keyValueFrom, keyValueObject, invalidkeyValues);
                
                if (rowsChecked != null){
                    // update the valid rows for the key value entry
                    entry.setValue(rowsChecked);
                }
                
                
                prevKeyValueFrom = keyValueFrom;
            }

            // remove invalid (obsolete key items)
            for (Iterator<AbstractKeyValue> iterInvalid = invalidkeyValues.iterator(); iterInvalid.hasNext();) {
                keyValueRangeMap.remove((AbstractKeyValue)iterInvalid.next());
            }
            
            return uniqueKeyValidationErrors.getMessageByCode(ITableContents.MSGCODE_UNIQUE_KEY_VIOLATION)!=null;
    }

    private void validateUniqueKeyRangeRowList(MessageList uniqueKeyValidationErrors,
            KeyValue keyValue,
            List<Row> rows,
            ColumnRange columnRange,
            List<AbstractKeyValue> invalidkeyValues) {
        if (AbstractKeyValue.getTwoColumnRanges(uniqueKey).size() == 1){
            // only one range exists, always an error
            for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext();) {
                uniqueKeyValidator.createValidationErrorUniqueKeyViolation(uniqueKeyValidationErrors, keyValue, iterator.next());                    
            }
        } else {
            // iterate all key value range objects with same from value
            // will be an error if the other ranges overlap too
            KeyValueRange prev = null;
            for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext();) {
                Row row = iterator.next();
                KeyValueRange keyValueRangeForRow = KeyValueRange.createKeyValue(uniqueKeyValidator.getCachedTableStructure(), uniqueKeyValidator.getCachedValueDatatypes(), 
                        uniqueKey, row, columnRange);
                if (prev == null){
                    prev = keyValueRangeForRow;
                    continue;
                }
                validateKeyValueRange(uniqueKeyValidationErrors, uniqueKey, 
                        prev,
                        keyValueRangeForRow, row, invalidkeyValues); 
                prev = keyValueRangeForRow;
            }
        }
    } 
    
    /*
     * Validates the given key value range object entry against the given 'from'- and 'to'-value key
     * value range objects. The key value entry (cache entry) contains the key value and either the
     * row or a list of rows which matches (are greater or less) the key value range. Each matched
     * key value range object must checked against the given 'to' or 'from' key value range object.
     * 
     * Additional the key value range object will be checked if the stored value is up to date for the stored
     * row, otherwise the key value range object is invalid and must be removed from the list if no more rows are
     * left then the key value range object is invalid.
     */    
    @SuppressWarnings("unchecked")
    private List validateKeyValueRange(MessageList list, IUniqueKey uniqueKey, KeyValueRange prevFrom, 
            KeyValueRange keyValue, Object keyValueObject, List<AbstractKeyValue> invalidkeyValues) {

        // ignore invalid key values
        if (!prevFrom.isValid()){
            invalidkeyValues.add(prevFrom);
            return null;
        }
        
        if (keyValueObject instanceof Row){
            Row currentRow = (Row)keyValueObject;

            // ignore invalid key values
            if (!keyValue.isValid()){
                invalidkeyValues.add(keyValue);
                return null;
            }
            
            validateKeyValueRangeFor(list, uniqueKey, prevFrom, keyValue, currentRow);
         } else if (keyValueObject instanceof List){
            List<Row> rows = (List)keyValueObject;
            List<Row> rowsChecked = new ArrayList<Row>();
            
            // first check if all rows are 'valid' for the key value
            for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext();) {
                Row currentRow = iterator.next();
                if (!keyValue.isValid(currentRow)) {
                    invalidkeyValues.add(keyValue);
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
            for (Iterator<Row> iterator = rowsChecked.iterator(); iterator.hasNext();) {
                Row currentRow = (Row)iterator.next();

                // validate each row against the previous row
                if (collisionInAllRanges(uniqueKey, keyValue, prevFrom.getRow(), currentRow)){
                    uniqueKeyValidator.createValidationErrorUniqueKeyViolation(list, prevFrom, prevFrom.getRow());
                    uniqueKeyValidator.createValidationErrorUniqueKeyViolation(list, keyValue, currentRow);
                }
            }
            return rowsChecked;
         } else {
             throw new RuntimeException("Wrong key value object :" + keyValueObject.getClass().getName() );
         }
        
        return null;
    }
    
    /*
     * Validates the given key value entry. Check if the range doesn't overlap with the given range ('from' and 'to' key value range)
     *  
     * The row could be an other as stored in the key value range 
     */
    private void validateKeyValueRangeFor(MessageList list,
            IUniqueKey uniqueKey,
            KeyValueRange prevFrom,
            KeyValueRange currKeyValue,
            Row currentRow) {
        Row prevRow = prevFrom.getRow();
        if (currentRow == prevRow) {
            return;
        }
        
        if (currKeyValue.isFromLessOrEqual(prevFrom.getValueTo()) &&
                prevFrom.isFromLessOrEqual(currKeyValue.getValueTo())) {
            // range collision, check other ranges too
            if (collisionInAllRanges(uniqueKey, currKeyValue, prevRow, currentRow)){
                uniqueKeyValidator.createValidationErrorUniqueKeyViolation(list, prevFrom, prevRow);
                uniqueKeyValidator.createValidationErrorUniqueKeyViolation(list, currKeyValue, currentRow);
            }
            
        }
    }

    private boolean collisionInAllRanges(IUniqueKey uniqueKey, KeyValueRange prevFrom, Row row1, Row row2) {
        List<ColumnRange> twoColumnRanges = AbstractKeyValue.getTwoColumnRanges(uniqueKey);
        for (Iterator<ColumnRange> iterator = twoColumnRanges.iterator(); iterator.hasNext();) {
            ColumnRange columnRange = iterator.next();
            if (!KeyValueRange.isRangeCollision(uniqueKeyValidator.getCachedTableStructure(), uniqueKeyValidator.getCachedValueDatatypes()
                    , columnRange, row1, row2)){
                return false;
            }
            
        }
        return true;
    }
    
    private void createValidationErrorToManyUniqueKeyViolations(MessageList list, IUniqueKey uniqueKey, int numberOfValidationErrors) {
        String text = NLS.bind("Table contents cannot be validated completly, there are more then {0} unique key violations for unique key {1}.", numberOfValidationErrors, uniqueKey);
        list.add(new Message(ITableContents.MSGCODE_TO_MANY_UNIQUE_KEY_VIOLATIONS, text, Message.ERROR)); 
    }    
}
