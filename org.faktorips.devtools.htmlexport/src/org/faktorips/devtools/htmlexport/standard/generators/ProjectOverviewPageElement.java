package org.faktorips.devtools.htmlexport.standard.generators;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;

public class ProjectOverviewPageElement extends RootPageElement {

    IIpsProject project;
    
    public ProjectOverviewPageElement(IIpsProject project) {
        this.project = project;
    }

    public String generateText() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlHead("Project " + project.getName()));

        String head = HtmlUtil.createHtmlElement("h1", project.getName());

        builder.append(HtmlUtil.createHtmlElement("body", head));

        builder.append(HtmlUtil.createHtmlElementCloseTag("html"));
        return builder.toString();
    }

}
