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
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;

public class HtmlCompositePageElementLayouter extends AbstractHtmlPageElementLayouter<AbstractCompositePageElement> {

    public HtmlCompositePageElementLayouter(AbstractCompositePageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        WrapperType wrapperType = pageElement.getWrapperType();
        if (wrapperType == WrapperType.NONE && pageElement.getStylesCopy().isEmpty()) {
            layouter.visitSubElements(pageElement);
            return;
        }
        String wrappingElement = getHtmlElementByWrappingType(wrapperType);
        append(htmlUtil.createHtmlElementOpenTag(wrappingElement, pageElement.getId(), getClasses()));
        layouter.visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag(wrappingElement));
    }

    /**
     * returns name of the html-element for the given {@link WrapperType}
     * 
     */
    private String getHtmlElementByWrappingType(WrapperType wrapper) {
        if (wrapper == WrapperType.LISTITEM) {
            return "li"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.TABLEROW) {
            return "tr"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.TABLECELL) {
            return "td"; //$NON-NLS-1$
        }
        if (wrapper == WrapperType.BLOCK) {
            return "div"; //$NON-NLS-1$
        }
        return "span"; //$NON-NLS-1$
    }
}
