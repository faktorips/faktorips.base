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
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;

public class HtmlCompositePageElementLayouter extends AbstractHtmlPageElementLayouter<AbstractCompositePageElement> {

    public HtmlCompositePageElementLayouter(AbstractCompositePageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        WrapperType wrapperType = pageElement.getWrapperType();
        if (wrapperType == WrapperType.NONE && pageElement.getStyles().isEmpty()) {
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
