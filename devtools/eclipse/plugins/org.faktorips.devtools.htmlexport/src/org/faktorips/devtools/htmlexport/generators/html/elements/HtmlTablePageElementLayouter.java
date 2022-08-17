/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public class HtmlTablePageElementLayouter extends AbstractHtmlPageElementLayouter<TablePageElement> {

    public HtmlTablePageElementLayouter(TablePageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        if (pageElement.isEmpty()) {
            return;
        }
        append(htmlUtil.createHtmlElementOpenTag("table", pageElement.getId(), getClasses())); //$NON-NLS-1$
        layouter.visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag("table")); //$NON-NLS-1$
    }

}
