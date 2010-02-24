package org.faktorips.devtools.htmlexport.standard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.AbstractDocumentorScript;
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
import org.faktorips.devtools.htmlexport.pages.elements.types.AllClassesPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AllPackagesPageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractObjectContentPageElement;
import org.faktorips.devtools.htmlexport.standard.pages.ProjectOverviewPageElement;

public class StandardDocumentorScript extends AbstractDocumentorScript {

    private static final String STANDARD_PATH = "standard/";

	public void execute(DocumentorConfiguration config) {
        List<IIpsObject> objects = config.getLinkedObjects();
        writeBaseFrame(config, objects);
    }

    private void writeBaseFrame(DocumentorConfiguration config, List<IIpsObject> objects) {
        writeOverviewPage(config, objects);
        writeBaseFrameDefinition(config);
        writeAllClassesPage(config, objects);
        writeProjectOverviewPage(config);
        writePackagesClassesPages(config, objects);
        writeClassesContentPages(config, objects);
        
        writeResources(config);
    }

    private void writeResources(DocumentorConfiguration config) {
    	ILayouter layouter = config.getLayouter();
		Set<LayoutResource> resources = layouter.getLayoutResources();
    	for (LayoutResource layoutResource : resources) {
			FileHandler.writeFile(config, STANDARD_PATH + layoutResource.getName(), layoutResource.getContent());
		}
    	
		
	}

	private void writeClassesContentPages(DocumentorConfiguration config, List<IIpsObject> objects) {
        for (IIpsObject ipsObject : objects) {
            writeClassContentPage(config, ipsObject);
        }
    }

    private void writeClassContentPage(DocumentorConfiguration config, IIpsObject ipsObject) {
        AbstractRootPageElement objectContentPage = AbstractObjectContentPageElement.getInstance(ipsObject, config);
        objectContentPage.build();
        FileHandler.writeFile(config, STANDARD_PATH + HtmlUtil.getPathFromRoot(ipsObject, LinkedFileType.getLinkedFileTypeByIpsElement(ipsObject)), getPageContent(config,
                objectContentPage));
    }

    private byte[] getPageContent(DocumentorConfiguration config, PageElement page) {
        ILayouter layouter = config.getLayouter();
        page.acceptLayouter(layouter);
        return layouter.generate();
    }

    private void writePackagesClassesPages(DocumentorConfiguration config, List<IIpsObject> objects) {
        for (IIpsPackageFragment ipsPackageFragment : getRelatedPackageFragments(objects)) {
            writePackagesClassesPage(config, ipsPackageFragment, objects);
        }
    }

    private void writePackagesClassesPage(DocumentorConfiguration config, IIpsPackageFragment ipsPackageFragment, List<IIpsObject> objects) {
        AllClassesPageElement allClassesPage = new AllClassesPageElement(ipsPackageFragment, objects, new IpsObjectInIIpsPackageFilter(ipsPackageFragment));
        allClassesPage.setLinkTarget("content");
        allClassesPage.build();
        FileHandler.writeFile(config, STANDARD_PATH + HtmlUtil.getPathFromRoot(ipsPackageFragment, LinkedFileType.getLinkedFileTypeByIpsElement(ipsPackageFragment)), getPageContent(
                config, allClassesPage));
    }

    private Set<IIpsPackageFragment> getRelatedPackageFragments(List<IIpsObject> objects) {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        for (IIpsObject ipsObject : objects) {
            relatedPackageFragments.add(ipsObject.getIpsPackageFragment());
        }
        return relatedPackageFragments;
    }

    private void writeAllClassesPage(DocumentorConfiguration config, List<IIpsObject> objects) {
        AllClassesPageElement allClassesPage = new AllClassesPageElement(config.getIpsProject(), objects);
        allClassesPage.setLinkTarget("content");
        allClassesPage.build();
        FileHandler.writeFile(config, "standard/classes.html", getPageContent(config, allClassesPage));
    }

    private void writeOverviewPage(DocumentorConfiguration config, List<IIpsObject> objects) {
        AllPackagesPageElement allPackagesPage = new AllPackagesPageElement(config.getIpsProject(), objects);
        allPackagesPage.setLinkTarget("classes");
        allPackagesPage.build();
        writeFileWithOutput(config, allPackagesPage, "standard/overview.html");
    }

    private void writeFileWithOutput(DocumentorConfiguration config, AbstractRootPageElement allPackagesPage, String filePath) {
        byte[] pageContent = getPageContent(config, allPackagesPage);

        /*
        System.out.println("=======================================");
        System.out.println(filePath);
        System.out.println();
        System.out.println(new String(pageContent));
		*/
		
        FileHandler.writeFile(config, filePath, pageContent);
    }

    private void writeBaseFrameDefinition(DocumentorConfiguration config) {
        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator("Komponenten", "20%, 80%", "30%, 70%");
        FileHandler.writeFile(config, "standard/index.html", baseFrameHtml.generate());
    }

    private void writeProjectOverviewPage(DocumentorConfiguration config) {
        AbstractRootPageElement projectOverviewHtml = new ProjectOverviewPageElement(config);
        projectOverviewHtml.build();
        FileHandler.writeFile(config, "standard/summary.html", getPageContent(config, projectOverviewHtml));
    }

}
