/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsObjectPathEntry.
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPathEntry extends PlatformObject implements IIpsObjectPathEntry {

    // name of xml elements representing path entries.
    public static final String XML_ELEMENT = "Entry"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_REEXPORTED = "reexported"; //$NON-NLS-1$

    private IpsObjectPath path;

    private boolean reexported = true;

    public IpsObjectPathEntry(IpsObjectPath ipsObjectPath) {
        ArgumentCheck.notNull(ipsObjectPath);
        this.path = ipsObjectPath;
    }

    @Override
    public IIpsObjectPath getIpsObjectPath() {
        return path;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public IIpsProject getIpsProject() {
        return path.getIpsProject();
    }

    @Override
    public int getIndex() {
        return path.getIndex(this);
    }

    @Override
    public boolean isReexported() {
        return reexported;
    }

    @Override
    public void setReexported(boolean reexported) {
        this.reexported = reexported;
    }

    /**
     * Returns <code>true</code> if the entry contains a source file with the indicated qualified
     * name type, otherwise <code>false</code>.
     */
    public abstract boolean exists(QualifiedNameType qnt) throws CoreException;

    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) {
        IIpsSrcFile file = findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
        if (file == null) {
            return null;
        }
        return file.getIpsObject();
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) {
        return findIpsSrcFile(qnt, new IpsObjectPathSearchContext(getIpsProject()));
    }

    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType, IpsObjectPathSearchContext searchContext) {
        if (searchContext.visitAndConsiderContentsOf(this)) {
            return findIpsSrcFileInternal(nameType, searchContext);
        }
        return null;
    }

    /**
     * @deprecated This method is obsolete. Use
     *             {@link #findIpsSrcFilesInternal(IpsObjectType, String, List, Set)} instead.
     * 
     *             Adds all ips source files of the given type found in the path entry to the result
     *             list.
     */
    @Deprecated
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

    /**
     * * @deprecated This method is obsolete. Use
     * {@link #findIpsSrcFilesInternal(IpsObjectType, String, List, Set)} instead.
     */
    @Deprecated
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
     * @deprecated this method is not actively used in F-IPS.
     * 
     *             Returns all isp source files of the given type starting with the given prefix
     *             found on the path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the source files.
     */
    @Deprecated
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
     * <p>
     * The default implementation simply delegates the request to the
     * {@link IIpsPackageFragmentRoot}. However you should overwrite this method if you either have
     * no or multiple {@link IIpsPackageFragmentRoot roots} or you want to have a completely other
     * search strategy.
     * 
     * @param type The result only contains {@link IIpsSrcFile source files} of this type
     * @param packageFragment The package fragment in which all source files have to be. If this
     *            parameter is <code>null</code> the result contains all {@link IIpsSrcFile source
     *            files of this entry}
     * @param result The result list containing all found files
     * @param visitedEntries The already visited {@link IIpsObjectPathEntry}. If this entry consists
     *            of multiple entries the implementation may need to add additional added entries.
     *            This entry itself is already added to the set
     */
    protected void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        ((AbstractIpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsSourceFiles(type, packageFragment, result);
    }

    /**
     * Returns the first ips source file with the indicated qualified name type found in the path
     * entry.
     */
    protected abstract IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType,
            IpsObjectPathSearchContext searchContext);

    /**
     * @deprecated this method is not actively used in F-IPS.
     * 
     *             Returns all {@link IpsSrcFile}s of the given type starting with the given prefix
     *             found on the path.
     * 
     * @param ignoreCase <code>true</code> if case differences should be ignored during the search.
     * 
     * @throws CoreException if an error occurs while searching for the source files.
     */
    @Deprecated
    public abstract void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException;

    /**
     * Initializes the entry with the data stored in the xml element.
     * 
     * @param element the Top {@link Element}
     * @param project The {@link IIpsProject}
     */
    protected void initFromXml(Element element, IProject project) {
        if (element.hasAttribute(XML_ATTRIBUTE_REEXPORTED)) {
            reexported = Boolean.valueOf(element.getAttribute(XML_ATTRIBUTE_REEXPORTED)).booleanValue();
        }
    }

    /**
     * Transforms the entry to an xml element.
     * 
     * @param doc The xml document used to created the element.
     */
    protected Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute(XML_ATTRIBUTE_REEXPORTED, Boolean.toString(reexported));
        return element;
    }

    /**
     * Returns the object path entry stored in the xml element.
     */
    public static final IIpsObjectPathEntry createFromXml(IpsObjectPath path, Element element, IProject project) {
        IpsObjectPathEntry entry;
        String type = element.getAttribute(XML_ATTRIBUTE_TYPE);
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
        if (type.equals(TYPE_CONTAINER)) {
            entry = new IpsContainerEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        if (type.equals(TYPE_BUNDLE)) {
            entry = new IpsBundleEntry(path);
            entry.initFromXml(element, project);
            return entry;
        }
        throw new RuntimeException("Unknown entry type " + type); //$NON-NLS-1$
    }
}
