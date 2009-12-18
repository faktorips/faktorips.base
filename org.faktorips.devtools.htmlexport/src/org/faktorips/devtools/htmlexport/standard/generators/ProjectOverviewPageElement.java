package org.faktorips.devtools.htmlexport.standard.generators;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;

public class ProjectOverviewPageElement extends RootPageElement {

    IIpsProject project;

    public ProjectOverviewPageElement(IIpsProject project) {
        this.project = project;
        setTitle("Project " + project.getName());
    }

    @Override
    public void build() {
        super.build();
        addPageElement(new TextPageElement(getTitle(), TextType.HEADING_2));
        
        addPageElement(new TextPageElement("Jetzt ne zündende Idee für die Projektübersicht!"));
    }
}
