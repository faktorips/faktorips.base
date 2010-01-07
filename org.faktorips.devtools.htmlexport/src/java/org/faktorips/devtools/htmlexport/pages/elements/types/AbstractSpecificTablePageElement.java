package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public abstract class AbstractSpecificTablePageElement extends TablePageElement {

	public AbstractSpecificTablePageElement() {
		super();
	}

	@Override
	public void build() {
		addHeadline();
		addDataRows();
	}

	protected void addHeadline() {
		PageElement[] pageElements = PageElementUtils.createTextPageElements(getHeadline(), null, TextType.WITHOUT_TYPE); 
		
		subElements.add(new TableRowPageElement(pageElements));
	}

	protected abstract List<String> getHeadline();
	
	protected abstract void addDataRows();

	/**
	 * keine eigenen hinzugefügten Elemente werden ignoriert
	 */
	@Override
	public void addPageElements(PageElement... pageElements) {
		throw new UnsupportedOperationException();
	}

}