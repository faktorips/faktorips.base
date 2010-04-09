package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.AlternateRowTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.RegexTablePageElementLayout;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class KeyValueTablePageElement extends TablePageElement {

	public KeyValueTablePageElement() {
		super(true);
		addLayouts(new AlternateRowTablePageElementLayout(false));
		addLayouts(new RegexTablePageElementLayout(".{1,3}", Style.CENTER)); //$NON-NLS-1$
	}

	/**
	 * Adds Rows to the {@link KeyValueTablePageElement}. One Row consists of two {@link PageElement}s.  
	 * @param an array of {@link PageElement}
	 * @throws IllegalArgumentException if the number of elements is uneven
	 * @return a reference of this
	 */
	@Override
	public KeyValueTablePageElement addPageElements(PageElement... pageElements) {
		if (pageElements.length % 2 == 1) throw new IllegalArgumentException(Messages.KeyValueTablePageElement_justEvenNumberOfPageElementsAllowed);
		if (pageElements.length == 0) return this;

		for (int i = 0; i < pageElements.length; i+=2) {
			addKeyValueRow(pageElements[i], pageElements[i + 1]);
		}
		
		return this;
	}

	/**
	 * adds a row to the table
	 * @param key
	 * @param value
	 * @return this
	 */
	public KeyValueTablePageElement addKeyValueRow(String key, String value) {
		return addKeyValueRow(new TextPageElement(key), new TextPageElement(value));
	}

	/**
	 * adds a row to the table
	 * @param key
	 * @param valuePageElement
	 * @return
	 */
	public KeyValueTablePageElement addKeyValueRow(String key, PageElement valuePageElement) {
		return addKeyValueRow(new TextPageElement(key), valuePageElement);
	}
		
	/**
	 * adds a row to the table
	 * @param keyPageElement
	 * @param valuePageElement
	 * @return
	 */
	public KeyValueTablePageElement addKeyValueRow(PageElement keyPageElement, PageElement valuePageElement) {
		addSubElement(new TableRowPageElement(new PageElement[]{keyPageElement, valuePageElement}));
		return this;
	}

}
