/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.htmlexport.generators.html;

import java.io.File;

import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;

public class HtmlOnePageLayouter extends HtmlLayouter {

    public HtmlOnePageLayouter(String resourcePath) {
        super(resourcePath);
    }

    @Override
    protected void initRootPage(AbstractRootPageElement pageElement) {
        setPathToRoot("");
    }

    @Override
    String createLinkBase(LinkPageElement pageElement) {
        return '.' + createInternalPath(pageElement.getPathFromRoot());
    }

    /**
     * @param pageElement
     * @return
     */
    private String createInternalPath(String pathFromRoot) {
        return pathFromRoot.replace(File.separatorChar, '.');
    }

    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        initRootPage(pageElement);

        append("<hr />");

        if (pageElement.hasId()) {
            append("<a name=\"" + createInternalPath(pageElement.getId()) + "\">");
        }

        visitSubElements(pageElement);
    }
}
