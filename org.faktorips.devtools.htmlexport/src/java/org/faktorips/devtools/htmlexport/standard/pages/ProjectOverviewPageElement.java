package org.faktorips.devtools.htmlexport.standard.pages;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public class ProjectOverviewPageElement extends RootPageElement {

    protected IIpsProject project;
    protected DocumentorConfiguration config;

    public ProjectOverviewPageElement(DocumentorConfiguration config) {
        this.project = config.getIpsProject();
        this.config = config;
        setTitle("Project " + project.getName());
    }

    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));
        
        addPageElements(new TextPageElement("Jetzt ne zündende Idee für die Projektübersicht!"));
        
        addPageElements(new MessageListTablePageElement(validateLinkedObjects()));
    }

	private MessageList validateLinkedObjects() {
		List<IIpsObject> linkedObjects = config.getLinkedObjects();
        MessageList ml = new MessageList();
        for (IIpsObject ipsObject : linkedObjects) {
			try {
				ml.add(ipsObject.validate(project));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return ml;
	}
}
