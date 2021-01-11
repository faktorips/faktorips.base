/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragmentRoot extends IpsElement implements IIpsPackageFragmentRoot {

    public AbstractIpsPackageFragmentRoot(IIpsProject parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean isBasedOnSourceFolder() {
        return getIpsObjectPathEntry().getType() == IIpsObjectPathEntry.TYPE_SRC_FOLDER;
    }

    @Override
    public boolean isBasedOnIpsArchive() {
        return getIpsObjectPathEntry().getType() == IIpsObjectPathEntry.TYPE_ARCHIVE;
    }

    @Override
    public IIpsProject getIpsProject() {
        return (IIpsProject)getParent();
    }

    @Override
    public IIpsPackageFragment getDefaultIpsPackageFragment() {
        return getIpsPackageFragment(""); //$NON-NLS-1$
    }

    @Override
    public IIpsObjectPathEntry getIpsObjectPathEntry() {
        return ((IpsProject)getIpsProject()).getIpsObjectPathInternal().getEntry(getName());
    }

    @Override
    public IIpsPackageFragment getIpsPackageFragment(String name) {
        if (isValidIpsPackageFragmentName(name)) {
            return newIpsPackageFragment(name);
        }
        return null;
    }

    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment
     * or a valid package package fragment name according to
     * <code>JavaConventions.validatePackageName</code>.
     */
    protected boolean isValidIpsPackageFragmentName(String name) {
        try {
            return !getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg();
        } catch (CoreException e) {
            // nothing to do, will return false
        }
        return false;
    }

    protected abstract IIpsPackageFragment newIpsPackageFragment(String name);

    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
        if (file == null) {
            return null;
        }
        return file.getIpsObject();
    }

    @Override
    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) throws CoreException {
        IIpsObjectPathEntry entry = getIpsObjectPathEntry();
        if (entry == null) {
            return null;
        }
        return entry.findIpsSrcFile(qnt);
    }

    @Override
    public List<IIpsSrcFile> findAllIpsSrcFiles(IpsObjectType type) throws CoreException {
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        findIpsSourceFiles(type, null, result);
        return result;
    }

    abstract void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result)
            throws CoreException;

}
