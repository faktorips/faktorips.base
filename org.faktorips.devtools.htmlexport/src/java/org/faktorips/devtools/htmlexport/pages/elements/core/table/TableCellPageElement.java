package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

public class TableCellPageElement extends WrapperPageElement {

	public TableCellPageElement(PageElement... pageElements) {
		super(LayouterWrapperType.TABLECELL, pageElements);
	}
	
	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutWrapperPageElement(this);
	}
}
