/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
        append(htmlUtil.createLinkOpenTag(createLinkBase(), pageElement.getLinkAnchor(), pageElement.getTarget(),
                getClasses(), pageElement.getTitle()));

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
