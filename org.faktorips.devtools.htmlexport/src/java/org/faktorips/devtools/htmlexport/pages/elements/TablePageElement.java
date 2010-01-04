package org.faktorips.devtools.htmlexport.pages.elements;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class TablePageElement extends AbstractCompositePageElement {

    public void acceptLayouter(ILayouter layoutVisitor) {
    	layoutVisitor.layoutTablePageElement(this);
    }

	/**
	 * nur {@link TableRowPageElement} wird als subelement akzeptiert
	 */
    @Override
	protected void checkPageElementType(PageElement pageElement) {
		if (! (pageElement instanceof TableRowPageElement)) throw new ClassCastException("Nur TableRowPageElement wird akzeptiert.");
	}

	@Override
	public void build() {}
}
