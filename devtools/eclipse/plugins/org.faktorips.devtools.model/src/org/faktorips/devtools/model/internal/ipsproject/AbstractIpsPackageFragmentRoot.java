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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.IpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

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
        return IIpsObjectPathEntry.TYPE_SRC_FOLDER.equals(getIpsObjectPathEntry().getType());
    }

    @Override
    public boolean isBasedOnIpsArchive() {
        return IIpsObjectPathEntry.TYPE_ARCHIVE.equals(getIpsObjectPathEntry().getType());
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
        } catch (IpsException e) {
            // nothing to do, will return false
        }
        return false;
    }

    protected abstract IIpsPackageFragment newIpsPackageFragment(String name);

    @Override
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) {
        IIpsSrcFile file = findIpsSrcFile(new QualifiedNameType(qualifiedName, type));
        if (file == null) {
            return null;
        }
        return file.getIpsObject();
    }

    @Override
    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) {
        IIpsObjectPathEntry entry = getIpsObjectPathEntry();
        if (entry == null) {
            return null;
        }
        return entry.findIpsSrcFile(qnt);
    }

    @Override
    public List<IIpsSrcFile> findAllIpsSrcFiles(IpsObjectType type) {
        ArrayList<IIpsSrcFile> result = new ArrayList<>();
        findIpsSourceFiles(type, null, result);
        return result;
    }

    abstract void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result)
            throws IpsException;

}
