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

package org.faktorips.devtools.htmlexport.standard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.BaseFrameHtmlGenerator;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInIIpsPackageFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsObjectTypeListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsPackagesListPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardDocumentorScript implements IDocumentorScript {

    private static final String STANDARD_PATH = ""; //$NON-NLS-1$
    private final HtmlUtil htmlUtil = new HtmlUtil();
    private final IoHandler ioHandler;

    public StandardDocumentorScript() {
        this(new FileHandler());
    }

    public StandardDocumentorScript(IoHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) throws CoreException {
        List<IIpsSrcFile> srcFiles = context.getDocumentedSourceFiles();
        Set<IIpsPackageFragment> relatedPackageFragments = getRelatedPackageFragments(srcFiles);
        IpsObjectType[] documentedIpsObjectTypes = context.getDocumentedIpsObjectTypes();

        monitor.beginTask(
                "Write Html Export", 5 + srcFiles.size() + relatedPackageFragments.size() + documentedIpsObjectTypes.length); //$NON-NLS-1$

        // Reihenfolge fuer anlauf des balkens im exportwizard ungemein wichtig

        try {
            writeBaseFrameDefinition(context, new SubProgressMonitor(monitor, 1));
            writeClassesContentPages(context, srcFiles, new SubProgressMonitor(monitor, srcFiles.size()));
            writeOverviewPage(context, srcFiles, new SubProgressMonitor(monitor, 1));
            writeAllClassesPage(context, srcFiles, new SubProgressMonitor(monitor, 1));

            writeProjectOverviewPage(context, new SubProgressMonitor(monitor, 1));
            writePackagesClassesPages(context, srcFiles, relatedPackageFragments, new SubProgressMonitor(monitor,
                    relatedPackageFragments.size()));

            writeObjectTypesClassesPages(context, documentedIpsObjectTypes, new SubProgressMonitor(monitor,
                    documentedIpsObjectTypes.length));

            writeResources(context, new SubProgressMonitor(monitor, 1));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }

        finally {
            monitor.done();
        }
    }

    private void writeResources(DocumentationContext context, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        ILayouter layouter = context.getLayouter();
        Set<LayoutResource> resources = layouter.getLayoutResources();
        for (LayoutResource layoutResource : resources) {
            ioHandler.writeFile(context, STANDARD_PATH + layoutResource.getName(), layoutResource.getContent());
        }
        monitor.done();
    }

    private void writeClassesContentPages(DocumentationContext context,
            List<IIpsSrcFile> srcFiles,
            IProgressMonitor monitor) throws IOException, CoreException {

        monitor.beginTask("Classes", srcFiles.size()); //$NON-NLS-1$

        for (IIpsSrcFile ipsObject : srcFiles) {
            writeClassContentPage(context, ipsObject);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writeClassContentPage(DocumentationContext context, IIpsSrcFile ipsSrcFile) throws IOException,
            CoreException {
        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(ipsSrcFile, context);
        if (objectContentPage == null) {
            return;
        }
        objectContentPage.build();
        ioHandler
                .writeFile(
                        context,
                        STANDARD_PATH
                                + htmlUtil.getPathFromRoot(ipsSrcFile,
                                        LinkedFileType.getLinkedFileTypeByIpsElement(ipsSrcFile)),
                        getPageContent(context, objectContentPage));
    }

    private byte[] getPageContent(DocumentationContext context, PageElement page) throws UnsupportedEncodingException {
        ILayouter layouter = context.getLayouter();
        page.acceptLayouter(layouter);
        return layouter.generate();
    }

    private void writeObjectTypesClassesPages(DocumentationContext context,
            IpsObjectType[] documentedIpsObjectTypes,
            SubProgressMonitor monitor) throws IOException {
        monitor.beginTask("Object Types Overview", 1); //$NON-NLS-1$
        for (IpsObjectType ipsObjectType : documentedIpsObjectTypes) {
            writeObjectTypesClassesPage(context, ipsObjectType);
            monitor.worked(1);
        }
        monitor.done();

    }

    private void writeObjectTypesClassesPage(DocumentationContext context, IpsObjectType ipsObjectType)
            throws IOException {
        List<IIpsSrcFile> documentedSourceFiles = context.getDocumentedSourceFiles(ipsObjectType);
        if (documentedSourceFiles.size() == 0) {
            return;
        }
        IpsObjectTypeListPageElement allClassesPage = new IpsObjectTypeListPageElement(ipsObjectType,
                documentedSourceFiles, context);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        ioHandler.writeFile(context,
                STANDARD_PATH + htmlUtil.getPathFromRoot(ipsObjectType, LinkedFileType.OBJECT_TYPE_CLASSES_OVERVIEW),
                getPageContent(context, allClassesPage));

    }

    private void writePackagesClassesPages(DocumentationContext context,
            List<IIpsSrcFile> srcFiles,
            Set<IIpsPackageFragment> relatedPackageFragments,
            IProgressMonitor monitor) throws IOException {

        monitor.beginTask("Packages Overview", relatedPackageFragments.size()); //$NON-NLS-1$
        for (IIpsPackageFragment ipsPackageFragment : relatedPackageFragments) {
            writePackagesClassesPage(context, ipsPackageFragment, srcFiles);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writePackagesClassesPage(DocumentationContext context,
            IIpsPackageFragment ipsPackageFragment,
            List<IIpsSrcFile> srcFiles) throws IOException {
        boolean shownTypeChooser = false;
        IpsElementListPageElement allClassesPage = new IpsElementListPageElement(ipsPackageFragment, srcFiles,
                new IpsElementInIIpsPackageFilter(ipsPackageFragment, context), context, shownTypeChooser);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        ioHandler.writeFile(
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

    private void writeAllClassesPage(DocumentationContext context, List<IIpsSrcFile> srcFiles, IProgressMonitor monitor)
            throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IpsElementListPageElement allClassesPage = new IpsElementListPageElement(context.getIpsProject(), srcFiles,
                context);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        ioHandler.writeFile(context, STANDARD_PATH + "classes.html", getPageContent(context, allClassesPage)); //$NON-NLS-1$

        monitor.done();
    }

    private void writeOverviewPage(DocumentationContext context, List<IIpsSrcFile> srcFiles, IProgressMonitor monitor)
            throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IpsPackagesListPageElement allPackagesPage = new IpsPackagesListPageElement(context.getIpsProject(), srcFiles,
                context);
        allPackagesPage.setLinkTarget("classes"); //$NON-NLS-1$
        allPackagesPage.build();
        writeFileWithOutput(context, allPackagesPage, STANDARD_PATH + "overview.html"); //$NON-NLS-1$

        monitor.done();
    }

    private void writeFileWithOutput(DocumentationContext context, PageElement allPackagesPage, String filePath)
            throws IOException {
        ioHandler.writeFile(context, filePath, getPageContent(context, allPackagesPage));
    }

    private void writeBaseFrameDefinition(DocumentationContext context, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator(
                context.getMessage("StandardDocumentorScript_documentation") + " " + context.getIpsProject().getName(), "20%, 80%", "30%, 70%"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
        ioHandler.writeFile(context, STANDARD_PATH + "index.html", baseFrameHtml.generate()); //$NON-NLS-1$

        monitor.done();
    }

    private void writeProjectOverviewPage(DocumentationContext context, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        PageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        projectOverviewHtml.build();
        ioHandler.writeFile(context, STANDARD_PATH + "summary.html", getPageContent(context, projectOverviewHtml)); //$NON-NLS-1$

        monitor.done();
    }

}
