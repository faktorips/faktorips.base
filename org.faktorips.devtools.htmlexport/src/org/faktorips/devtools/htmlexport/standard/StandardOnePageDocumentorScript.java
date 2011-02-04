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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardOnePageDocumentorScript implements IDocumentorScript {

    private IoHandler fileHandler = new FileHandler();

    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) throws CoreException {
        writeProjectOverviewPage(context);

        // TODO HIER WEITERMACHEN!!!;

        try {
            fileHandler.writeFile("complete.html", context.getLayouter().generate()); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private void writeProjectOverviewPage(DocumentationContext context) {
        PageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        createContent(context, projectOverviewHtml);
    }

    private void createContent(DocumentationContext context, PageElement pageElement) {
        pageElement.build();
        ILayouter layouter = context.getLayouter();
        pageElement.acceptLayouter(layouter);
    }

}
