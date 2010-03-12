package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

/**
 * Set Styles, if
 * <ol>
 * <li>{@link TableCellPageElement} consists just of one {@link TextPageElement}
 * </li>
 * <li>the text of the {@link TextPageElement} matches the given regular
 * expression</li>
 * </ol>
 * 
 * e.g. RegexTablePageElementLayout(".{1,3}", Style.CENTER) shows every Cell
 * with maximum 3 characters centered
 * 
 * @author dicker
 * 
 */
public class RegexTablePageElementLayout extends DefaultTablePageElementLayout {
	private String regex;
	private Style[] styles;

	public RegexTablePageElementLayout(String regex, Style... styles) {
		super();
		this.regex = regex;
		this.styles = styles;
	}

	@Override
	public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
		if (cellIsMatching(cellPageElement))
			cellPageElement.addStyles(styles);
	}

	private boolean cellIsMatching(TableCellPageElement cellPageElement) {
		if (cellPageElement.getSubElements().size() != 1)
			return false;

		if (!(cellPageElement.getSubElements().get(0) instanceof TextPageElement))
			return false;

		TextPageElement element = (TextPageElement) cellPageElement.getSubElements().get(0);

		if (element.getText() == null)
			return false;

		return (element.getText().matches(regex));
	}

}
