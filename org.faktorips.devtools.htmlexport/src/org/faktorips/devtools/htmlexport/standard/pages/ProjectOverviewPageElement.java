/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public class ProjectOverviewPageElement extends AbstractRootPageElement {

    private static final SimpleDateFormat CREATION_TIME_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
    private DocumentationContext context;

    /**
     * a page for the overview of an IpsProject, which is defined in the context
     * 
     */
    public ProjectOverviewPageElement(DocumentationContext context) {
        this.context = context;
        setTitle(context.getMessage("ProjectOverviewPageElement_project") + " " + getProject().getName()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_1));

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
        PageElement createCreationTime = new TextPageElement(
                context.getMessage("ProjectOverviewPageElement_created") + " " //$NON-NLS-1$ //$NON-NLS-2$
                        + CREATION_TIME_DATE_FORMAT.format(new Date()), TextType.BLOCK).addStyles(Style.SMALL);
        addPageElements(createCreationTime);
    }

    /**
     * adds the paths of the IpsObjects
     */
    private void addIpsObjectPaths() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(
                context.getMessage("ProjectOverviewPageElement_paths"), TextType.HEADING_2)); //$NON-NLS-1$
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

    private PageElement createArchiveEntriesList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context.getMessage("ProjectOverviewPageElement_archiveEntries"), //$NON-NLS-1$
                TextType.HEADING_3));

        if (objectPath.getArchiveEntries().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage("ProjectOverviewPageElement_noArchiveEntries"))); //$NON-NLS-1$
        }
        ListPageElement archiveEntriesList = new ListPageElement();
        for (IIpsArchiveEntry ipsArchiveEntry : objectPath.getArchiveEntries()) {
            archiveEntriesList.addPageElements(new TextPageElement(ipsArchiveEntry.getIpsArchive().getArchivePath()
                    .toString()));
        }
        return wrapper.addPageElements(archiveEntriesList);
    }

    private PageElement createReferencedIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(
                context.getMessage("ProjectOverviewPageElement_referencedProjects"), //$NON-NLS-1$
                TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage("ProjectOverviewPageElement_noReferencedProjects"))); //$NON-NLS-1$
        }

        List<String> referencedIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : objectPath.getReferencedIpsProjects()) {
            referencedIpsProjectsName.add(ipsProject.getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils().createTextPageElements(referencedIpsProjectsName)));
        return wrapper.addPageElements(referencedProjects);
    }

    private PageElement createReferencingIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context
                .getMessage("ProjectOverviewPageElement_referencingProjects"), //$NON-NLS-1$
                TextType.HEADING_3));
        IIpsProject[] referencingProjectLeavesOrSelf;
        try {
            referencingProjectLeavesOrSelf = objectPath.getIpsProject().findReferencingProjectLeavesOrSelf();
        } catch (CoreException e) {
            context.addStatus(new IpsStatus(IStatus.ERROR, "Error getting referencing projects", e)); //$NON-NLS-1$
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage("ProjectOverviewPageElement_noReferencingProjects"))); //$NON-NLS-1$
        }

        List<String> referencingIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : referencingProjectLeavesOrSelf) {
            if (getProject().equals(ipsProject)) {
                continue;
            }
            referencingIpsProjectsName.add(ipsProject.getName());
        }

        if (referencingIpsProjectsName.size() == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage("ProjectOverviewPageElement_noReferencingProjects"))); //$NON-NLS-1$
        }

        ListPageElement referencingProjects = new ListPageElement(Arrays.asList(new PageElementUtils().createTextPageElements(referencingIpsProjectsName)));
        return wrapper.addPageElements(referencingProjects);
    }

    private PageElement createSourceFolders(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(context.getMessage("ProjectOverviewPageElement_sourceFolder"), //$NON-NLS-1$
                TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper.addPageElements(new TextPageElement(context
                    .getMessage("ProjectOverviewPageElement_noSourceFolder"))); //$NON-NLS-1$
        }

        List<String> sourceFolder = new ArrayList<String>();
        for (IIpsSrcFolderEntry folderEntry : objectPath.getSourceFolderEntries()) {
            sourceFolder.add(folderEntry.getSourceFolder().getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(new PageElementUtils().createTextPageElements(sourceFolder)));
        return wrapper.addPageElements(referencedProjects);
    }

    private void addValidationErrorsTable() {
        MessageListTablePageElement messageListTablePageElement = new MessageListTablePageElement(
                validateLinkedObjects(), getContext());
        if (messageListTablePageElement.isEmpty()) {
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
                new TextPageElement(
                        context.getMessage("ProjectOverviewPageElement_validationErros"), TextType.HEADING_2), //$NON-NLS-1$
                messageListTablePageElement }));
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
        return PathUtilFactory.createPathUtil(getProject()).getPathToRoot();
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
}
