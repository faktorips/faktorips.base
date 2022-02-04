/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.standard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.BaseFrameHtmlGenerator;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementInIIpsPackageFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsObjectTypeListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsPackagesListPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;

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

    @SuppressWarnings("deprecation")
    @Override
    public void execute(DocumentationContext context, IProgressMonitor monitor) throws CoreRuntimeException {
        List<IIpsSrcFile> srcFiles = new ArrayList<>(context.getDocumentedSourceFiles());
        Set<IIpsPackageFragment> relatedPackageFragments = getRelatedPackageFragments(srcFiles);
        IpsObjectType[] documentedIpsObjectTypes = context.getDocumentedIpsObjectTypes();

        monitor.beginTask(
                "Write Html Export", //$NON-NLS-1$
                5 + srcFiles.size() + relatedPackageFragments.size() + documentedIpsObjectTypes.length);

        // Reihenfolge fuer anlauf des balkens im exportwizard ungemein wichtig

        try {
            writeBaseFrameDefinition(context, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
            writeClassesContentPages(context, srcFiles,
                    new org.eclipse.core.runtime.SubProgressMonitor(monitor, srcFiles.size()));
            writeOverviewPage(context, srcFiles, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
            writeAllClassesPage(context, srcFiles, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));

            writeProjectOverviewPage(context, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
            writePackagesClassesPages(context, srcFiles, relatedPackageFragments,
                    new org.eclipse.core.runtime.SubProgressMonitor(monitor,
                            relatedPackageFragments.size()));

            writeObjectTypesClassesPages(context, documentedIpsObjectTypes,
                    new org.eclipse.core.runtime.SubProgressMonitor(monitor,
                            documentedIpsObjectTypes.length));

            writeResources(context, new org.eclipse.core.runtime.SubProgressMonitor(monitor, 1));
        } catch (IOException e) {
            throw new CoreRuntimeException(new IpsStatus(e));
        } finally {
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
            IProgressMonitor monitor) throws IOException {

        monitor.beginTask("Classes", srcFiles.size()); //$NON-NLS-1$

        for (IIpsSrcFile ipsObject : srcFiles) {
            writeClassContentPage(context, ipsObject);
            monitor.worked(1);
        }
        monitor.done();
    }

    private void writeClassContentPage(DocumentationContext context, IIpsSrcFile ipsSrcFile) throws IOException {
        ICompositePageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(ipsSrcFile, context);
        if (objectContentPage == null) {
            return;
        }
        objectContentPage.build();
        writeFile(context, ipsSrcFile, objectContentPage);
    }

    private void writeFile(DocumentationContext context,
            IIpsSrcFile ipsSrcFile,
            ICompositePageElement objectContentPage)
            throws IOException {
        ioHandler
                .writeFile(
                        context,
                        STANDARD_PATH
                                + htmlUtil.getPathFromRoot(ipsSrcFile,
                                        LinkedFileType.getLinkedFileTypeByIpsElement(ipsSrcFile)),
                        getPageContent(context, objectContentPage));
    }

    private byte[] getPageContent(DocumentationContext context, IPageElement page) throws UnsupportedEncodingException {
        ILayouter layouter = context.getLayouter();
        page.acceptLayouter(layouter);
        return layouter.generate();
    }

    @SuppressWarnings("deprecation")
    private void writeObjectTypesClassesPages(DocumentationContext context,
            IpsObjectType[] documentedIpsObjectTypes,
            org.eclipse.core.runtime.SubProgressMonitor monitor) throws IOException {
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
        allClassesPage.setLinkTarget(TargetType.CONTENT);
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
                new IpsElementInIIpsPackageFilter(ipsPackageFragment), context, shownTypeChooser);
        allClassesPage.setLinkTarget(TargetType.CONTENT);
        allClassesPage.build();
        ioHandler.writeFile(
                context,
                STANDARD_PATH
                        + htmlUtil.getPathFromRoot(ipsPackageFragment,
                                LinkedFileType.getLinkedFileTypeByIpsElement(ipsPackageFragment)),
                getPageContent(context, allClassesPage));
    }

    private Set<IIpsPackageFragment> getRelatedPackageFragments(List<IIpsSrcFile> srcFiles) {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<>();
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
        allClassesPage.setLinkTarget(TargetType.CONTENT);
        allClassesPage.build();
        ioHandler.writeFile(context, STANDARD_PATH + "classes.html", getPageContent(context, allClassesPage)); //$NON-NLS-1$

        monitor.done();
    }

    private void writeOverviewPage(DocumentationContext context, List<IIpsSrcFile> srcFiles, IProgressMonitor monitor)
            throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IpsPackagesListPageElement allPackagesPage = new IpsPackagesListPageElement(context.getIpsProject(), srcFiles,
                context);
        allPackagesPage.setLinkTarget(TargetType.CLASSES);
        allPackagesPage.build();
        writeFileWithOutput(context, allPackagesPage, STANDARD_PATH + "overview.html"); //$NON-NLS-1$

        monitor.done();
    }

    private void writeFileWithOutput(DocumentationContext context, IPageElement allPackagesPage, String filePath)
            throws IOException {
        ioHandler.writeFile(context, filePath, getPageContent(context, allPackagesPage));
    }

    private void writeBaseFrameDefinition(DocumentationContext context, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator(
                context.getMessage(HtmlExportMessages.StandardDocumentorScript_documentation)
                        + " " + context.getIpsProject().getName(), //$NON-NLS-1$
                "20%, 80%", "30%, 70%"); //$NON-NLS-1$ //$NON-NLS-2$
        ioHandler.writeFile(context, STANDARD_PATH + "index.html", baseFrameHtml.generate()); //$NON-NLS-1$

        monitor.done();
    }

    private void writeProjectOverviewPage(DocumentationContext context, IProgressMonitor monitor) throws IOException {
        monitor.beginTask("", 1); //$NON-NLS-1$

        IPageElement projectOverviewHtml = new ProjectOverviewPageElement(context);
        projectOverviewHtml.build();
        ioHandler.writeFile(context, STANDARD_PATH + "summary.html", getPageContent(context, projectOverviewHtml)); //$NON-NLS-1$

        monitor.done();
    }

}
