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

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.htmlexport.generators.AbstractPageElementLayouter;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;

/**
 * 
 * @author dicker
 */
public abstract class AbstractHtmlPageElementLayouter<T extends IPageElement> extends AbstractPageElementLayouter<T> {

    protected final HtmlUtil htmlUtil = new HtmlUtil();
    protected final HtmlLayouter layouter;

    public AbstractHtmlPageElementLayouter(T pageElement, HtmlLayouter layouter) {
        super(pageElement);
        this.layouter = layouter;
    }

    @Override
    protected void setAnchor() {
        append(htmlUtil.createAnchor(getPageElement().getAnchor()));
    }

    protected void append(String text) {
        layouter.append(text);
    }

    protected String getClasses() {
        Set<Style> styles = pageElement.getStyles();
        if (styles == null || styles.isEmpty()) {
            return null;
        }

        return StringUtils.join(styles, ' ');
    }
}
