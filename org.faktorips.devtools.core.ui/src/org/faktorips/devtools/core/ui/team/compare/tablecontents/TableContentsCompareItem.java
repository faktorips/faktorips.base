/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;

/**
 * A CompareItem for building a structure that represents a <code>ITableContents</code> object. The
 * <code>IIpsSrcFile</code>, the <code>ITableContents</code>, its generations and all contained rows
 * are each represented by a <code>TableContentsCompareItem</code>.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem
 * @author Stefan Widmaier
 */
public class TableContentsCompareItem extends AbstractCompareItem {
    private static final String IPSTABLECONTENTS_ELEMENT_TYPE = "ipstablecontentsElement"; //$NON-NLS-1$
    /**
     * Array containing the widths of all columns of the table counted in tabulators. The array has
     * a length of the tables' columnNumber plus one. The first entry defines the width of the
     * column of row numbers, the others contain the widths of the content-columns of the table.
     * Thus columnWidthsInTabs[2] contains the width of the second content column in tabs.
     */
    private int[] columnWidthsInTabs = null;

    /**
     * A row's content string separating the column values with a "|" character, and with all
     * whitespace removed. Used for hashCode() and equals().
     */
    private String rowContentStringColumnSeparated;

    /**
     * Creates an <code>TableContentsCompareItem</code> using the given parent and
     * <code>IIpsElement</code>. If the given parent is null, this
     * <code>TableContentsCompareItem</code> is marked as a root element, as indicated by the method
     * isRoot(). The given <code>IIpsElement</code> must not be <code>null</code>.
     * 
     * @param parent The parent of this <code>AbstractCompareItem</code>, or null if it is the root
     *            of a tree/structure.
     * @param content The referenced content. Must not be null.
     */
    public TableContentsCompareItem(AbstractCompareItem parent, IIpsElement content) {
        super(parent, content);
    }

    /**
     * A row is represented by its row number and all values of its columns separated by tabs. The
     * number of tabs needed to separate the values is calculated. This is necessary to align values
     * of different rows to columns.
     * <p>
     * A tableContent item is represented by it name and the attribute "tableStructure".
     * <p>
     * No text is created for CompareItems that represent generations. {@inheritDoc}
     */
    @Override
    protected String initContentString() {
        StringBuilder sb = new StringBuilder();
        if (getIpsElement() instanceof IRow) {
            IRow row = (IRow)getIpsElement();
            ITableContents table = (ITableContents)row.getIpsObject();
            int[] columnWidths = getColumnWidths();
            StringBuilder sbColSep = new StringBuilder();

            /*
             * Do not display Rownumber at the start of the line since textcompare/RangeDifferencing
             * needs to recognize rows of equal content, even if they have a different rownumber.
             */
            // String rowNumber= row.getRowNumber()+COLON_BLANK;
            // sb.append(rowNumber);
            // sb.append(getNeededTabs(columnWidths[0], rowNumber));
            for (int colCounter = 0; colCounter < table.getNumOfColumns(); colCounter++) {
                String value = getRowValueAt(row, colCounter);
                sb.append(value);
                sb.append(getNeededTabs(columnWidths[colCounter + 1], value));
                sb.append("\u007C "); //$NON-NLS-1$
                sbColSep.append(value);
                sbColSep.append('\u007C');
            }
            rowContentStringColumnSeparated = sbColSep.toString();
        } else if (getIpsElement() instanceof ITableContents) {
            ITableContents table = (ITableContents)getIpsElement();
            sb.append(Messages.TableContentsCompareItem_TableContents).append(COLON_BLANK);
            sb.append(QUOTE).append(table.getName()).append(QUOTE).append(NEWLINE);
            sb.append(TAB).append(Messages.TableContentsCompareItem_TableStructure).append(COLON_BLANK).append(QUOTE)
                    .append(table.getTableStructure()).append(QUOTE);
        } else if (getIpsElement() instanceof IIpsSrcFile) {
            sb.append(Messages.TableContentsCompareItem_SrcFile);
        }
        return sb.toString();
    }

    @Override
    protected String initContentStringWithoutWhiteSpace() {
        if (rowContentStringColumnSeparated != null) {
            return rowContentStringColumnSeparated;
        }
        return super.initContentStringWithoutWhiteSpace();
    }

    /**
     * Calculates the number of tabs needed to fill the column up with tabs, or in other words to
     * align the next value with its column.
     * 
     * @param widthInTabs The width of the column in tabs.
     * @param value The value of this table cell. Must not be null.
     * @return the number of tabs needed to reach the next column.
     */
    private StringBuilder getNeededTabs(int widthInTabs, String value) {
        StringBuilder sb = new StringBuilder();
        int neededTabs = widthInTabs - (value.length() + 2) / TableContentsCompareViewer.TAB_WIDTH;
        for (int tabCounter = 0; tabCounter < neededTabs; tabCounter++) {
            sb.append(TAB);
        }
        return sb;
    }

    @Override
    protected String initName() {
        StringBuilder sb = new StringBuilder();
        if (getIpsElement() instanceof IRow) {
            IRow row = (IRow)getIpsElement();
            // translate 0 based index to 1 based row number
            sb.append(Messages.TableContentsCompareItem_Row).append(COLON_BLANK).append(row.getRowNumber() + 1);
        } else if (getIpsElement() instanceof ITableRows) {
            sb.append(Messages.TableContentsCompareItem_rows);
        } else if (getIpsElement() instanceof ITableContents) {
            ITableContents table = (ITableContents)getIpsElement();
            sb.append(Messages.TableContentsCompareItem_TableContents).append(COLON_BLANK);
            sb.append(QUOTE).append(table.getName()).append(QUOTE);
        } else if (getIpsElement() instanceof IIpsSrcFile) {
            sb.append(Messages.TableContentsCompareItem_SrcFile);
        }
        return sb.toString();
    }

    /**
     * For the root element we return the file extension. For all other elements we return the a
     * constant that is also registered in the content merge viewer extension in attribute
     * 'extensions'. This seems to be ugly because the attribute 'extensions' is documented as
     * listing file name extensions, but JDT does the same for java elements.
     */
    @Override
    public String getType() {
        if (isRoot()) {
            return IpsObjectType.TABLE_CONTENTS.getFileExtension();
        } else {
            return IPSTABLECONTENTS_ELEMENT_TYPE;
        }
    }

    /**
     * Initializes the column width for all columns of all generations. {@inheritDoc}
     */
    @Override
    public void init() {
        if (isRoot()) {
            if (hasChildren()) {
                IIpsElement element = (getChildItems().get(0)).getIpsElement();
                if (element instanceof ITableContents) {
                    initColumnWidths((ITableContents)element);
                }
            }
        }
        super.init();
    }

    /**
     * Introduces a special compare for compareItems representing rows. Returns true only if both
     * compareitems contain <code>IRow</code> objects and if both rows have the same content String
     * representation.
     * <p>
     * If the compareitems do not contain rows, equals() returns the same value as the equals()
     * method in <code>AbstractCompareItem</code>.
     * 
     * @see AbstractCompareItem#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TableContentsCompareItem) {
            TableContentsCompareItem otherTableContentsCompareItem = (TableContentsCompareItem)o;
            if (getIpsElement() instanceof IRow && otherTableContentsCompareItem.getIpsElement() instanceof IRow) {
                return rowContentStringColumnSeparated
                        .equals(otherTableContentsCompareItem.rowContentStringColumnSeparated);
            }
        }
        return super.equals(o);
    }

    /**
     * If this compareItem contains an <code>IRow</code> as IpsElement, hashCode() returns the hash
     * code of {@link #getContentStringWithoutWhiteSpace()}. Otherwise returns the same value as
     * hashCode() in <code>AbstractCompareItem</code>. {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getIpsElement() instanceof IRow) {
            return rowContentStringColumnSeparated.hashCode();
        } else {
            return super.hashCode();
        }
    }

    /**
     * Calculates the widths of all columns in tabs by searching for the longest (String) value in
     * the table content. The column width is at least one tab greater than needed to fit in the
     * string value (number of characters).
     * <p>
     * This calculation is based on the base tabWidth (number of characters a tab is displayed as)
     * of the displaying textViewer. The tab width is set by the
     * <code>TableContentsCompareViewer</code> and can be accessed as a static field. @see
     * TableContentsCompareViewer#TAB_WIDTH
     * <p>
     * This method is called exactly once on the root TableContentsCompareItem.
     */
    private void initColumnWidths(ITableContents table) {
        columnWidthsInTabs = new int[table.getNumOfColumns() + 1];
        ITableRows tableRows = table.getTableRows();
        IRow[] rows = tableRows.getRows();
        for (int colCounter = 0; colCounter < table.getNumOfColumns(); colCounter++) {
            int maxWidth = 0;
            for (IRow row : rows) {
                maxWidth = Math.max(maxWidth, getRowValueAt(row, colCounter).length());
            }
            columnWidthsInTabs[colCounter + 1] = getColumnTabWidthForLength(maxWidth);
        }
        // calculate width of the rowNumber column
        String maxRowNumberString = String.valueOf(table.getNumOfColumns()) + COLON_BLANK;
        columnWidthsInTabs[0] = getColumnTabWidthForLength(maxRowNumberString.length());
    }

    /**
     * Returns the number of tabs needed to fit the given string length plus one.
     * <p>
     * Example: tab width is 4
     * <ul>
     * <li>length is 7 -> returns 3 (2 tabs to fit 7 characters + 1 additional tab)</li>
     * <li>length is 8 -> returns 3 (2 tabs to fit 8 characters exactly + 1 additional tab)</li>
     * <li>length is 9 -> returns 4 (3 tabs to fit 9 characters + 1 additional tab)</li>
     * </ul>
     * 
     * @return Tabs needed to fit this string and create space between columns.
     * 
     * @see TableContentsCompareViewer#TAB_WIDTH
     */
    private int getColumnTabWidthForLength(int stringLength) {
        int tabs = stringLength / TableContentsCompareViewer.TAB_WIDTH + 1;
        if (stringLength % TableContentsCompareViewer.TAB_WIDTH > 0) {
            tabs++;
        }
        return tabs;
    }

    /**
     * Returns an int array containing the widths of all columns in tabs. This includes the column
     * of row numbers, that are part of the string representation of the IpsObject.
     */
    public int[] getColumnWidths() {
        if (isRoot()) {
            return columnWidthsInTabs;
        } else {
            return ((TableContentsCompareItem)getParent()).getColumnWidths();
        }
    }

    /**
     * Returns the value at the given column index in the given row. If the value retrieved from the
     * row is null the NULL-representation string (defined by the IpsPreferences) is returned. This
     * method thus never returns <code>null</code>.
     * 
     * @param row The row a value should be retrieved from.
     * @param columnIndex The column index the value should be retrieved from inside the row.
     * @return The value at the given index in the given row or the NULL-representation string
     *             (defined by the IpsPreferences) if the row returned <code>null</code> as a value.
     */
    private String getRowValueAt(IRow row, int columnIndex) {
        String value = row.getValue(columnIndex);
        if (value == null) {
            value = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return value;
    }
}
