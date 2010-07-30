/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
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
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public class ProjectOverviewPageElement extends AbstractRootPageElement {

    private static final SimpleDateFormat CREATION_TIME_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm"); //$NON-NLS-1$
    private DocumentorConfiguration config;

    /**
     * a page for the overview of an IpsProject, which is defined in the config
     * 
     * @param config
     */
    public ProjectOverviewPageElement(DocumentorConfiguration config) {
        this.config = config;
        setTitle(Messages.ProjectOverviewPageElement_project + " " + getProject().getName()); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement #build()
     */
    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_1));

        addIpsObjectPaths();

        if (getConfig().isShowValidationErrors()) {
            addValidationErrorsTable();
        }

        addCreationTime();
    }

    /**
     * adds creation time
     */
    private void addCreationTime() {
        PageElement createCreationTime = new TextPageElement(Messages.ProjectOverviewPageElement_created + " " //$NON-NLS-1$
                + CREATION_TIME_DATE_FORMAT.format(new Date()), TextType.BLOCK).addStyles(Style.SMALL);
        addPageElements(createCreationTime);
    }

    /**
     * adds the paths of the IpsObjects
     */
    private void addIpsObjectPaths() {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_paths, TextType.HEADING_2));
        IIpsObjectPath objectPath;
        try {
            objectPath = getProject().getIpsObjectPath();

            wrapper.addPageElements(createArchiveEntriesList(objectPath));
            wrapper.addPageElements(createReferencedIpsProjectList(objectPath));
            wrapper.addPageElements(createReferencingIpsProjectList(objectPath));
            wrapper.addPageElements(createSourceFolders(objectPath));

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        addPageElements(wrapper);
    }

    private PageElement createArchiveEntriesList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_archiveEntries,
                TextType.HEADING_3));

        if (objectPath.getArchiveEntries().length == 0) {
            return wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_noArchiveEntries));
        }
        ListPageElement archiveEntriesList = new ListPageElement();
        for (IIpsArchiveEntry ipsArchiveEntry : objectPath.getArchiveEntries()) {
            archiveEntriesList.addPageElements(new TextPageElement(ipsArchiveEntry.getArchivePath().toString()));
        }
        return wrapper.addPageElements(archiveEntriesList);
    }

    private PageElement createReferencedIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_referencedProjects,
                TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper
                    .addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_noReferencedProjects));
        }

        List<String> referencedIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : objectPath.getReferencedIpsProjects()) {
            referencedIpsProjectsName.add(ipsProject.getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(PageElementUtils
                .createTextPageElements(referencedIpsProjectsName)));
        return wrapper.addPageElements(referencedProjects);
    }

    private PageElement createReferencingIpsProjectList(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_referencingProjects,
                TextType.HEADING_3));
        IIpsProject[] referencingProjectLeavesOrSelf;
        try {
            referencingProjectLeavesOrSelf = objectPath.getIpsProject().findReferencingProjectLeavesOrSelf();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

        List<String> referencingIpsProjectsName = new ArrayList<String>();
        for (IIpsProject ipsProject : referencingProjectLeavesOrSelf) {
            if (getProject().equals(ipsProject)) {
                continue;
            }
            referencingIpsProjectsName.add(ipsProject.getName());
        }

        if (referencingIpsProjectsName.size() == 0) {
            return wrapper.addPageElements(new TextPageElement(
                    Messages.ProjectOverviewPageElement_noReferencingProjects));
        }

        ListPageElement referencingProjects = new ListPageElement(Arrays.asList(PageElementUtils
                .createTextPageElements(referencingIpsProjectsName)));
        return wrapper.addPageElements(referencingProjects);
    }

    private PageElement createSourceFolders(IIpsObjectPath objectPath) {
        AbstractCompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_sourceFolder,
                TextType.HEADING_3));
        if (objectPath.getReferencedIpsProjects().length == 0) {
            return wrapper.addPageElements(new TextPageElement(Messages.ProjectOverviewPageElement_noSourceFolder));
        }

        List<String> sourceFolder = new ArrayList<String>();
        for (IIpsSrcFolderEntry folderEntry : objectPath.getSourceFolderEntries()) {
            sourceFolder.add(folderEntry.getSourceFolder().getName());
        }
        ListPageElement referencedProjects = new ListPageElement(Arrays.asList(PageElementUtils
                .createTextPageElements(sourceFolder)));
        return wrapper.addPageElements(referencedProjects);
    }

    private void addValidationErrorsTable() {
        MessageListTablePageElement messageListTablePageElement = new MessageListTablePageElement(
                validateLinkedObjects());
        if (messageListTablePageElement.isEmpty()) {
            return;
        }
        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new PageElement[] {
                new TextPageElement(Messages.ProjectOverviewPageElement_validationErros, TextType.HEADING_2),
                messageListTablePageElement }));
    }

    private MessageList validateLinkedObjects() {
        List<IIpsSrcFile> srcFiles = getConfig().getDocumentedSourceFiles();
        MessageList ml = new MessageList();
        for (IIpsSrcFile srcFile : srcFiles) {
            try {
                ml.add(srcFile.getIpsObject().validate(getProject()));
            } catch (Exception e) {
                System.out.println(srcFile);
                e.printStackTrace();
            }
        }
        return ml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement
     * #getPathToRoot()
     */
    @Override
    public String getPathToRoot() {
        return PathUtilFactory.createPathUtil(getProject()).getPathToRoot();
    }

    /**
     * returns the configurated IpsProject
     * 
     * @return
     */
    protected IIpsProject getProject() {
        return getConfig().getIpsProject();
    }

    /**
     * returns the config
     * 
     * @return
     */
    protected DocumentorConfiguration getConfig() {
        return config;
    }
}
