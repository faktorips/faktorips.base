package org.faktorips.devtools.htmlexport.documentor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.IDocumentorScript;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * Configuration for the Documentator
 * 
 * @author dicker
 * 
 */
public class DocumentorConfiguration {

    /**
     * related {@link IpsObjectType}s: Just {@link IIpsObject} of these types are documented
     */
    protected IpsObjectType[] linkedIpsObjectTypes = new IpsObjectType[0];
    /**
     * Path for output
     */
    protected String path;
    /**
     * All scripts within this documentation
     */
    protected List<IDocumentorScript> scripts = new ArrayList<IDocumentorScript>();
    /**
     * {@link IIpsProject}, which will be documented
     */
    protected IIpsProject ipsProject;
    /**
     * {@link ILayouter} contains the layout for the documentation
     */
    protected ILayouter layouter;

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
    private List<IIpsObject> linkedObjects;

    public DocumentorConfiguration() {
        super();
    }

    public void setLinkedIpsObjectClasses(IpsObjectType... ipsObjectTypes) {
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
        if (linkedObjects == null) {
            linkedObjects = getLinkedObjects(getLinkedIpsObjectTypes());
        }
        return linkedObjects;
    }

    private List<IIpsObject> getLinkedObjects(IpsObjectType... ipsObjectTypes) {
        List<IIpsObject> objects = new ArrayList<IIpsObject>();

        List<IIpsSrcFile> srcFiles = getLinkedSource(ipsObjectTypes);
        for (IIpsSrcFile ipsSrcFile : srcFiles) {
            try {
                objects.add(ipsSrcFile.getIpsObject());
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    /**
     * returns all {@link IIpsSrcFile} within the {@link IpsProject}, which type is within the given
     * array.
     * 
     * @param ipsObjectTypes
     * @return List<IIpsSrcFile>
     */
    public List<IIpsSrcFile> getLinkedSource(IpsObjectType... ipsObjectTypes) {
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            ipsProject.findAllIpsSrcFiles(result, ipsObjectTypes);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Collection<IIpsPackageFragment> getLinkedPackageFragments() {
        Set<IIpsPackageFragment> relatedPackageFragments = new HashSet<IIpsPackageFragment>();
        List<IIpsObject> linkedObjects = getLinkedObjects();
        for (IIpsObject ipsObject : linkedObjects) {
            relatedPackageFragments.add(ipsObject.getIpsPackageFragment());
        }
        return relatedPackageFragments;
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
