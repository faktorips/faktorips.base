package org.faktorips.devtools.htmlexport.pages.elements.core.table;

public interface TablePageElementLayout {
	
	/**
	 * Layout für Zeile festlegen
	 * @param row
	 * @param rowPageElement
	 */
	public void layoutRow(int row, TableRowPageElement rowPageElement);

	/**
	 * Layout für Zelle festlegen
	 * @param row
	 * @param cell
	 * @param cellPageElement
	 */
	public void layoutCell(int row, int column, TableCellPageElement cellPageElement);
}
