package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;


public class AlternateTablePageElementLayout extends DefaultTablePageElementLayout {
	boolean ignoreFirstLine;
	
	public AlternateTablePageElementLayout(boolean ignoreFirstLine) {
		super();
		this.ignoreFirstLine = ignoreFirstLine;
	}

	@Override
	public void layoutRow(int row, TableRowPageElement rowPageElement) {
		if (ignoreFirstLine && row == 0) return;
		rowPageElement.addStyles(getStyle(row));
	}

	private Style getStyle(int row) {
		if (row % 2 == 0 && ignoreFirstLine) return Style.TABLE_ROW_EVEN;
		if (row % 2 == 1 && !ignoreFirstLine) return Style.TABLE_ROW_EVEN;
		return Style.TABLE_ROW_UNEVEN;
	}
}
