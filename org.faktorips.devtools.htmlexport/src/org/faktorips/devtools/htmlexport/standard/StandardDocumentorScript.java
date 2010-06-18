package org.faktorips.devtools.htmlexport.standard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.BaseFrameHtmlGenerator;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectInIIpsPackageFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsObjectListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsPackagesListPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardDocumentorScript implements IDocumentorScript {

    private static final String STANDARD_PATH = ""; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.IDocumentorScript#execute(org.faktorips.devtools.htmlexport
     * .documentor.DocumentorConfiguration)
     */
    public void execute(DocumentorConfiguration config, IProgressMonitor monitor) {
        List<IIpsObject> objects = config.getLinkedObjects();
        Set<IIpsPackageFragment> relatedPackageFragments = getRelatedPackageFragments(objects);

        monitor.beginTask("Write Html Export", 5 + objects.size() + relatedPackageFragments.size());
        writeOverviewPage(config, objects, new SubProgressMonitor(monitor, 1));
        writeBaseFrameDefinition(config, new SubProgressMonitor(monitor, 1));
        writeAllClassesPage(config, objects, new SubProgressMonitor(monitor, 1));
        writeProjectOverviewPage(config, new SubProgressMonitor(monitor, 1));
        writeResources(config, new SubProgressMonitor(monitor, 1));

        writePackagesClassesPages(config, objects, relatedPackageFragments, new SubProgressMonitor(monitor,
                relatedPackageFragments.size()));
        writeClassesContentPages(config, objects, new SubProgressMonitor(monitor, objects.size()));

        monitor.done();
    }

    private void writeResources(DocumentorConfiguration config, IProgressMonitor monitor) {
        ILayouter layouter = config.getLayouter();
        Set<LayoutResource> resources = layouter.getLayoutResources();
        for (LayoutResource layoutResource : resources) {
            FileHandler.writeFile(config, STANDARD_PATH + layoutResource.getName(), layoutResource.getContent());
        }
    }

    private void writeClassesContentPages(DocumentorConfiguration config,
            List<IIpsObject> objects,
            IProgressMonitor monitor) {

        monitor.beginTask("Classes", objects.size());

        for (IIpsObject ipsObject : objects) {
            writeClassContentPage(config, ipsObject);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writeClassContentPage(DocumentorConfiguration config, IIpsObject ipsObject) {
        AbstractRootPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(ipsObject, config);
        objectContentPage.build();
        FileHandler.writeFile(config, STANDARD_PATH
                + HtmlUtil.getPathFromRoot(ipsObject, LinkedFileType.getLinkedFileTypeByIpsElement(ipsObject)),
                getPageContent(config, objectContentPage));
    }

    private byte[] getPageContent(DocumentorConfiguration config, PageElement page) {
        ILayouter layouter = config.getLayouter();
        page.acceptLayouter(layouter);
        return layouter.generate();
    }

    private void writePackagesClassesPages(DocumentorConfiguration config,
            List<IIpsObject> objects,
            Set<IIpsPackageFragment> relatedPackageFragments,
            IProgressMonitor monitor) {

        monitor.beginTask("Packages Overview", relatedPackageFragments.size());
        for (IIpsPackageFragment ipsPackageFragment : relatedPackageFragments) {
            writePackagesClassesPage(config, ipsPackageFragment, objects);
            monitor.worked(1);
        }

        monitor.done();
    }

    private void writePackagesClassesPage(DocumentorConfiguration config,
            IIpsPackageFragment ipsPackageFragment,
            List<IIpsObject> objects) {
        IpsObjectListPageElement allClassesPage = new IpsObjectListPageElement(ipsPackageFragment, objects,
                new IpsObjectInIIpsPackageFilter(ipsPackageFragment), config);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        FileHandler.writeFile(config, STANDARD_PATH
                + HtmlUtil.getPathFromRoot(ipsPackageFragment, LinkedFileType
                        .getLinkedFileTypeByIpsElement(ipsPackageFragment)), getPageContent(config, allClassesPage));
    }

    private Set<IIpsPackageFragment> getRelatedPackageFragments(List<IIpsObject> objects) {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        for (IIpsObject ipsObject : objects) {
            relatedPackageFragments.add(ipsObject.getIpsPackageFragment());
        }
        return relatedPackageFragments;
    }

    private void writeAllClassesPage(DocumentorConfiguration config, List<IIpsObject> objects, IProgressMonitor monitor) {
        monitor.beginTask("", 1);

        IpsObjectListPageElement allClassesPage = new IpsObjectListPageElement(config.getIpsProject(), objects, config);
        allClassesPage.setLinkTarget("content"); //$NON-NLS-1$
        allClassesPage.build();
        FileHandler.writeFile(config, STANDARD_PATH + "classes.html", getPageContent(config, allClassesPage)); //$NON-NLS-1$

        monitor.done();
    }

    private void writeOverviewPage(DocumentorConfiguration config, List<IIpsObject> objects, IProgressMonitor monitor) {
        monitor.beginTask("", 1);

        IpsPackagesListPageElement allPackagesPage = new IpsPackagesListPageElement(config.getIpsProject(), objects,
                config);
        allPackagesPage.setLinkTarget("classes"); //$NON-NLS-1$
        allPackagesPage.build();
        writeFileWithOutput(config, allPackagesPage, STANDARD_PATH + "overview.html"); //$NON-NLS-1$

        monitor.done();
    }

    private void writeFileWithOutput(DocumentorConfiguration config,
            AbstractRootPageElement allPackagesPage,
            String filePath) {
        byte[] pageContent = getPageContent(config, allPackagesPage);

        /*
         * System.out.println("=======================================");
         * System.out.println(filePath); System.out.println(); System.out.println(new
         * String(pageContent));
         */

        FileHandler.writeFile(config, filePath, pageContent);
    }

    private void writeBaseFrameDefinition(DocumentorConfiguration config, IProgressMonitor monitor) {
        monitor.beginTask("", 1);

        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator(Messages.StandardDocumentorScript_documentation
                + " " + config.getIpsProject().getName(), "20%, 80%", "30%, 70%"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ 
        FileHandler.writeFile(config, STANDARD_PATH + "index.html", baseFrameHtml.generate()); //$NON-NLS-1$

        monitor.done();
    }

    private void writeProjectOverviewPage(DocumentorConfiguration config, IProgressMonitor monitor) {
        monitor.beginTask("", 1);

        AbstractRootPageElement projectOverviewHtml = new ProjectOverviewPageElement(config);
        projectOverviewHtml.build();
        FileHandler.writeFile(config, STANDARD_PATH + "summary.html", getPageContent(config, projectOverviewHtml)); //$NON-NLS-1$

        monitor.done();
    }

}
