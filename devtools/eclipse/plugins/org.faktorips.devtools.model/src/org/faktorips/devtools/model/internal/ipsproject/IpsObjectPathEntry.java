/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
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
        path = ipsObjectPath;
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
    public abstract boolean exists(QualifiedNameType qnt) throws IpsException;

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) {
        return null;
    }

    /**
     * Returns all objects of the given type found in this path entry and this entry only.
     * <p>
     * Returns an empty list for entries that do not contain any source files themselves but act as
     * composite entries. E.g. {@link IIpsProjectRefEntry} and {@link IIpsContainerEntry}.
     * <p>
     * The default implementation simply delegates the request to the
     * {@link IIpsPackageFragmentRoot}. However you should overwrite this method if you either have
     * no or multiple {@link IIpsPackageFragmentRoot roots} or you want to have a completely other
     * search strategy.
     * 
     * @param type The result only contains {@link IIpsSrcFile source files} of this type
     */
    @Override
    public List<IIpsSrcFile> findIpsSrcFiles(IpsObjectType type) {
        return getIpsPackageFragmentRoot().findAllIpsSrcFiles(type);
    }

    /**
     * Initializes the entry with the data stored in the xml element.
     * 
     * @param element the Top {@link Element}
     * @param project The {@link IIpsProject}
     */
    protected void initFromXml(Element element, AProject project) {
        if (element.hasAttribute(XML_ATTRIBUTE_REEXPORTED)) {
            reexported = Boolean.parseBoolean(element.getAttribute(XML_ATTRIBUTE_REEXPORTED));
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
    public static final IIpsObjectPathEntry createFromXml(IpsObjectPath path, Element element, AProject project) {
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
