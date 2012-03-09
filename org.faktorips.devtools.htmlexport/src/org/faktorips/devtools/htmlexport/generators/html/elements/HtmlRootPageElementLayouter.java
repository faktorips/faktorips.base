/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
