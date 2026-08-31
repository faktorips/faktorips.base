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
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;

public class HtmlRootPageElementLayouter extends AbstractHtmlPageElementLayouter<AbstractRootPageElement> {

    public HtmlRootPageElementLayouter(AbstractRootPageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        getLayouter().initRootPage(getPageElement());

        String title = getPageElement().getTitle() + " (" + getLayouter().getContext().getIpsProject().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        append(getHtmlUtil().createHtmlHead(title, getLayouter().getPathToRoot() + getLayouter().getStyleDefinitionPath(),
                getPageElement().isContentUnit()));

        getLayouter().visitSubElements(getPageElement());

        append(getHtmlUtil().createHtmlFoot());
    }

}
