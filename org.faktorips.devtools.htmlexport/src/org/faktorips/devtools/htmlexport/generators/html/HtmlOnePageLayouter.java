/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import java.io.File;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;

/**
 * Layouter to put the whole documentation in one page
 * 
 * TODO still in developement
 * 
 * 
 * @author dicker
 */
public class HtmlOnePageLayouter extends HtmlLayouter {

    public HtmlOnePageLayouter(String resourcePath) {
        super(resourcePath);
    }

    @Override
    public void initRootPage(AbstractRootPageElement pageElement) {
        setPathToRoot(""); //$NON-NLS-1$
    }

    @Override
    public String createLinkBase(LinkPageElement pageElement) {
        return '.' + createInternalPath(pageElement.getPathFromRoot());
    }

    private String createInternalPath(String pathFromRoot) {
        return pathFromRoot.replace(File.separatorChar, '.');
    }

    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        initRootPage(pageElement);

        append("<hr />"); //$NON-NLS-1$

        if (pageElement.hasId()) {
            append("<a name=\"" + createInternalPath(pageElement.getId()) + "\">"); //$NON-NLS-1$//$NON-NLS-2$
        }

        visitSubElements(pageElement);
    }
}
