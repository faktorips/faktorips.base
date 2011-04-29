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

import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public class HtmlTablePageElementLayouter extends AbstractHtmlPageElementLayouter<TablePageElement> {

    public HtmlTablePageElementLayouter(TablePageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        if (pageElement.isEmpty()) {
            return;
        }
        append(htmlUtil.createHtmlElementOpenTag("table", pageElement.getId(), getClasses())); //$NON-NLS-1$
        layouter.visitSubElements(pageElement);
        append(htmlUtil.createHtmlElementCloseTag("table")); //$NON-NLS-1$
    }

}
