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
        if (getPageElement().hasStyle(Style.BLOCK)) {
            append(getHtmlUtil().createHtmlElementOpenTag("div")); //$NON-NLS-1$
        }
        append(getHtmlUtil().createLinkOpenTag(createLinkBase(), getPageElement().getLinkAnchor(), getPageElement()
                .getTarget().getId(), getClasses(), getPageElement().getTitle()));

        getLayouter().visitSubElements(getPageElement());

        append(getHtmlUtil().createHtmlElementCloseTag("a")); //$NON-NLS-1$
        if (getPageElement().hasStyle(Style.BLOCK)) {
            append(getHtmlUtil().createHtmlElementCloseTag("div")); //$NON-NLS-1$
        }
    }

    protected String createLinkBase() {
        return getLayouter().createLinkBase(getPageElement());
    }
}
