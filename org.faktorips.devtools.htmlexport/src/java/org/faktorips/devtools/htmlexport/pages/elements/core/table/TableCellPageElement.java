package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

/**
 * {@link TableCellPageElement} represents a cell in a table
 * @author dicker
 *
 */
public class TableCellPageElement extends WrapperPageElement {

	/**
	 * creates a {@link TableCellPageElement} with the given {@link PageElement}s as content
	 * @param pageElements
	 */
	public TableCellPageElement(PageElement... pageElements) {
		super(WrapperType.TABLECELL, pageElements);
	}
	
	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement#acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
	 */
	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutWrapperPageElement(this);
	}
}
