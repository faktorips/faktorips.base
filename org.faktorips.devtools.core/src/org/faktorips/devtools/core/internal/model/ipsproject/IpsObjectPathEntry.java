/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
    @Override
    public IIpsObjectPath getIpsObjectPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsProject getIpsProject() {
        return path.getIpsProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return path.getIndex(this);
    }

    /**
     * Returns <code>true</code> if the entry contains a source file with the indicated qualified
     * name type, otherwise <code>false</code>.
     * 
     * @throws CoreException
     */
    abstract public boolean exists(QualifiedNameType qnt) throws CoreException;

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
        if (file == null) {
            return null;
        }
        return file.getIpsObject();
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) throws CoreException {
        return findIpsSrcFile(qnt, null);
    }

    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {
        if (visitedEntries != null) {
            if (visitedEntries.contains(this)) {
                return null;
            }
            visitedEntries.add(this);
        }
        return findIpsSrcFileInternal(nameType, visitedEntries);
    }

    /**
     * Adds all ips source files of the given type found in the path entry to the result list.
     */
    public final void findIpsSrcFiles(IpsObjectType type,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        if (visitedEntries != null) {
            if (visitedEntries.contains(this)) {
                return;
            }
            visitedEntries.add(this);
        }
        findIpsSrcFilesInternal(type, null, result, visitedEntries);
    }

    public final void findIpsSrcFiles(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        if (visitedEntries != null) {
            if (visitedEntries.contains(this)) {
                return;
            }
            visitedEntries.add(this);
        }
        findIpsSrcFilesInternal(type, packageFragment, result, visitedEntries);
    }

    /**
     * Returns all isp source files of the given type starting with the given prefix found on the
     * path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the source files.
     */
    public final void findIpsSrcFilesStartingWith(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        if (visitedEntries.contains(this)) {
            return;
        }
        visitedEntries.add(this);
        findIpsSrcFilesStartingWithInternal(type, prefix, ignoreCase, result, visitedEntries);
    }

    /**
     * Adds all objects of the given type found in the path entry to the result list.
     */
    protected abstract void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException;

    /**
     * Returns the first ips source file with the indicated qualified name type found in the path
     * entry.
     */
    protected abstract IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException;

    /**
     * Returns all ips source files of the given type starting with the given prefix found on the
     * path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the source files.
     */
    public abstract void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException;

    /**
     * Initializes the entry with the data stored in the xml element.
     */
    public abstract void initFromXml(Element element, IProject project);

    /**
     * Transforms the entry to an xml element.
     * 
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
