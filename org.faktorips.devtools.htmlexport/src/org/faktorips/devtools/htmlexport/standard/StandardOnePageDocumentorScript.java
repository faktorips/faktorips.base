package org.faktorips.devtools.htmlexport.standard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardOnePageDocumentorScript implements IDocumentorScript {

    @Override
    public void execute(DocumentorConfiguration config, IProgressMonitor monitor) {
        writeProjectOverviewPage(config);

        // TODO HIER WEITERMACHEN!!!;

        FileHandler.writeFile("complete.html", config.getLayouter().generate());
    }

    private void writeProjectOverviewPage(DocumentorConfiguration config) {
        AbstractPageElement projectOverviewHtml = new ProjectOverviewPageElement(config);
        createContent(config, projectOverviewHtml);
    }

    private void createContent(DocumentorConfiguration config, PageElement pageElement) {
        pageElement.build();
        ILayouter layouter = config.getLayouter();
        pageElement.acceptLayouter(layouter);
    }

}
