package org.faktorips.devtools.htmlexport.standard.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public class ProjectOverviewPageElement extends AbstractRootPageElement {

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

		addPageElements(createIpsObjectPaths());

		addMessageListTable();
		
		addPageElements(createCreationTime());
	}

	private PageElement createCreationTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		return new TextPageElement("Generiert am " + sdf.format(new Date()), TextType.BLOCK).addStyles(Style.SMALL);
	}

	private PageElement createIpsObjectPaths() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Pfade", TextType.HEADING_2));
		IIpsObjectPath objectPath;
		try {
			objectPath = project.getIpsObjectPath();

			wrapper.addPageElements(createArchiveEntriesList(objectPath));
			wrapper.addPageElements(createReferencedIpsProjectList(objectPath));
			wrapper.addPageElements(createReferencingIpsProjectList(objectPath));
			wrapper.addPageElements(createSourceFolders(objectPath));

		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		return wrapper;
	}

	private PageElement createArchiveEntriesList(IIpsObjectPath objectPath) {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Archive Entries", TextType.HEADING_3));

		if (objectPath.getArchiveEntries().length == 0)
			return wrapper.addPageElements(new TextPageElement("keine Archive Entries"));
		ListPageElement archiveEntriesList = new ListPageElement();
		for (IIpsArchiveEntry ipsArchiveEntry : objectPath.getArchiveEntries()) {
			archiveEntriesList.addPageElements(new TextPageElement(ipsArchiveEntry.getArchivePath().toString()));
		}
		return wrapper.addPageElements(archiveEntriesList);
	}

	private PageElement createReferencedIpsProjectList(IIpsObjectPath objectPath) {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Referenzierte Projekte", TextType.HEADING_3));
		if (objectPath.getReferencedIpsProjects().length == 0)
			return wrapper.addPageElements(new TextPageElement("keine referenzierten Projekte"));

		List<String> referencedIpsProjectsName = new ArrayList<String>();
		for (IIpsProject ipsProject : objectPath.getReferencedIpsProjects()) {
			referencedIpsProjectsName.add(ipsProject.getName());
		}
		ListPageElement referencedProjects = new ListPageElement(Arrays.asList(PageElementUtils
				.createTextPageElements(referencedIpsProjectsName)));
		return wrapper.addPageElements(referencedProjects);
	}

	private PageElement createReferencingIpsProjectList(IIpsObjectPath objectPath) {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Referenzierende Projekte", TextType.HEADING_3));
		IIpsProject[] referencingProjectLeavesOrSelf;
		try {
			referencingProjectLeavesOrSelf = objectPath.getIpsProject().findReferencingProjectLeavesOrSelf();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
		if (referencingProjectLeavesOrSelf.length == 0)
			return wrapper.addPageElements(new TextPageElement("keine referenzierenden Projekte"));

		List<String> referencedIpsProjectsName = new ArrayList<String>(referencingProjectLeavesOrSelf.length);
		for (IIpsProject ipsProject : referencingProjectLeavesOrSelf) {
			referencedIpsProjectsName.add(ipsProject.getName());
		}
		ListPageElement referencedProjects = new ListPageElement(Arrays.asList(PageElementUtils
				.createTextPageElements(referencedIpsProjectsName)));
		return wrapper.addPageElements(referencedProjects);
	}

	private PageElement createSourceFolders(IIpsObjectPath objectPath) {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Source Folder", TextType.HEADING_3));
		if (objectPath.getReferencedIpsProjects().length == 0)
			return wrapper.addPageElements(new TextPageElement("keine Source Folder"));

		List<String> sourceFolder = new ArrayList<String>();
		for (IIpsSrcFolderEntry folderEntry : objectPath.getSourceFolderEntries()) {
			sourceFolder.add(folderEntry.getSourceFolder().getName());
		}
		ListPageElement referencedProjects = new ListPageElement(Arrays.asList(PageElementUtils
				.createTextPageElements(sourceFolder)));
		return wrapper.addPageElements(referencedProjects);
	}

	private void addMessageListTable() {
		MessageListTablePageElement messageListTablePageElement = new MessageListTablePageElement(
				validateLinkedObjects());
		if (messageListTablePageElement.isEmpty())
			return;
		addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
				new TextPageElement("Validierungsfehler", TextType.HEADING_2), messageListTablePageElement }));
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

	@Override
	public String getPathToRoot() {
		return PathUtilFactory.createPathUtil(project).getPathToRoot();
	}

}
