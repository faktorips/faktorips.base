package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;

/**
 * HilfsElement, um Strukturen ohne eigenes Layout abzubilden z.B.
 * Tabellenzeilen und -zellen, Listenitems usw.
 * 
 * @author dicker
 * 
 */
public class WrapperPageElement extends AbstractCompositePageElement {
	LayouterWrapperType wrapperType;

	public WrapperPageElement(LayouterWrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}

	public WrapperPageElement(LayouterWrapperType wrapperType, PageElement... pageElements) {
		this(wrapperType, null, pageElements);
	}

	public WrapperPageElement(LayouterWrapperType wrapperType, Set<Style> styles, PageElement... pageElements) {
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

	public LayouterWrapperType getWrapperType() {
		return wrapperType;
	}

	public void setWrapperType(LayouterWrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}
}
