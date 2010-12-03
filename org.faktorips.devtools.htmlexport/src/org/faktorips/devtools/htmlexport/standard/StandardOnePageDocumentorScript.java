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

package org.faktorips.devtools.htmlexport.standard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardOnePageDocumentorScript implements IDocumentorScript {

    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) {
        writeProjectOverviewPage(context);

        // TODO HIER WEITERMACHEN!!!;

        FileHandler.writeFile("complete.html", context.getLayouter().generate()); //$NON-NLS-1$
    }

    private void writeProjectOverviewPage(DocumentationContext context) {
        AbstractPageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        createContent(context, projectOverviewHtml);
    }

    private void createContent(DocumentationContext context, PageElement pageElement) {
        pageElement.build();
        ILayouter layouter = context.getLayouter();
        pageElement.acceptLayouter(layouter);
    }

}
