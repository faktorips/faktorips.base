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
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

public class HtmlLinkPageElementLayouter extends AbstractHtmlPageElementLayouter<LinkPageElement> {

    public HtmlLinkPageElementLayouter(LinkPageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(htmlUtil.createHtmlElementOpenTag("div")); //$NON-NLS-1$
        }
        append(htmlUtil.createLinkOpenTag(createLinkBase(), pageElement.getLinkAnchor(), pageElement.getTarget()
                .getId(), getClasses(), pageElement.getTitle()));

        layouter.visitSubElements(pageElement);

        append(htmlUtil.createHtmlElementCloseTag("a")); //$NON-NLS-1$
        if (pageElement.hasStyle(Style.BLOCK)) {
            append(htmlUtil.createHtmlElementCloseTag("div")); //$NON-NLS-1$
        }
    }

    protected String createLinkBase() {
        return layouter.createLinkBase(pageElement);
    }
}
