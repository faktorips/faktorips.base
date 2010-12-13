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

package org.faktorips.devtools.htmlexport.standard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.BaseFrameHtmlGenerator;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInIIpsPackageFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsPackagesListPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardDocumentorScript implements IDocumentorScript {

    private static final String STANDARD_PATH = ""; //$NON-NLS-1$
    private final HtmlUtil htmlUtil = new HtmlUtil();

    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) {
        List<IIpsSrcFile> srcFiles = context.getDocumentedSourceFiles();
        Set<IIpsPackageFragment> relatedPackageFragments = getRelatedPackageFragments(srcFiles);

        monitor.beginTask("Write Html Export", 5 + srcFiles.size() + relatedPackageFragments.size()); //$NON-NLS-1$

        // Reihenfolge fuer anlauf des balkens im exportwizard ungemein wichtig

        writeBaseFrameDefinition(context, new SubProgressMonitor(monitor, 1));
        writeClassesContentPages(context, srcFiles, new SubProgressMonitor(monitor, srcFiles.size()));
        writeOverviewPage(context, srcFiles, new SubProgressMonitor(monitor, 1));
        writeAllClassesPage(context, srcFiles, new SubProgressMonitor(monitor, 1));

        writeProjectOverviewPage(context, new SubProgressMonitor(monitor, 1));
        writePackagesClassesPages(context, srcFiles, relatedPackageFragments, new SubProgressMonitor(monitor,
                relatedPackageFragments.size()));

        writeResources(context, new SubProgressMonitor(monitor, 1));

        monitor.done();
    }

    private void writeResources(DocumentationContext context, IProgressMonitor monitor) {
        monitor.beginTask("", 1); //$NON-NLS-1$

        ILayouter layouter = context.getLayouter();
        Set<LayoutResource> resources = layouter.getLayoutResources();
        for (LayoutResource layoutResource : resources) {
            FileHandler.writeFile(context, STANDARD_PATH + layoutResource.getName(), layoutResource.getContent());
        }
        monitor.done();
    }

    private void writeClassesContentPages(DocumentationContext context,
            List<IIpsSrcFile> srcFiles,
            IProgressMonitor monitor) {

        monitor.beginTask("Classes", srcFiles.size()); //$NON-NLS-1$

        for (IIpsSrcFile ipsObject : srcFiles) {
            writeClassContentPage(context, ipsObject);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writeClassContentPage(DocumentationContext context, IIpsSrcFile ipsSrcFile) {
        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(ipsSrcFile, context);
        if (objectContentPage == null) {
            return;
        }
        objectContentPage.build();
        FileHandler
                .writeFile(
                        context,
                        STANDARD_PATH
                                + htmlUtil.getPathFromRoot(ipsSrcFile,
                                        LinkedFileType.getLinkedFileTypeByIpsElement(ipsSrcFile)),
                        getPageContent(context, objectContentPage));
    }

    private byte[] getPageContent(DocumentationContext context, PageElement page) {
        ILayouter layouter = context.getLayouter();
        page.acceptLayouter(layouter);
        return layouter.generate();
    }

    private void writePackagesClassesPages(DocumentationContext context,
            List<IIpsSrcFile> srcFiles,
            Set<IIpsPackageFragment> relatedPackageFragments,
            IProgressMonitor monitor) {

        monitor.beginTask("Packages Overview", relatedPackageFragments.size()); //$NON-NLS-1$
        for (IIpsPackageFragment ipsPackageFragment : relatedPackageFragments) {
            writePackagesClassesPage(context, ipsPackageFragment, srcFiles);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writePackagesClassesPage(DocumentationContext context,
            IIpsPackageFragment ipsPackageFragment,
            List<IIpsSrcFile> srcFiles) {
        boolean shownTypeChooser = false; // TODO auf true, wenn fertig
        IpsElementListPageElement allClassesPage = new IpsElementListPageElement(ipsPackageFragment, srcFiles,
                new IpsElementInIIpsPackageFilter(ipsPackageFragment, context), context, shownTypeChooser);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        FileHandler.writeFile(
                context,
                STANDARD_PATH
                        + htmlUtil.getPathFromRoot(ipsPackageFragment,
                                LinkedFileType.getLinkedFileTypeByIpsElement(ipsPackageFragment)),
                getPageContent(context, allClassesPage));
    }

    private Set<IIpsPackageFragment> getRelatedPackageFragments(List<IIpsSrcFile> srcFiles) {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        for (IIpsSrcFile srcFile : srcFiles) {
            relatedPackageFragments.add(srcFile.getIpsPackageFragment());
        }
        return relatedPackageFragments;
    }

    private void writeAllClassesPage(DocumentationContext context, List<IIpsSrcFile> srcFiles, IProgressMonitor monitor) {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IpsElementListPageElement allClassesPage = new IpsElementListPageElement(context.getIpsProject(), srcFiles,
                context);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        FileHandler.writeFile(context, STANDARD_PATH + "classes.html", getPageContent(context, allClassesPage)); //$NON-NLS-1$

        monitor.done();
    }

    private void writeOverviewPage(DocumentationContext context, List<IIpsSrcFile> srcFiles, IProgressMonitor monitor) {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IpsPackagesListPageElement allPackagesPage = new IpsPackagesListPageElement(context.getIpsProject(), srcFiles,
                context);
        allPackagesPage.setLinkTarget("classes"); //$NON-NLS-1$
        allPackagesPage.build();
        writeFileWithOutput(context, allPackagesPage, STANDARD_PATH + "overview.html"); //$NON-NLS-1$

        monitor.done();
    }

    private void writeFileWithOutput(DocumentationContext context, AbstractPageElement allPackagesPage, String filePath) {
        byte[] pageContent = getPageContent(context, allPackagesPage);

        FileHandler.writeFile(context, filePath, pageContent);
    }

    private void writeBaseFrameDefinition(DocumentationContext context, IProgressMonitor monitor) {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator(Messages.StandardDocumentorScript_documentation
                + " " + context.getIpsProject().getName(), "20%, 80%", "30%, 70%"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ 
        FileHandler.writeFile(context, STANDARD_PATH + "index.html", baseFrameHtml.generate()); //$NON-NLS-1$

        monitor.done();
    }

    private void writeProjectOverviewPage(DocumentationContext context, IProgressMonitor monitor) {
        monitor.beginTask("", 1); //$NON-NLS-1$

        AbstractPageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        projectOverviewHtml.build();
        FileHandler.writeFile(context, STANDARD_PATH + "summary.html", getPageContent(context, projectOverviewHtml)); //$NON-NLS-1$

        monitor.done();
    }

}
