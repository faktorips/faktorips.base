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

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
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
     * @param context the current {@link DocumentationContext}
     * 
     */
    public TableCellPageElement(DocumentationContext context, IPageElement... pageElements) {
        super(WrapperType.TABLECELL, context, pageElements);
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutWrapperPageElement(this);
    }
}
