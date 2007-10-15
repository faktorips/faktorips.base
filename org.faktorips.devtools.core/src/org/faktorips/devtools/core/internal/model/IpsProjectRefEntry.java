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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
        return "Project Reference:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR  //$NON-NLS-1$
            +  "     type=\"project\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "     referencedIpsProject=\"base\">      The other project used by this project." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
            +  "  </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR;  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    // the ips project referenced by this entry
    private IIpsProject referencedIpsProject;
    
    public IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }
    
    public IpsProjectRefEntry(IpsObjectPath path, IIpsProject referencedIpsProject) {
        super(path);
        this.referencedIpsProject = referencedIpsProject;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getReferencedIpsProject() {
        return referencedIpsProject;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TYPE_PROJECT_REFERENCE;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot(IIpsProject project) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObjectInternal(IIpsProject project, QualifiedNameType nameType, Set visitedEntries)
            throws CoreException {
        
        if (referencedIpsProject!=null) {
            return ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsObject(referencedIpsProject, nameType, visitedEntries);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjectsInternal(IIpsProject project, IpsObjectType type, List result, Set visitedEntries)
            throws CoreException {
        
        if (referencedIpsProject!=null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsObjects(referencedIpsProject, type, result, visitedEntries);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void findIpsObjectsInternal(IIpsProject ipsProject, List result, Set visitedEntries) throws CoreException {
        if (referencedIpsProject!=null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findAllIpsObjects(referencedIpsProject, result, visitedEntries);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void findIpsSrcFilesInternal(IIpsProject project, IpsObjectType type, List result, Set visitedEntries)
            throws CoreException {
        if (referencedIpsProject != null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFiles(referencedIpsProject, type,
                    result, visitedEntries);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected IIpsSrcFile findIpsSrcFileInternal(IIpsProject ipsProject, QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        if (referencedIpsProject!=null) {
            return ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFile(referencedIpsProject, nameType, visitedEntries);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjectsStartingWithInternal(IIpsProject project,
            IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result,
            Set visitedEntries) throws CoreException {
        if (referencedIpsProject != null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsObjectsStartingWith(
                    referencedIpsProject, type, prefix, ignoreCase, result, visitedEntries);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void findIpsSrcFilesStartingWithInternal(IIpsProject ipsProject,
            IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List result,
            Set visitedEntries) throws CoreException {
        if (referencedIpsProject != null) {
            ((IpsProject)referencedIpsProject).getIpsObjectPathInternal().findIpsSrcFilesStartingWith(
                    referencedIpsProject, type, prefix, ignoreCase, result, visitedEntries);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(Element element, IProject project) {
        String projectName = element.getAttribute("referencedIpsProject"); //$NON-NLS-1$
        if (!StringUtils.isEmpty(projectName)) {
            referencedIpsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_PROJECT_REFERENCE); //$NON-NLS-1$
        element.setAttribute("referencedIpsProject", referencedIpsProject==null?"":referencedIpsProject.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        IIpsProject project = getReferencedIpsProject();
        if(project==null){
            String text = Messages.IpsProjectRefEntry_noReferencedProjectSpecified;
            Message msg = new Message(MSGCODE_PROJECT_NOT_SPECIFIED, text, Message.ERROR, this);
            result.add(msg);
            return result;
        }
        if (!project.exists()){
            String text = NLS.bind(Messages.IpsProjectRefEntry_msgMissingReferencedProject, project.getName());
            Message msg = new Message(MSGCODE_MISSING_PROJECT, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ProjectRefEntry[" + (referencedIpsProject==null?"null":referencedIpsProject.getName()) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
