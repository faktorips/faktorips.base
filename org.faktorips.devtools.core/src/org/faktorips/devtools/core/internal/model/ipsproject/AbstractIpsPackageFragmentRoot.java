/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        return (IIpsProject)parent;
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
    public List<IIpsSrcFile> findAllIpsSrcFiled(IpsObjectType type) throws CoreException {
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        findIpsSourceFiles(type, null, result);
        return result;
    }

    abstract void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result)
            throws CoreException;

}
