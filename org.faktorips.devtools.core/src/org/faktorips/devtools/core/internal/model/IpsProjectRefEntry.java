/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
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
    
    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Project Reference:" + SystemUtils.LINE_SEPARATOR
            +  "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR 
            +  "     type=\"project\"" + SystemUtils.LINE_SEPARATOR
            +  "     referencedIpsProject=\"base\">      The other project used by this project." + SystemUtils.LINE_SEPARATOR
            +  "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR; 
    }
    
    // the ips project referenced by this entry
    private IIpsProject referencedIpsProject;
    
    public IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }
    
    public IpsProjectRefEntry(IpsObjectPath path, IIpsProject referencedIpsProject) {
        super(path);
        ArgumentCheck.notNull(referencedIpsProject);
        this.referencedIpsProject = referencedIpsProject;
    }

    /**
     * Overridden.
     */
    public IIpsProject getReferencedIpsProject() {
        return referencedIpsProject;
    }

    /**
     * Overridden
     */
    public String getType() {
        return TYPE_PROJECT_REFERENCE;
    }

    /**
     * Overridden
     */
    public IIpsObject findIpsObject(IIpsProject project, IpsObjectType type, String qualifiedName)
            throws CoreException {
        return referencedIpsProject.findIpsObject(type, qualifiedName);
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(IIpsProject project, QualifiedNameType nameType)
            throws CoreException {
        return referencedIpsProject.findIpsObject(nameType);
    }

    /**
     * Overridden.
     */
    public void findIpsObjects(IIpsProject project, IpsObjectType type, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjects(type, result);
    }

    /**
     * Overridden.
     */
    public void findIpsObjectsStartingWith(IIpsProject project, IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
    }
    
    /**
     * Overridden.
     */
    public void initFromXml(Element element, IProject project) {
        String projectName = element.getAttribute("referencedIpsProject"); //$NON-NLS-1$
        referencedIpsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
    }

    /**
     * Overridden.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_PROJECT_REFERENCE); //$NON-NLS-1$
        element.setAttribute("referencedIpsProject", referencedIpsProject.getName()); //$NON-NLS-1$
        return element;
    }

}
