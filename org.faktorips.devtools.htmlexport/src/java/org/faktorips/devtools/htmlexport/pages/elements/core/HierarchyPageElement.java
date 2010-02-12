package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.WrapperType;

public class HierarchyPageElement extends WrapperPageElement {
	WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);

	public HierarchyPageElement(PageElement titlePageElement) {
		super(WrapperType.BLOCK);
		addSubElement(titlePageElement);

		wrapper.addStyles(Style.INDENTION);
		addSubElement(wrapper);
	}

	public HierarchyPageElement(PageElement titlePageElement, PageElement... pageElements) {
		this(titlePageElement);

		addPageElements(pageElements);
	}

	@Override
	public AbstractCompositePageElement addPageElements(PageElement... pageElements) {
		wrapper.addPageElements(pageElements);
		return this;
	}
}
