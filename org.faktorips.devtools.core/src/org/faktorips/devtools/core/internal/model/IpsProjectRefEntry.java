package org.faktorips.devtools.core.internal.model;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsProjectRefEntry.
 *  
 * @author Jan Ortmann
 */
public class IpsProjectRefEntry extends IpsObjectPathEntry implements
        IIpsProjectRefEntry {
    
    // the ips porject referenced by this entry
    private IIpsProject referencedIpsProject;
    
    IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }
    
    IpsProjectRefEntry(IpsObjectPath path, IIpsProject referencedIpsProject) {
        super(path);
        ArgumentCheck.notNull(referencedIpsProject);
        this.referencedIpsProject = referencedIpsProject;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsProjectRefEntry#getReferencedIpsProject()
     */
    public IIpsProject getReferencedIpsProject() {
        return referencedIpsProject;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.IIpsObjectPathEntry#getType()
     */
    public String getType() {
        return TYPE_PROJECT_REFERENCE;
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObject(org.faktorips.devtools.core.model.IpsObjectType, java.lang.String)
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName)
            throws CoreException {
        return referencedIpsProject.findIpsObject(type, qualifiedName);
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObject(org.faktorips.devtools.core.model.IpsObjectType, java.lang.String)
     */
    public IIpsObject findIpsObject(QualifiedNameType nameType)
            throws CoreException {
        return referencedIpsProject.findIpsObject(nameType);
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObjects(org.faktorips.devtools.core.model.IpsObjectType, java.util.List)
     */
    public void findIpsObjects(IpsObjectType type, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjects(type, result);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#findIpsObjectsStartingWith(org.faktorips.devtools.core.model.IpsObjectType, java.lang.String, boolean, java.util.List)
     */
    public void findIpsObjectsStartingWith(IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#initFromXml(org.w3c.dom.Element)
     */
    public void initFromXml(Element element) {
        String projectName = element.getAttribute("referencedIpsProject");
        referencedIpsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
    }

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPathEntry#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_PROJECT_REFERENCE);
        element.setAttribute("referencedIpsProject", referencedIpsProject.getName());
        return element;
    }

}
