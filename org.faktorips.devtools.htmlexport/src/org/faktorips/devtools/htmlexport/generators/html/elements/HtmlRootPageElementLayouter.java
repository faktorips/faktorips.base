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
        layouter.initRootPage(pageElement);

        String title = pageElement.getTitle() + " (" + layouter.getContext().getIpsProject().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        append(htmlUtil.createHtmlHead(title, layouter.getPathToRoot() + layouter.getStyleDefinitionPath(),
                pageElement.isContentUnit()));

        layouter.visitSubElements(pageElement);

        append(htmlUtil.createHtmlFoot());
    }

}
