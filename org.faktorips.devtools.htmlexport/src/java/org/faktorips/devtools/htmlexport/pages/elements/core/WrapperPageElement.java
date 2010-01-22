package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.PageElementWrapperType;

/**
 * HilfsElement, um Strukturen ohne eigenes Layout abzubilden z.B.
 * Tabellenzeilen und -zellen, Listenitems usw.
 * 
 * @author dicker
 * 
 */
public class WrapperPageElement extends AbstractCompositePageElement {
	PageElementWrapperType wrapperType;

	public WrapperPageElement(PageElementWrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}

	public WrapperPageElement(PageElementWrapperType wrapperType, PageElement... pageElements) {
		this(wrapperType, null, pageElements);
	}

	public WrapperPageElement(PageElementWrapperType wrapperType, Set<Style> styles, PageElement... pageElements) {
		super();
		addPageElements(pageElements);
		if (styles != null) {
			addStyles(styles.toArray(new Style[styles.size()]));
		}
		this.wrapperType = wrapperType;
	}

	@Override
	public void build() {
	}

	@Override
	public void acceptLayouter(ILayouter layouter) {
		layouter.layoutWrapperPageElement(this);
	}

	public PageElementWrapperType getWrapperType() {
		return wrapperType;
	}

	public void setWrapperType(PageElementWrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}
}
