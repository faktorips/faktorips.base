package org.faktorips.devtools.htmlexport.documentor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class DocumentorConfiguration {

	private IpsObjectType[] linkedIpsObjectTypes = new IpsObjectType[0];
    private String path;
    private List<IDocumentorScript> scripts = new ArrayList<IDocumentorScript>();
    private IIpsProject ipsProject;
    private ILayouter layouter;
    
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	public void setLinkedIpsObjectClasses(IpsObjectType... ipsObjectTypes ){
    	linkedIpsObjectTypes = ipsObjectTypes;
    }
    
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public IpsObjectType[] getLinkedIpsObjectTypes() {
        return linkedIpsObjectTypes;
    }

    public List<IIpsObject> getLinkedObjects() {
        List<IIpsObject> objects = new ArrayList<IIpsObject>();

        List<IIpsSrcFile> srcFiles = getLinkedSources();
        for (IIpsSrcFile ipsSrcFile : srcFiles) {
            try {
                objects.add(ipsSrcFile.getIpsObject());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    public List<IIpsSrcFile> getLinkedSources() {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(result, getLinkedIpsObjectTypes());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void addDocumentorScript(IDocumentorScript script) {
        scripts.add(script);
    }

    public List<IDocumentorScript> getScripts() {
        return scripts;
    }

    public ILayouter getLayouter() {
        return layouter;
    }

    public void setLayouter(ILayouter layouter) {
        this.layouter = layouter;
    }

	public SimpleDateFormat getSimpleDateFormat() {
		return dateFormat;
	}
    
}
