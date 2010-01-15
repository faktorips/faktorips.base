package org.faktorips.devtools.htmlexport.standard.pages;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
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
		addPageElements(new TextPageElement(getTitle(), TextType.HEADING_1));

		addPageElements(new TextPageElement("Jetzt ne zündende Idee für die Projektübersicht!"));

		addPageElements(new WrapperPageElement(LayouterWrapperType.BLOCK,
				new PageElement[] { new TextPageElement("Validierungsfehler", TextType.HEADING_2), new MessageListTablePageElement(validateLinkedObjects()) }));
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
