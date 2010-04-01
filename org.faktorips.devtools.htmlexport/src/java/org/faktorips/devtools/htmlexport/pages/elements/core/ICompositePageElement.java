package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * An {@link ICompositePageElement} contains other PageElements
 * @author dicker
 *
 */
public interface ICompositePageElement extends PageElement {
	
	/**
	 * lets the {@link ILayouter} visit the subElements
	 * @param layouter
	 */
	public void visitSubElements(ILayouter layouter);

	/**
	 * adds one or more {@link PageElement}s
	 * @param pageElements
	 * @return this
	 */
	public ICompositePageElement addPageElements(PageElement... pageElements);
}
