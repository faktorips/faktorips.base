package org.faktorips.devtools.htmlexport;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public abstract class AbstractDocumentorScript implements IDocumentorScript {

    protected IIpsProject project;

    public IIpsProject getProject() {
        return project;
    }

    public void setProject(IIpsProject project) {
        this.project = project;
    }
}