package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;

import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * {@link AbstractRootPageElement} is the abstract implementation of the root of the page
 * @author dicker
 *
 */
public abstract class AbstractRootPageElement extends AbstractCompositePageElement {

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#build()
	 */
	@Override
    public void build() {
        subElements = new ArrayList<PageElement>();
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement#acceptLayouter(org.faktorips.devtools.htmlexport.generators.ILayouter)
     */
    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }
    
    /**
     * @return path to the root (used for setting the right relative path in links from the page
     */
    public abstract String getPathToRoot();
}
