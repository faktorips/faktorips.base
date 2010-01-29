package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;

public class HierarchyPageElement extends WrapperPageElement {
	WrapperPageElement wrapper = new WrapperPageElement(PageElementWrapperType.BLOCK);

	public HierarchyPageElement(PageElement titlePageElement) {
		super(PageElementWrapperType.BLOCK);
		addSubElement(titlePageElement);

		wrapper.addStyles(Style.INDENTION);
		addSubElement(wrapper);
	}

	public HierarchyPageElement(PageElement titlePageElement, PageElement... pageElements) {
		this(titlePageElement);

		addPageElements(pageElements);
	}

	@Override
	public void addPageElements(PageElement... pageElements) {
		wrapper.addPageElements(pageElements);
	}
}
