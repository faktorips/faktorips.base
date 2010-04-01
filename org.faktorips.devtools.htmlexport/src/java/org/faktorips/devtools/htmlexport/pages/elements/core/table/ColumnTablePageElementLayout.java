package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * The {@link RowTablePageElementLayout} layouts table columns and adds specified {@link Style}s to the {@link TableCellPageElement}s of a column. 
 * @author dicker
 *
 */
public class ColumnTablePageElementLayout extends DefaultTablePageElementLayout {
	private int[] columns;
	private Style[] styles;

	/**
	 * adds the given {@link Style}s to all cells of the given columns
	 * @param columns
	 * @param styles
	 */
	public ColumnTablePageElementLayout(int[] columns, Style... styles) {
		this.columns = columns;
		this.styles = styles;
	}

	/**
	 * adds the given {@link Style}s to all cells of the given column
	 * @param column
	 * @param styles
	 */
	public ColumnTablePageElementLayout(int column, Style... styles) {
		this(new int[]{column}, styles);
	}
	
	protected boolean isRelatedColumn(int column) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == column) return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.table.DefaultTablePageElementLayout#layoutCell(int, int, org.faktorips.devtools.htmlexport.pages.elements.core.table.TableCellPageElement)
	 */
	@Override
	public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
		if (isRelatedColumn(column)) cellPageElement.addStyles(styles);
	}
}
