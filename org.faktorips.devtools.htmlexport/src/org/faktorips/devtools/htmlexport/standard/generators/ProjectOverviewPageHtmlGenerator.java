package org.faktorips.devtools.htmlexport.standard.generators;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.generators.AbstractTextGenerator;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;

public class ProjectOverviewPageHtmlGenerator extends AbstractTextGenerator {

    IIpsProject project;
    
    public ProjectOverviewPageHtmlGenerator(IIpsProject project) {
        this.project = project;
    }

    @Override
    public String generateText() {
        StringBuilder builder = new StringBuilder();
        builder.append(HtmlUtil.createHtmlHead("Project " + project.getName()));

        String head = HtmlUtil.createHtmlElement("h1", project.getName());

        builder.append(HtmlUtil.createHtmlElement("body", head));

        builder.append(HtmlUtil.createHtmlElementCloseTag("html"));
        return builder.toString();
    }

}
