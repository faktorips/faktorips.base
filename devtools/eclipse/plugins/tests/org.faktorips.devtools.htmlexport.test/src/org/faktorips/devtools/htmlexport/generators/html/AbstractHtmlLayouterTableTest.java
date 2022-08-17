/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import org.faktorips.devtools.htmlexport.helper.html.AbstractTestHtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public abstract class AbstractHtmlLayouterTableTest extends AbstractTestHtmlLayouter {

    public AbstractHtmlLayouterTableTest() {
        super();
    }

    public AbstractHtmlLayouterTableTest(String name) {
        super(name);
    }

    protected TablePageElement createTable(int rows, int cols) {
        TablePageElement table = new TablePageElement(getContext());

        for (int row = 0; row < rows; row++) {
            IPageElement[] pageElements = new IPageElement[cols];
            for (int col = 0; col < cols; col++) {
                pageElements[col] = new TextPageElement(createCellContent(row, col), getContext());
            }
            table.addPageElements(new TableRowPageElement(pageElements, getContext()));
        }
        return table;
    }

    protected String createCellContent(int row, int col) {
        return "item " + row + "-" + col;
    }

}
