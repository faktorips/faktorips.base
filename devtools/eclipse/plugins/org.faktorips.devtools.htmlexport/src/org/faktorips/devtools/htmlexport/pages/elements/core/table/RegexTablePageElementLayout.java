/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

/**
 * The {@link RegexTablePageElementLayout} uses a regular expression to layout a cell.
 * <p>
 * Set given {@link Style}s, if
 * <ol>
 * <li>{@link TableCellPageElement} consists just of one {@link TextPageElement}</li>
 * <li>the text of the {@link TextPageElement} matches the given regular expression</li>
 * </ol>
 * <p>
 * e.g. RegexTablePageElementLayout(".{1,3}", Style.CENTER) centers every cell with maximum 3
 * characters
 * 
 * @author dicker
 * 
 */
public class RegexTablePageElementLayout extends DefaultTablePageElementLayout {
    private String regex;
    private Style[] styles;

    /**
     * creates a {@link RegexTablePageElementLayout} with the pattern regex and adds the given
     * {@link Style}s to the matching cells
     * 
     */
    public RegexTablePageElementLayout(String regex, Style... styles) {
        super();
        this.regex = regex;
        this.styles = styles;
    }

    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
        if (cellMatches(cellPageElement)) {
            cellPageElement.addStyles(styles);
        }
    }

    private boolean cellMatches(TableCellPageElement cellPageElement) {
        if ((cellPageElement.size() != 1) || !(cellPageElement.getSubElement(0) instanceof TextPageElement)) {
            return false;
        }

        TextPageElement element = (TextPageElement)cellPageElement.getSubElement(0);

        if (element.getText() == null) {
            return false;
        }

        return (element.getText().matches(regex));
    }
}
