/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;

/**
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot {

    private IIpsStorage storage;

    public LibraryIpsPackageFragmentRoot(IIpsProject ipsProject, IIpsStorage storage) {
        super(ipsProject, storage.getName());
        this.storage = storage;
    }

    @Override
    public IIpsStorage getIpsStorage() {
        return storage;
    }

    @Override
    public boolean exists() {
        if (getIpsStorage() == null) {
            return false;
        }
        return getIpsStorage().exists();
    }

    @Override
    public IFolder getArtefactDestination(boolean derived) throws CoreException {
        throw newExceptionMethodNotAvailableForArchvies();
    }

    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    @Override
    public IIpsPackageFragment[] getSortedIpsPackageFragments() throws CoreException {
        // TODO Sort IpsPackageFragments by IpsPackageFragment.SORT_ORDER_FILE_NAME
        List<IIpsPackageFragment> sortedPacks = getIpsPackageFragmentsAsList();
        return sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    private List<IIpsPackageFragment> getIpsPackageFragmentsAsList() throws CoreException {
        if (getIpsStorage() == null) {
            return new ArrayList<IIpsPackageFragment>(0);
        }

        String[] packNames = storage.getNonEmptyPackages();
        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>(packNames.length);
        for (String packName : packNames) {
            list.add(new LibraryIpsPackageFragment(this, packName));
        }

        return list;
    }

    @Override
    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new LibraryIpsPackageFragment(this, name);
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        return new IResource[0];
    }

    @Override
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newExceptionMethodNotAvailableForArchvies();
    }

    @Override
    public IResource getCorrespondingResource() {
        return storage.getCorrespondingResource();
    }

    private CoreException newExceptionMethodNotAvailableForArchvies() {
        return new CoreException(new IpsStatus("Not possible for archives because they are not modifiable.")); //$NON-NLS-1$
    }

    @Override
    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result) throws CoreException {
        if (type == null) {
            return;
        }
        if (storage == null) {
            return;
        }
        Set<QualifiedNameType> qntSet = storage.getQNameTypes();
        for (QualifiedNameType qnt : qntSet) {
            if (!type.equals(qnt.getIpsObjectType())) {
                continue;
            }
            if (packageFragment != null && !qnt.getPackageName().equals(packageFragment)) {
                continue;
            }
            IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
            if (pack == null) {
                return;
            }
            IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
            if (file.exists()) {
                result.add(file);
            }
        }
    }

    @Override
    public boolean isContainedInArchive() {
        if (getIpsStorage() == null) {
            return false;
        }
        return !getIpsStorage().isFolder();
    }

    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("IPS Package Fragment Roots that are stored" + //$NON-NLS-1$
                " in an archive cannot be deleted."); //$NON-NLS-1$
    }

}
