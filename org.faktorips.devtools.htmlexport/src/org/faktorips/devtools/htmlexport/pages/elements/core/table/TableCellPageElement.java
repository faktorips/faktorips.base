/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;

/**
 * {@link TableCellPageElement} represents a cell in a table
 * 
 * @author dicker
 * 
 */
public class TableCellPageElement extends WrapperPageElement {

    /**
     * creates a {@link TableCellPageElement} with the given {@link IPageElement}s as content of the
     * cell
     * 
     */
    public TableCellPageElement(IPageElement... pageElements) {
        super(WrapperType.TABLECELL, pageElements);
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }
}
