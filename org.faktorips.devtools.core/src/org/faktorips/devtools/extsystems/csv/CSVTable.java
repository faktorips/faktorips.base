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

package org.faktorips.devtools.extsystems.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.internal.ui.preferences.formatter.LineWrappingTabPage;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContentsGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;

import com.ibm.icu.text.SimpleDateFormat;

// TODO: refactor, javadoc
// TODO: ctor signature CSVTable(XXX encoding, Character|char fieldDelimiter, -"- textDelimiter)
// TODO: trim
public class CSVTable {

    private final static String NEW_LINE = "\r\n";
    
    private String fieldDelimiter = CSVLineParser.DEFAULT_FIELD_DELIMITER;
    private String textDelimiter = CSVLineParser.DEFAULT_TEXT_DELIMITER;
    
    
    private HashMap rows = new HashMap();
    private int numColumns;

    
    /**
     * Constructor for testing only.
     */
    public CSVTable(int numColumns) {
        this.numColumns = numColumns;
        // TODO: forward to delimiter ctor
    }

    public CSVTable(FileInputStream fis) throws IOException {
        this(fis, CSVLineParser.DEFAULT_FIELD_DELIMITER, CSVLineParser.DEFAULT_TEXT_DELIMITER);
    }

    public CSVTable(FileInputStream fis, String fieldDelimiter, String textDelimiter) throws IOException {
        if (fieldDelimiter.equals(textDelimiter)) {
            throw new IllegalArgumentException("Text and field delimiters must not be equal.");
        }
        this.fieldDelimiter = fieldDelimiter;
        this.textDelimiter = textDelimiter;
        
        createFromInputStream(fis);
    }
    
    // TODO: remove
    private void createFromInputStream(InputStream is) throws IOException {
        throw new NotImplementedException();
    }

    public CSVTableRow createRow(int rowIndex) {
        CSVTableRow row = new CSVTableRow(this);
        rows.put(rowIndex, row);
        return row;
    }

    public CSVTableRow getRow(int index) {
        return (CSVTableRow)rows.get(index);
    }

    public void saveToFile(OutputStream fos, boolean exportColumnHeaderRow) {
        PrintWriter writer = new PrintWriter(fos, false);

        
        if (exportColumnHeaderRow) {
            // TODO: write header line properly
            writer.write("HEADER" + NEW_LINE);
        }
        
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            CSVTableRow row = (CSVTableRow)rows.get(rowIndex);
            
            // FIXME: here, use size of columns from tablestructure
            for (short i = 0; i < getNumColumns(); i++) {

                if (i > 0) {
                    writer.write(fieldDelimiter);
                    writer.write(" "); // TODO: remove
                }
                
                // FIXME: determine datatype of cell and use according converter for field serialization
                Object value = row.getValue(i);

                // Special treatment of Date class
                if (value instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    value = dateFormat.format((Date) value);
                }

                writer.write(textDelimiter);
                writer.write(value.toString());
                writer.write(textDelimiter);
            }
            
            writer.write(NEW_LINE);
        }
        writer.close();
    }
    
    public int getNumColumns() {
        return this.numColumns;
    }
    
    private void setNumColumns(int width) {
        this.numColumns = width;
    }

    public String toString() {
        String s = "[CSVTable(rows=" + rows.size() + "):";
        for (int i = 0; i < rows.size(); i++) {
            s += ((CSVTableRow)rows.get(i));
        }
        return s + "]";
    }
    
    public class CSVTableRow {

        private ArrayList cells = new ArrayList();
        private final CSVTable csv;
        
        public CSVTableRow(CSVTable csv) {
            this.csv = csv;
            for (int i = 0; i < csv.getNumColumns(); i++) {
                cells.add("");
            }
        }

        public void setValue(short column, Object o) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, o);
        }
        
        public void setValue(short column, boolean b) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, b);
        }
        
        public void setValue(short column, double d) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, d);
        }
        
        public void setValue(short column, int i) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, i);
        }
        
        public void setValue(short column, long l) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, l);
        }
        
        public void setValue(short column, Date date) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, date);
        }
        
        public void setValue(short column, String s) {
            csv.setNumColumns(Math.max(csv.getNumColumns(), column + 1));
            cells.set(column, s);
        }

//        public void createColumn(short insertAfter) {
//            int currentSize = cells.size();
//            if (insertAfter < currentSize) {
//                cells.add(insertAfter, "");
//            } else {
//                cells.add("");
//            }
//            csv.setNumColumns(Math.max(csv.getNumColumns(), cells.size()));
//        }
        
        public Object getValue(short column) {
            return cells.get(column);
        }

        public String toString() {
            return "[cells=" + cells.size() + "]";
        }
        
        
    }

}
