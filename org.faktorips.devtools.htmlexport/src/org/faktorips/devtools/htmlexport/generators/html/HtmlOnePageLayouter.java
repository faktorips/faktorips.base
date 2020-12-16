/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html;

import java.io.File;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
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

    public HtmlOnePageLayouter(DocumentationContext context, String resourcePath) {
        super(context, resourcePath);
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
