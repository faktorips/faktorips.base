/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.MessageList;

public class ProjectOverviewPageElement extends AbstractRootPageElement {

    private static final String CREATION_TIME_DATE_FORMAT = "dd.MM.yyyy HH:mm"; //$NON-NLS-1$

    /**
     * a page for the overview of an IpsProject, which is defined in the context
     * 
     */
    public ProjectOverviewPageElement(DocumentationContext context) {
        super(context);
        setTitle(context.getMessage(HtmlExportMessages.ProjectOverviewPageElement_project)
                + " " + getProject().getName()); //$NON-NLS-1$
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_1, getContext()));

        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_version)
                + ": " + getProject().getVersionProvider().getProjectVersion().asString(), getContext())); //$NON-NLS-1$

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
        IPageElement createCreationTime = new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_created)
                + " " //$NON-NLS-1$
                + new SimpleDateFormat(CREATION_TIME_DATE_FORMAT).format(new Date()), TextType.BLOCK, getContext())
                        .addStyles(Style.SMALL);
        addPageElements(createCreationTime);
    }

    /**
     * adds the paths of the IpsObjects
     */
    private void addIpsObjectPaths() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_paths), TextType.HEADING_2, getContext()));
        IIpsObjectPath objectPath;
        objectPath = getProject().getIpsObjectPath();

        wrapper.addPageElements(createArchiveEntriesList(objectPath));
        wrapper.addPageElements(createReferencedIpsProjectList(objectPath));
        wrapper.addPageElements(createReferencingIpsProjectList(objectPath));
        wrapper.addPageElements(createSourceFolders(objectPath));

        addPageElements(wrapper);
    }

    private IPageElement createArchiveEntriesList(IIpsObjectPath objectPath) {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_archiveEntries), TextType.HEADING_3, getContext()));

        if (objectPath.getArchiveEntries().length == 0) {
            return wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.ProjectOverviewPageElement_noArchiveEntries), getContext()));
        }
        ListPageElement archiveEntriesList = new ListPageElement(getContext());
        for (IIpsArchiveEntry ipsArchiveEntry : objectPath.getArchiveEntries()) {
            archiveEntriesList.addPageElements(new TextPageElement(ipsArchiveEntry.getIpsArchive().getArchivePath()
                    .toString(), getContext()));
        }
        return wrapper.addPageElements(archiveEntriesList);
    }

    private IPageElement createReferencedIpsProjectList(IIpsObjectPath objectPath) {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_referencedProjects), TextType.HEADING_3, getContext()));
        if (objectPath.getDirectlyReferencedIpsProjects().size() == 0) {
            return wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.ProjectOverviewPageElement_noReferencedProjects), getContext()));
        }

        List<String> referencedIpsProjectsName = new ArrayList<>();
        for (IIpsProject ipsProject : objectPath.getDirectlyReferencedIpsProjects()) {
            referencedIpsProjectsName.add(ipsProject.getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils(getContext())
                .createTextPageElements(referencedIpsProjectsName)), getContext());
        return wrapper.addPageElements(referencedProjects);
    }

    private IPageElement createReferencingIpsProjectList(IIpsObjectPath objectPath) {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_referencingProjects), TextType.HEADING_3, getContext()));
        IIpsProject[] referencingProjectLeavesOrSelf;
        referencingProjectLeavesOrSelf = objectPath.getIpsProject().findReferencingProjectLeavesOrSelf();

        List<String> referencingIpsProjectsName = new ArrayList<>();
        for (IIpsProject ipsProject : referencingProjectLeavesOrSelf) {
            if (getProject().equals(ipsProject)) {
                continue;
            }
            referencingIpsProjectsName.add(ipsProject.getName());
        }

        if (referencingIpsProjectsName.size() == 0) {
            return wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.ProjectOverviewPageElement_noReferencingProjects), getContext()));
        }

        ListPageElement referencingProjects = new ListPageElement(Arrays.asList(new PageElementUtils(getContext())
                .createTextPageElements(referencingIpsProjectsName)), getContext());
        return wrapper.addPageElements(referencingProjects);
    }

    private IPageElement createSourceFolders(IIpsObjectPath objectPath) {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.ProjectOverviewPageElement_sourceFolder), TextType.HEADING_3, getContext()));
        if (objectPath.getDirectlyReferencedIpsProjects().size() == 0) {
            return wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.ProjectOverviewPageElement_noSourceFolder), getContext()));
        }

        List<String> sourceFolder = new ArrayList<>();
        for (IIpsSrcFolderEntry folderEntry : objectPath.getSourceFolderEntries()) {
            sourceFolder.add(folderEntry.getSourceFolder().getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils(getContext())
                .createTextPageElements(sourceFolder)), getContext());
        return wrapper.addPageElements(referencedProjects);
    }

    private void addValidationErrorsTable() {
        MessageListTablePageElement messageListTablePageElement = new MessageListTablePageElement(
                validateLinkedObjects(), getContext());
        if (messageListTablePageElement.isEmpty()) {
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(),
                new TextPageElement(getContext().getMessage(
                        HtmlExportMessages.ProjectOverviewPageElement_validationErros), TextType.HEADING_2,
                        getContext()),
                messageListTablePageElement));
    }

    private MessageList validateLinkedObjects() {
        Set<IIpsSrcFile> srcFiles = getContext().getDocumentedSourceFiles();
        MessageList ml = new MessageList();
        for (IIpsSrcFile srcFile : srcFiles) {
            try {
                ml.add(srcFile.getIpsObject().validate(getProject()));
            } catch (IpsException e) {
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

    @Override
    public boolean isContentUnit() {
        return true;
    }

}
