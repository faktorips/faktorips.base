/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.standard.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public class ProjectOverviewPageElement extends AbstractRootPageElement {

    private static final String CREATION_TIME_DATE_FORMAT = "dd.MM.yyyy HH:mm"; //$NON-NLS-1$
    private DocumentationContext context;

    /**
     * a page for the overview of an IpsProject, which is defined in the context
     * 
     */
    public ProjectOverviewPageElement(DocumentationContext context) {
        this.context = context;
        setTitle(context.getMessage(HtmlExportMessages.ProjectOverviewPageElement_project)
                + " " + getProject().getName()); //$NON-NLS-1$
    }

    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_1));

        addPageElements(new TextPageElement(context.getMessage(HtmlExportMessages.ProjectOverviewPageElement_version)
                + ": " + getProject().getVersionProvider().getProjectVersion().asString())); //$NON-NLS-1$

        addIpsObjectPaths();

        if (getContext().showsValidationErrors()) {
            addValidationErrorsTable();
        }

        addCreationTime();
    }

    /**
     * adds creation time
     */
    private void addCreationTime() {
        IPageElement createCreationTime = new TextPageElement(
                context.getMessage(HtmlExportMessages.ProjectOverviewPageElement_created) + " " //$NON-NLS-1$
                + new SimpleDateFormat(CREATION_TIME_DATE_FORMAT).format(new Date()), TextType.BLOCK)
        .addStyles(Style.SMALL);
        addPageElements(createCreationTime);
    }

    /**
     * adds the paths of the IpsObjects
     */
    private void addIpsObjectPaths() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage(HtmlExportMessages.ProjectOverviewPageElement_paths), TextType.HEADING_2));
        IIpsObjectPath objectPath;
        try {
            objectPath = getProject().getIpsObjectPath();
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.ERROR, "Error getting IpsObjectPath of project", e)); //$NON-NLS-1$
            return;
        }

        wrapper.addPageElements(createArchiveEntriesList(objectPath));
        wrapper.addPageElements(createReferencedIpsProjectList(objectPath));
        wrapper.addPageElements(createReferencingIpsProjectList(objectPath));
        wrapper.addPageElements(createSourceFolders(objectPath));

        addPageElements(wrapper);
    }

    private IPageElement createArchiveEntriesList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage(HtmlExportMessages.ProjectOverviewPageElement_archiveEntries), TextType.HEADING_3));

        if (objectPath.getArchiveEntries().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage(HtmlExportMessages.ProjectOverviewPageElement_noArchiveEntries)));
        }
        ListPageElement archiveEntriesList = new ListPageElement();
        for (IIpsArchiveEntry ipsArchiveEntry : objectPath.getArchiveEntries()) {
            archiveEntriesList.addPageElements(new TextPageElement(ipsArchiveEntry.getIpsArchive().getArchivePath()
                    .toString()));
        }
        return wrapper.addPageElements(archiveEntriesList);
    }

    private IPageElement createReferencedIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage(HtmlExportMessages.ProjectOverviewPageElement_referencedProjects), TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage(HtmlExportMessages.ProjectOverviewPageElement_noReferencedProjects)));
        }

        List<String> referencedIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : objectPath.getReferencedIpsProjects()) {
            referencedIpsProjectsName.add(ipsProject.getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils()
        .createTextPageElements(referencedIpsProjectsName)));
        return wrapper.addPageElements(referencedProjects);
    }

    private IPageElement createReferencingIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage(HtmlExportMessages.ProjectOverviewPageElement_referencingProjects), TextType.HEADING_3));
        IIpsProject[] referencingProjectLeavesOrSelf;
        referencingProjectLeavesOrSelf = objectPath.getIpsProject().findReferencingProjectLeavesOrSelf();

        List<String> referencingIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : referencingProjectLeavesOrSelf) {
            if (getProject().equals(ipsProject)) {
                continue;
            }
            referencingIpsProjectsName.add(ipsProject.getName());
        }

        if (referencingIpsProjectsName.size() == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage(HtmlExportMessages.ProjectOverviewPageElement_noReferencingProjects)));
        }

        ListPageElement referencingProjects = new ListPageElement(Arrays.asList(new PageElementUtils()
        .createTextPageElements(referencingIpsProjectsName)));
        return wrapper.addPageElements(referencingProjects);
    }

    private IPageElement createSourceFolders(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage(HtmlExportMessages.ProjectOverviewPageElement_sourceFolder), TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage(HtmlExportMessages.ProjectOverviewPageElement_noSourceFolder)));
        }

        List<String> sourceFolder = new ArrayList<String>();
        for (IIpsSrcFolderEntry folderEntry : objectPath.getSourceFolderEntries()) {
            sourceFolder.add(folderEntry.getSourceFolder().getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils()
        .createTextPageElements(sourceFolder)));
        return wrapper.addPageElements(referencedProjects);
    }

    private void addValidationErrorsTable() {
        MessageListTablePageElement messageListTablePageElement = new MessageListTablePageElement(
                validateLinkedObjects(), getContext());
        if (messageListTablePageElement.isEmpty()) {
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new IPageElement[] {
                new TextPageElement(context.getMessage(HtmlExportMessages.ProjectOverviewPageElement_validationErros),
                        TextType.HEADING_2), messageListTablePageElement }));
    }

    private MessageList validateLinkedObjects() {
        List<IIpsSrcFile> srcFiles = getContext().getDocumentedSourceFiles();
        MessageList ml = new MessageList();
        for (IIpsSrcFile srcFile : srcFiles) {
            try {
                ml.add(srcFile.getIpsObject().validate(getProject()));
            } catch (CoreException e) {
                getContext().addStatus(new IpsStatus(IStatus.WARNING, "Error validating " + srcFile.getName(), e)); //$NON-NLS-1$
            }
        }
        return ml;
    }

    @Override
    public String getPathToRoot() {
        return HtmlPathFactory.createPathUtil(getProject()).getPathToRoot();
    }

    /**
     * returns the chosen IpsProject
     * 
     */
    protected IIpsProject getProject() {
        return getContext().getIpsProject();
    }

    /**
     * returns the context
     * 
     */
    protected DocumentationContext getContext() {
        return context;
    }

    @Override
    public boolean isContentUnit() {
        return true;
    }
}
