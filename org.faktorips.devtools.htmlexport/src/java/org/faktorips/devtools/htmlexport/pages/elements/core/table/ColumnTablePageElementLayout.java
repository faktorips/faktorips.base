package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

public class ColumnTablePageElementLayout extends DefaultTablePageElementLayout {
	int[] columns;
	Style[] styles;

	public ColumnTablePageElementLayout(int column, Style... styles) {
		setColumns(column);
		setStyles(styles);
	}
	
	public ColumnTablePageElementLayout(int[] columns, Style... styles) {
		setColumns(columns);
		setStyles(styles);
	}

	protected boolean isRelatedColumn(int column) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == column) return true;
		}
		return false;
	}
	
	
	@Override
	public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
		if (isRelatedColumn(column)) cellPageElement.addStyles(styles);
	}
	
	private void setColumns(int... columns) {
		this.columns = columns;
	}

	private void setStyles(Style... styles) {
		this.styles = styles;
	}
}
