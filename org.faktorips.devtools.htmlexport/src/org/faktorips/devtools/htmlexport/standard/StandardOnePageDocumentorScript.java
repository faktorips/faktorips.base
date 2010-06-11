package org.faktorips.devtools.htmlexport.standard;

import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardOnePageDocumentorScript implements IDocumentorScript {

    public void execute(DocumentorConfiguration config) {
        writeProjectOverviewPage(config);

        // TODO HIER WEITERMACHEN!!!;

        FileHandler.writeFile("complete.html", config.getLayouter().generate());
    }

    private void writeProjectOverviewPage(DocumentorConfiguration config) {
        AbstractRootPageElement projectOverviewHtml = new ProjectOverviewPageElement(config);
        createContent(config, projectOverviewHtml);
    }

    private void createContent(DocumentorConfiguration config, PageElement pageElement) {
        pageElement.build();
        ILayouter layouter = config.getLayouter();
        pageElement.acceptLayouter(layouter);
    }

}
