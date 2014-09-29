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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType, IpsObjectPathSearchContext searchContext) {
        if (searchContext.visitAndConsiderContentsOf(this)) {
            return findIpsSrcFileInternal(nameType, searchContext);
        }
        return null;
    }

    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType type, IpsObjectPathSearchContext searchContext) {
        if (searchContext.visitAndConsiderContentsOf(this)) {
            return findIpsSrcFilesInternal(type, searchContext);
        }
        return Collections.emptyList();
    }

    /**
     * Returns all objects of the given type found in the path entry.
     * <p>
     * The default implementation simply delegates the request to the
     * {@link IIpsPackageFragmentRoot}. However you should overwrite this method if you either have
     * no or multiple {@link IIpsPackageFragmentRoot roots} or you want to have a completely other
     * search strategy.
     * 
     * @param type The result only contains {@link IIpsSrcFile source files} of this type
     * @param searchContext The current {@link IpsObjectPathSearchContext}
     */
    protected List<IIpsSrcFile> findIpsSrcFilesInternal(IpsObjectType type, IpsObjectPathSearchContext searchContext) {
        try {
            return getIpsPackageFragmentRoot().findAllIpsSrcFiles(type);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns the first ips source file with the indicated qualified name type found in the path
     * entry.
     */
    protected abstract IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType nameType,
            IpsObjectPathSearchContext searchContext);

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
