/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
