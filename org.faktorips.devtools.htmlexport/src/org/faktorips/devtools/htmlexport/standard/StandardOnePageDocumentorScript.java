/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.standard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class StandardOnePageDocumentorScript implements IDocumentorScript {

    private IoHandler fileHandler = new FileHandler();

    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) throws CoreRuntimeException {
        writeProjectOverviewPage(context);

        // TODO HIER WEITERMACHEN!!!;

        try {
            fileHandler.writeFile("complete.html", context.getLayouter().generate()); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new CoreRuntimeException(new IpsStatus(e));
        } catch (IOException e) {
            throw new CoreRuntimeException(new IpsStatus(e));
        }
    }

    private void writeProjectOverviewPage(DocumentationContext context) {
        IPageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        createContent(context, projectOverviewHtml);
    }

    private void createContent(DocumentationContext context, IPageElement pageElement) {
        pageElement.build();
        ILayouter layouter = context.getLayouter();
        pageElement.acceptLayouter(layouter);
    }

}
