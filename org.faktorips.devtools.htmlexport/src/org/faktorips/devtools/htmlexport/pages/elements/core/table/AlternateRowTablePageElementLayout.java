package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * The {@link AlternateRowTablePageElementLayout} layouts the even and uneven rows of a table with
 * different {@link Style}s
 * 
 * @author dicker
 * 
 */
public class AlternateRowTablePageElementLayout extends DefaultTablePageElementLayout {

    /**
     * true if first row should be ignored
     */
    protected boolean ignoreFirstRow;

    /**
     * creates an {@link AlternateRowTablePageElementLayout}
     * 
     * @param ignoreFirstRow
     */
    public AlternateRowTablePageElementLayout(boolean ignoreFirstRow) {
        super();
        this.ignoreFirstRow = ignoreFirstRow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.pages.elements.core.table.DefaultTablePageElementLayout
     * #layoutRow(int,
     * org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement)
     */
    @Override
    public void layoutRow(int row, TableRowPageElement rowPageElement) {
        if (ignoreFirstRow && row == 0) {
            return;
        }
        rowPageElement.addStyles(getStyle(row));
    }

    // TODO change algorithm: use ignoreFirstRow to calculate even or uneven
    private Style getStyle(int row) {
        if (row % 2 == 0) {
            return Style.TABLE_ROW_EVEN;
        }
        return Style.TABLE_ROW_UNEVEN;
    }
}
