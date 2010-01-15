package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

public class TablePageElement extends AbstractCompositePageElement {

	private Set<TablePageElementLayout> tableLayouts = new HashSet<TablePageElementLayout>();

	public void acceptLayouter(ILayouter layoutVisitor) {
		layoutVisitor.layoutTablePageElement(this);
	}

	/**
	 * @throws ClassCastException
	 *             wenn PageElement nicht vom Typ {@link TableRowPageElement}
	 *             ist
	 * 
	 */
	@Override
	protected void addSubElement(PageElement pageElement) {
		TableRowPageElement rowPageElement = (TableRowPageElement) pageElement;
		if (hasBorder()) {
			rowPageElement.addStyles(Style.BORDER);
		}
		
		super.addSubElement(rowPageElement);
		rowPageElement.setTablePageElement(this);
	}

	public TablePageElement() {
		this(true);
	}

	public TablePageElement(boolean border) {
		super();
		setBorder(border);
	}

	@Override
	public void build() {
	}

	public boolean hasBorder() {
		return styles.contains(Style.BORDER);
	}

	public void setBorder(boolean border) {
		if (border) {
			styles.add(Style.BORDER);
			return;
		}
		styles.remove(Style.BORDER);
	}

	@Override
	public void visitSubElements(ILayouter layouter) {
		List<PageElement> subElements = getSubElements();
		for (int i = 0; i < subElements.size(); i++) {
			TableRowPageElement rowPageElement = (TableRowPageElement) subElements.get(i);
			for (TablePageElementLayout tableLayout : tableLayouts) {
				tableLayout.layoutRow(i, rowPageElement);
			}
			rowPageElement.build();
			rowPageElement.acceptLayouter(layouter);
		}
	}

	public Set<TablePageElementLayout> getLayouts() {
		return tableLayouts;
	}

	public void addLayouts(TablePageElementLayout... layouts) {
		tableLayouts.addAll(Arrays.asList(layouts));
	}

	public void removeLayouts(TablePageElementLayout... layouts) {
		tableLayouts.removeAll(Arrays.asList(layouts));
	}
}
