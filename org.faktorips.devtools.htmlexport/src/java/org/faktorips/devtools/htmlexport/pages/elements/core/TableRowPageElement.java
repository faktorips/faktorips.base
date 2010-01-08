package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;

public class TableRowPageElement extends WrapperPageElement {
	
	
	public TableRowPageElement(PageElement[] pageElements) {
		super(LayouterWrapperType.TABLEROW, pageElements);
	}

	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutWrapperPageElement(this);
	}

    @Override
    public void visitSubElements(ILayouter layouter) {
        for (PageElement subElement : subElements) {
            layouter.layoutWrapperPageElement(new WrapperPageElement(LayouterWrapperType.TABLECELL, subElement));
        }
    }
}
