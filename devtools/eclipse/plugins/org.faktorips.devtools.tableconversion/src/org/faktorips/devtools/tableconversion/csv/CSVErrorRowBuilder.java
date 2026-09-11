/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.opencsv.ICSVParser;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Builds a preview row for a CSV line that could not be parsed (e.g. because of an unterminated
 * quote), so that the UI can display and highlight exactly the broken cells instead of the whole
 * row.
 */
final class CSVErrorRowBuilder {

    private CSVErrorRowBuilder() {
        // utility class
    }

    /**
     * Determines the column at which {@code rawLine} breaks, reuses the still well-formed leading
     * fields and distributes the remaining (partial) raw content over the remaining columns, each
     * prefixed with {@link ITableFormat#PREVIEW_ERROR_CELL_MARKER}.
     */
    static String[] buildErrorRow(String rawLine, ICSVParser parser, int columnCount) {
        BrokenCell brokenField = findBrokenField(rawLine, parser.getSeparator());
        String[] brokenParts = splitBrokenParts(rawLine, brokenField, parser.getSeparator());

        int rowLength = Math.max(columnCount, brokenField.column() + brokenParts.length);
        String[] errorRow = new String[rowLength];
        Arrays.fill(errorRow, IpsStringUtils.EMPTY);

        fillLeadingFields(errorRow, rawLine, brokenField, parser);
        fillBrokenColumns(errorRow, brokenField, brokenParts);
        return errorRow;
    }

    /**
     * Scans the line for an unmatched quote and returns the column at which it starts.
     */
    private static BrokenCell findBrokenField(String rawLine, char separator) {
        int brokenColumn = 0;
        int rawContentStart = 0;
        boolean inQuotes = false;
        for (int i = 0; i < rawLine.length(); i++) {
            char c = rawLine.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == separator && !inQuotes) {
                brokenColumn++;
                rawContentStart = i + 1;
            }
        }
        return new BrokenCell(brokenColumn, rawContentStart);
    }

    /**
     * Fills the columns before the broken one with the values parsed from the still well-formed
     * leading part of the line.
     */
    private static void fillLeadingFields(String[] errorRow, String rawLine, BrokenCell brokenCell,
            ICSVParser parser) {
        if (brokenCell.rawContentStart() <= 0) {
            return;
        }
        String wellFormedPrefix = rawLine.substring(0, brokenCell.rawContentStart() - 1);
        try {
            String[] leadingFields = parser.parseLine(wellFormedPrefix);
            for (int i = 0; i < Math.min(leadingFields.length, errorRow.length); i++) {
                errorRow[i] = leadingFields[i];
            }
        } catch (IOException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Splits the broken raw content into the parts to be distributed over the columns
     * from the broken one onwards.
     */
    private static String[] splitBrokenParts(String rawLine, BrokenCell brokenCell, char separator) {
        String brokenValue = rawLine.substring(brokenCell.rawContentStart());
        if (brokenValue.startsWith("\"")) { //$NON-NLS-1$
            brokenValue = brokenValue.substring(1);
        }
        return brokenValue.split(Pattern.quote(String.valueOf(separator)), -1);
    }

    /**
     * Distributes the broken content over the columns from the broken one onwards, marking each
     * part as broken unless it looks like a well-formed quoted field on its own.
     */
    private static void fillBrokenColumns(String[] errorRow, BrokenCell brokenCell, String[] brokenParts) {
        for (int i = 0; i < brokenParts.length; i++) {
            String part = brokenParts[i];
            errorRow[brokenCell.column() + i] = i > 0 && isWellFormedField(part) ? unwrapQuotes(part)
                    : ITableFormat.PREVIEW_ERROR_CELL_MARKER + part;
        }
    }

    private static boolean isWellFormedField(String part) {
        return part.length() >= 2 && part.startsWith("\"") && part.endsWith("\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String unwrapQuotes(String part) {
        return part.substring(1, part.length() - 1);
    }

    /**
     * The column at which an unparsable line breaks, and the character index at which that
     * column's raw content starts.
     */
    private record BrokenCell(int column, int rawContentStart) {
    }
}
