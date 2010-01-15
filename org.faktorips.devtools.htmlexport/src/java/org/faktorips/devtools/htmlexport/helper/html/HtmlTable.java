package org.faktorips.devtools.htmlexport.helper.html;

public class HtmlTable {
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
            if (isHeaderCell()) return "th";
            return "td";
        }
    }

    private int rowCount, columnCount;
    private HtmlTableCell[][] htmlTableCells;
    private String tableClasses;
    
    public HtmlTable(String[][] cellText, String tableClasses, String cellClasses) {
        this.tableClasses = tableClasses;
        initTable(cellText, cellClasses);
    }
    
    private void initTable(String[][] cellText, String cellClasses) {
        rowCount = cellText.length;
        
        if (rowCount == 0) return;

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
            if (columnCount != row.length && columnCount > -1) throw new IllegalArgumentException("Spaltenzahl stimmt nicht überein!");
            columnCount = row.length;
        }
    }
    
    public boolean isEmpty() {
        return rowCount == 0 || columnCount == 0;
    }
    
    public String generate() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlElementOpenTag("table", new HtmlAttribute("class", tableClasses)));
        for (int i = 0; i < htmlTableCells.length; i++) {
            generateRow(builder, i);
            
        }
        builder.append(HtmlUtil.createHtmlElementCloseTag("table"));
        return builder.toString();

    }

    private void generateRow(StringBuilder builder, int i) {
        builder.append(HtmlUtil.createHtmlElementOpenTag("tr", ""));
        for (HtmlTableCell cell : htmlTableCells[i]) {
            builder.append(cell.generate());
        }
        builder.append(HtmlUtil.createHtmlElementCloseTag("tr"));
    }
}
