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

package org.faktorips.devtools.htmlexport.helper.html;

/**
 * Helper for layouting tables
 * 
 * @author dicker
 * 
 */
public class HtmlTable {

    /**
     * represents a tablecell.
     * 
     * @author dicker
     * 
     */
    public class HtmlTableCell {
        private String text;
        private String classes;
        private boolean headerCell;

        private HtmlTableCell(String text, String classes, boolean headerCell) {
            this.text = text;
            this.classes = classes;
            this.headerCell = headerCell;
        }

        private HtmlTableCell(String text, String classes) {
            this(text, classes, false);
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getClasses() {
            return classes;
        }

        public void setClasses(String classes) {
            this.classes = classes;
        }

        public boolean isHeaderCell() {
            return headerCell;
        }

        public void setHeaderCell(boolean headerCell) {
            this.headerCell = headerCell;
        }

        public String generate() {
            return HtmlUtil.createHtmlElement(getElementName(), text, classes);
        }

        private String getElementName() {
            if (isHeaderCell()) {
                return "th"; //$NON-NLS-1$
            }
            return "td"; //$NON-NLS-1$
        }
    }

    private int rowCount, columnCount;
    private HtmlTableCell[][] htmlTableCells;
    private String tableClasses;

    /**
     * creates an {@link HtmlTable} with an 2-dimensional Array of Strings, the classes of table and
     * cells for the html-elements
     * 
     * @param cellText
     * @param tableClasses
     * @param cellClasses
     * @throws IllegalArgumentException if the number of columns differs
     * 
     */
    public HtmlTable(String[][] cellText, String tableClasses, String cellClasses) {
        this.tableClasses = tableClasses;

        rowCount = cellText.length;

        if (rowCount == 0) {
            return;
        }

        calculateColumnCount(cellText);

        initCells(cellText, cellClasses);
    }

    private void initCells(String[][] cellText, String cellClasses) {
        htmlTableCells = new HtmlTableCell[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                htmlTableCells[i][j] = new HtmlTableCell(cellText[i][j], cellClasses);
            }
        }
    }

    private void calculateColumnCount(String[][] cellText) {
        columnCount = -1;
        for (String[] row : cellText) {
            if (columnCount != row.length && columnCount > -1) {
                throw new IllegalArgumentException("The number of columns differ!"); //$NON-NLS-1$
            }
            columnCount = row.length;
        }
    }

    /**
     * @return true, if the table has no cells
     */
    public boolean isEmpty() {
        return rowCount == 0 || columnCount == 0;
    }

    /**
     * returns the complete html of the table
     * 
     * @return
     */
    public String generate() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlElementOpenTag("table", new HtmlAttribute("class", tableClasses))); //$NON-NLS-1$ //$NON-NLS-2$
        for (int i = 0; i < htmlTableCells.length; i++) {
            generateRow(builder, i);

        }
        builder.append(HtmlUtil.createHtmlElementCloseTag("table")); //$NON-NLS-1$
        return builder.toString();

    }

    private void generateRow(StringBuilder builder, int i) {
        builder.append(HtmlUtil.createHtmlElementOpenTag("tr", "")); //$NON-NLS-1$ //$NON-NLS-2$
        for (HtmlTableCell cell : htmlTableCells[i]) {
            builder.append(cell.generate());
        }
        builder.append(HtmlUtil.createHtmlElementCloseTag("tr")); //$NON-NLS-1$
    }
}
