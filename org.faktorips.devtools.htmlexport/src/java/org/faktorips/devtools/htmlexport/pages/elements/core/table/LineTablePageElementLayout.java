package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

public class LineTablePageElementLayout extends DefaultTablePageElementLayout {
	int[] rows;
	Style[] styles;
	
	public LineTablePageElementLayout(int[] rows, Style... styles) {
		setRows(rows);
		setStyles(styles);
	}

	public LineTablePageElementLayout(int row, Style... styles) {
		setRows(row);
		setStyles(styles);
	}

	@Override
	public void layoutRow(int row, TableRowPageElement rowPageElement) {
		if (isRelatedRow(row)) rowPageElement.addStyles(styles);
	}

	protected boolean isRelatedRow(int row) {
		for (int i = 0; i < rows.length; i++) {
			if (rows[i] == row) return true;
		}
		return false;
	}
	
	private void setRows(int... rows) {
		this.rows = rows;
	}

	private void setStyles(Style... styles) {
		this.styles = styles;
	}

}
