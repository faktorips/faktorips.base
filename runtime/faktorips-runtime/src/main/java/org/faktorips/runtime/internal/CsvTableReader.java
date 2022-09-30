/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.faktorips.runtime.IRuntimeRepository;

/**
 * Encapsulation of OpenCSV code to avoid problems when tables are saved as XML and the library is
 * not on the classpath.
 */
class CsvTableReader {

    private static final String NULL_VALUE = "\\N";

    private CsvTableReader() {
        // util
    }

    static void readCsv(StringReader stringReader, Table<?> table, IRuntimeRepository productRepository) {
        CSVParser csvParser = new CSVParserBuilder().withSeparator('|').withQuoteChar('"')
                .withEscapeChar('\\')
                .build();
        try (CSVReader csvReader = new CSVReaderBuilder(stringReader)
                .withCSVParser(csvParser).build()) {
            String[] csvLine;
            while ((csvLine = csvReader.readNext()) != null) {
                for (int i = 0; i < csvLine.length; i++) {
                    if (NULL_VALUE.equals(csvLine[i])) {
                        csvLine[i] = null;
                    }
                }
                table.addRow(Arrays.asList(csvLine), productRepository);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
