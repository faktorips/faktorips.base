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
import org.faktorips.devtools.htmlexport.helper.html.HtmlTextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

public class HtmlTextPageElementLayouter extends AbstractHtmlPageElementLayouter<TextPageElement> {

    public HtmlTextPageElementLayouter(TextPageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        if (pageElement.getType() == TextType.WITHOUT_TYPE && pageElement.getStylesCopy().isEmpty()) {
            append(htmlUtil.getHtmlText(pageElement.getText()));
            return;
        }
        append(htmlUtil.createHtmlElement(identifyTagName(pageElement), pageElement.getText(), getClasses()));

    }

    /**
     * returns the name of an html-element according to the given {@link TextPageElement}
     */
    private String identifyTagName(TextPageElement textPageElement) {
        return HtmlTextType.getHtmlTextTypeByTextType(textPageElement.getType()).getTagName();
    }
}
