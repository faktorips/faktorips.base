/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * This import operation imports a CSV files containing messages for validation rules.
 * <p>
 * As the provided by the super class {@link ValidationRuleMessagesImportOperation} it is able to
 * handle messages codes or qualified rule names as key value to identify a validation rule.
 * <p>
 * Before execute the import operation you have to set the column indices and the column delimiter.
 */
public class ValidationRuleCsvImporter extends ValidationRuleMessagesImportOperation {

    private int keyColumnIndex;

    private int valueColumnIndex;

    private char delimiter = ',';

    public ValidationRuleCsvImporter(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        super(contents, root, locale);
    }

    /**
     * This method set the column indices for the key and value column. The indices are 0 based,
     * that means 0 is the first column.
     * 
     * @param keyColumnIndex The index of the key column
     * @param valueColumnIndex the index of the value column, containing the message
     */
    public void setKeyAndValueColumn(int keyColumnIndex, int valueColumnIndex) {
        this.keyColumnIndex = keyColumnIndex;
        this.valueColumnIndex = valueColumnIndex;
    }

    /**
     * This method sets the column delimiter used in the given CSV file.
     * 
     * @param delimiter The column delimiter of the CSV file
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    protected IStatus loadContent() {
        try (InputStreamReader reader = new InputStreamReader(getContents());
                CSVReader csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(new CSVParserBuilder()
                                .withSeparator(delimiter)
                                .build())
                        .build()) {
            ColumnPositionMappingStrategy<CsvTableBean> strat = new ColumnPositionMappingStrategy<>();
            String[] columns = new String[Math.max(keyColumnIndex, valueColumnIndex) + 1];
            columns[keyColumnIndex] = CsvTableBean.PROPERTY_KEY;
            columns[valueColumnIndex] = CsvTableBean.PROPERTY_VALUE;
            strat.setColumnMapping(columns);
            CsvToBean<CsvTableBean> csvToBean = new CsvToBean<>();
            csvToBean.setCsvReader(csvReader);
            csvToBean.setMappingStrategy(strat);
            List<CsvTableBean> list = csvToBean.parse();
            MultiStatus multipleMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, 0,
                    Messages.ValidationRuleMessagesPropertiesImporter_status_problemsDuringImport, null);
            Map<String, String> indexMap = indexTableEntries(list, multipleMessages);
            setKeyValueMap(indexMap);
            return multipleMessages;
            // CSOFF: IllegalCatch
            // The opencsv API throws RuntimeException
        } catch (RuntimeException | IOException e) {
            return new IpsStatus(e);
        }
        // CSON: IllegalCatch
    }

    Map<String, String> indexTableEntries(List<CsvTableBean> tableBeans, MultiStatus multipleMessages) {
        HashMap<String, String> indexMap = new HashMap<>();
        for (CsvTableBean csvTableBean : tableBeans) {
            String previousValue = indexMap.put(csvTableBean.getKey(), csvTableBean.getValue());
            if (previousValue != null) {
                multipleMessages.add(new IpsStatus(IStatus.ERROR, NLS.bind(
                        Messages.ValidationRuleCsvImporter_error_duplicatedKey, csvTableBean.getKey())));
            }
        }
        return indexMap;
    }

    public static class CsvTableBean {

        public static final String PROPERTY_KEY = "key"; //$NON-NLS-1$

        public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

        private String key;

        private String value;

        public CsvTableBean() {
            // default constructor for bean convention
        }

        public CsvTableBean(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            CsvTableBean other = (CsvTableBean)obj;
            return Objects.equals(key, other.key)
                    && Objects.equals(value, other.value);
        }

        @Override
        public String toString() {
            return "CsvTableBean [key=" + key + ", value=" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

    }

}
