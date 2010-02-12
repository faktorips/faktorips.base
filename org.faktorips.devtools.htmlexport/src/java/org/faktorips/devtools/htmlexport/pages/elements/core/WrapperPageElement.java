package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Set;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.WrapperType;

/**
 * HilfsElement, um Strukturen ohne eigenes Layout abzubilden z.B.
 * Tabellenzeilen und -zellen, Listenitems usw.
 * 
 * @author dicker
 * 
 */
public class WrapperPageElement extends AbstractCompositePageElement {
	WrapperType wrapperType;

	public WrapperPageElement(WrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}

	public WrapperPageElement(WrapperType wrapperType, PageElement... pageElements) {
		this(wrapperType, null, pageElements);
	}

	public WrapperPageElement(WrapperType wrapperType, Set<Style> styles, PageElement... pageElements) {
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

	public WrapperType getWrapperType() {
		return wrapperType;
	}

	public void setWrapperType(WrapperType wrapperType) {
		this.wrapperType = wrapperType;
	}
}
