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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsObjectPathEntry.
 *  
 * @author Jan Ortmann
 */
public abstract class IpsObjectPathEntry implements IIpsObjectPathEntry {
    
    // name of xml elements representing path entries.
    public final static String XML_ELEMENT = "Entry"; //$NON-NLS-1$

    private IpsObjectPath path;
    
    public IpsObjectPathEntry(IpsObjectPath path) {
        ArgumentCheck.notNull(path);
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPath getIpsObjectPath() {
        return path;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return path.getIndex(this);
    }

    /**
     * Returns the first object with the indicated qualified name type found in the path entry.
     */
    public final IIpsObject findIpsObject(IIpsProject ipsProject, QualifiedNameType nameType, Set visitedEntries) throws CoreException {
        if (visitedEntries.contains(this)) {
            return null;
        }
        visitedEntries.add(this);
        return findIpsObjectInternal(ipsProject, nameType, visitedEntries);
    }

    /**
     * Returns the first object with the indicated qualified name type found in the path entry.
     */
    protected abstract IIpsObject findIpsObjectInternal(IIpsProject ipsProject, QualifiedNameType nameType, Set visitedEntries) throws CoreException;

    /**
     * Adds all objects of the given type found in the path entry to the result list. 
     */
    public final void findIpsObjects(IIpsProject ipsProject, IpsObjectType type, List result, Set visitedEntries) throws CoreException {
        if (visitedEntries.contains(this)) {
            return;
        }
        visitedEntries.add(this);
        findIpsObjectsInternal(ipsProject, type, result, visitedEntries);
    }
    
    /**
     * Adds all objects of the given type found in the path entry to the result list. 
     */
    protected abstract void findIpsObjectsInternal(IIpsProject ipsProject, IpsObjectType type, List result, Set visitedEntries) throws CoreException;

    
    /**
     * Adds all objects of the given type found in the path entry to the result list. 
     */
    public final void findIpsObjects(IIpsProject ipsProject, List result, Set visitedEntries) throws CoreException {
        if (visitedEntries.contains(this)) {
            return;
        }
        visitedEntries.add(this);
        findIpsObjectsInternal(ipsProject, result, visitedEntries);
    }
    
    /**
     * Adds all objects of the given type found in the path entry to the result list. 
     */
    protected abstract void findIpsObjectsInternal(IIpsProject ipsProject, List result, Set visitedEntries) throws CoreException;
    
    
    /**
     * Returns all objects of the given type starting with the given prefix found on the path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the objects. 
     */
    public final void findIpsObjectsStartingWith(
    		IIpsProject ipsProject, 
    		IpsObjectType type, 
    		String prefix, 
    		boolean ignoreCase, 
    		List result,
            Set visitedEntries) throws CoreException {

        if (visitedEntries.contains(this)) {
            return;
        }
        visitedEntries.add(this);
        findIpsObjectsStartingWithInternal(ipsProject, type, prefix, ignoreCase, result, visitedEntries);
        
    }

    /**
     * Returns all objects of the given type starting with the given prefix found on the path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the objects. 
     */
    protected abstract void findIpsObjectsStartingWithInternal(
            IIpsProject ipsProject, 
            IpsObjectType type, 
            String prefix, 
            boolean ignoreCase, 
            List result,
            Set visitedEntries) throws CoreException;

    /**
     * Initializes the entry with the data stored in the xml element.
     */
    public abstract void initFromXml(Element element, IProject project);
    
    /**
     * Transforms the entry to an xml element.
     * @param doc The xml document used to created the element.
     */
    public abstract Element toXml(Document doc);
    
    /**
     * Returns the object path entry stored in the xml element.     
     */
    public final static IIpsObjectPathEntry createFromXml(IpsObjectPath path, Element element, IProject project) {
        IpsObjectPathEntry entry;
        String type = element.getAttribute("type"); //$NON-NLS-1$
        if (type.equals(TYPE_SRC_FOLDER)) {
            entry = new IpsSrcFolderEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        if (type.equals(TYPE_PROJECT_REFERENCE)) {
            entry = new IpsProjectRefEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        if (type.equals(TYPE_ARCHIVE)) {
            entry = new IpsArchiveEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        throw new RuntimeException("Unknown entry type " + type); //$NON-NLS-1$
    }
    

}
