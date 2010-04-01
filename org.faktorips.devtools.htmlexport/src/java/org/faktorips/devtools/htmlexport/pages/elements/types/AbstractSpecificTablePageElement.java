package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.ColumnTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public abstract class AbstractSpecificTablePageElement extends TablePageElement implements DataPageElement {
	public AbstractSpecificTablePageElement() {
		super();
		addLayouts(new RowTablePageElementLayout(0, Style.TABLE_HEADLINE));
		addLayouts(new AlternateRowTablePageElementLayout(true));
	}

	@Override
	public void build() {
		addHeadline();
		addDataRows();
	}

	protected void addHeadline() {
		PageElement[] pageElements = PageElementUtils
				.createTextPageElements(getHeadline(), null, TextType.WITHOUT_TYPE);

		addSubElement(new TableRowPageElement(pageElements));
	}

	protected abstract List<String> getHeadline();

	protected abstract void addDataRows();

	/**
	 * keine eigenen hinzugefügten Elemente werden ignoriert
	 */
	@Override
	public AbstractCompositePageElement addPageElements(PageElement... pageElements) {
		throw new UnsupportedOperationException();
	}

	/**
	 * setzt die Uberschrift einer Spalte und gleichzeitig ein Layout für die Spalte
	 * @param headline
	 * @param item
	 * @param styles
	 */
	protected void addHeadlineAndColumnLayout(List<String> headline, String item, Style... styles) {
		addLayouts(new ColumnTablePageElementLayout(new int[]{headline.size()}, Style.CENTER));
		headline.add(item);
	}
}