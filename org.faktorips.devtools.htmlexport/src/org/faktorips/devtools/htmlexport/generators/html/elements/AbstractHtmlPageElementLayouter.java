/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
