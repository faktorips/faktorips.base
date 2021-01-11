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
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;

public class HtmlListPageElementLayouter extends AbstractHtmlPageElementLayouter<ListPageElement> {

    public HtmlListPageElementLayouter(ListPageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        if (pageElement.isEmpty()) {
            return;
        }

        String listBaseHtmlTag = pageElement.isOrdered() ? "ol" : "ul"; //$NON-NLS-1$ //$NON-NLS-2$
        append(htmlUtil.createHtmlElementOpenTag(listBaseHtmlTag, pageElement.getId(), getClasses()));

        layouter.visitSubElements(pageElement);

        append(htmlUtil.createHtmlElementCloseTag(listBaseHtmlTag));
    }

}
