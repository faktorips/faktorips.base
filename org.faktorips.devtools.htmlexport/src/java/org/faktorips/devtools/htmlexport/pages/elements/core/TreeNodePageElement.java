package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * A {@link TreeNodePageElement} represents a node in a tree. The children are
 * shown within an indented {@link WrapperPageElement}.
 * 
 * @author dicker
 * 
 */
public class TreeNodePageElement extends WrapperPageElement {

	WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);

	/**
	 * creates a {@link TreeNodePageElement} with a PageElement as
	 * representation
	 * 
	 * @param pageElement
	 */
	public TreeNodePageElement(PageElement pageElement) {
		super(WrapperType.BLOCK);
		addSubElement(pageElement);

		wrapper.addStyles(Style.INDENTION);
		addSubElement(wrapper);
	}

	/**
	 * adds children to the node. 
	 */
	@Override
	public AbstractCompositePageElement addPageElements(PageElement... pageElements) {
		wrapper.addPageElements(pageElements);
		return this;
	}
}
