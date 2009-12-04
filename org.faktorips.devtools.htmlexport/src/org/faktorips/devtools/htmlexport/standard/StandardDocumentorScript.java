package org.faktorips.devtools.htmlexport.standard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.AbstractDocumentorScript;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.IGenerator;
import org.faktorips.devtools.htmlexport.generators.html.AllClassesPageHtmlGenerator;
import org.faktorips.devtools.htmlexport.generators.html.AllPackagesPageHtmlGenerator;
import org.faktorips.devtools.htmlexport.generators.html.BaseFrameHtmlGenerator;
import org.faktorips.devtools.htmlexport.generators.html.objects.AbstractObjectContentPageHtmlGenerator;
import org.faktorips.devtools.htmlexport.helper.FileHandler;
import org.faktorips.devtools.htmlexport.helper.filter.IpsObjectInIIpsPackageFilter;
import org.faktorips.devtools.htmlexport.helper.html.HtmlUtil;
import org.faktorips.devtools.htmlexport.helper.html.path.LinkedFileTypes;
import org.faktorips.devtools.htmlexport.standard.generators.ProjectOverviewPageHtmlGenerator;

public class StandardDocumentorScript extends AbstractDocumentorScript {

    public void execute(DocumentorConfiguration config) {
        List<IIpsObject> objects = config.getLinkedObjects();
        writeBaseFrame(config, objects);
    }

    private void writeBaseFrame(DocumentorConfiguration config, List<IIpsObject> objects) {
        writeBaseFrameDefinition(config);
        writeAllClassesPage(config, objects);
        writeAllOverviewPage(config, objects);
        writeProjectOverviewPage(config);
        writePackagesClassesPages(config, objects);
        writeClassesContentPages(config, objects);
    }

    private void writeClassesContentPages(DocumentorConfiguration config, List<IIpsObject> objects) {
        for (IIpsObject ipsObject : objects) {
            writeClassContentPage(config, ipsObject);
        }
        
    }

    private void writeClassContentPage(DocumentorConfiguration config, IIpsObject ipsObject) {
        IGenerator objectContentPage = AbstractObjectContentPageHtmlGenerator.getInstance(ipsObject);
        FileHandler.writeFile(config, "standard/" + HtmlUtil.getPathFromRoot(ipsObject, LinkedFileTypes.CLASS_CONTENT), objectContentPage.generate());
    }

    private void writePackagesClassesPages(DocumentorConfiguration config, List<IIpsObject> objects) {
        for (IIpsPackageFragment ipsPackageFragment : getRelatedPackageFragments(objects)) {
            writePackagesClassesPage(config, ipsPackageFragment, objects);
        }
    }

    private void writePackagesClassesPage(DocumentorConfiguration config, IIpsPackageFragment ipsPackageFragment, List<IIpsObject> objects) {
        IGenerator allClassesPage = new AllClassesPageHtmlGenerator(ipsPackageFragment, objects, new IpsObjectInIIpsPackageFilter(ipsPackageFragment));
        FileHandler.writeFile(config, "standard/" + HtmlUtil.getPathFromRoot(ipsPackageFragment, LinkedFileTypes.PACKAGE_CLASSES_OVERVIEW), allClassesPage.generate());
    }

    private Set<IIpsPackageFragment> getRelatedPackageFragments(List<IIpsObject> objects) {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        for (IIpsObject ipsObject : objects) {
            relatedPackageFragments.add(ipsObject.getIpsPackageFragment());
        }
        return relatedPackageFragments;
    }

    private void writeAllClassesPage(DocumentorConfiguration config, List<IIpsObject> objects) {
        IGenerator allClassesPage = new AllClassesPageHtmlGenerator(config.getIpsProject(), objects);
        FileHandler.writeFile(config, "standard/classes.html", allClassesPage.generate());
    }

    private void writeAllOverviewPage(DocumentorConfiguration config, List<IIpsObject> objects) {
        IGenerator allPackagesPage = new AllPackagesPageHtmlGenerator(config.getIpsProject(), objects);
        FileHandler.writeFile(config, "standard/overview.html", allPackagesPage.generate());
    }

    private void writeBaseFrameDefinition(DocumentorConfiguration config) {
        IGenerator baseFrameHtml = new BaseFrameHtmlGenerator("Komponenten", "20%, 80%", "30%, 70%");
        FileHandler.writeFile(config, "standard/index.html", baseFrameHtml.generate());
    }
    
    private void writeProjectOverviewPage(DocumentorConfiguration config) {
        IGenerator projectOverviewHtml = new ProjectOverviewPageHtmlGenerator(config.getIpsProject());
        FileHandler.writeFile(config, "standard/summary.html", projectOverviewHtml.generate());
    }

}
