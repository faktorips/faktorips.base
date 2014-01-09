/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;

/**
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragment extends AbstractIpsPackageFragment {

    public LibraryIpsPackageFragment(LibraryIpsPackageFragmentRoot root, String name) {
        super(root, name);
    }

    @Override
    public boolean exists() {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getRoot();
        if (!root.exists()) {
            return false;
        }
        try {
            IIpsStorage storage = root.getIpsStorage();
            if (storage == null) {
                return false;
            }
            return storage.containsPackage(getName());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    @Override
    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {

        List<IIpsPackageFragment> list = getChildIpsPackageFragmentsAsList();

        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    @Override
    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getParent();
        IIpsStorage archive = root.getIpsStorage();
        if (archive == null) {
            return new IIpsSrcFile[0];
        }
        Set<QualifiedNameType> set = archive.getQNameTypes(getName());
        IIpsSrcFile[] srcFiles = new IIpsSrcFile[set.size()];
        int i = 0;
        for (QualifiedNameType qnt : set) {
            srcFiles[i++] = new LibraryIpsSrcFile(this, qnt.getFileName());
        }
        return srcFiles;
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        return new IResource[0];
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String name) {
        return new LibraryIpsSrcFile(this, name);
    }

    @Override
    public IIpsSrcFile createIpsFile(String name, InputStream source, boolean force, IProgressMonitor monitor)
            throws CoreException {
        try {
            if (source != null) {
                source.close();
            }
        } catch (IOException e) {
            throw new CoreException(new IpsStatus("Unable to close contents.", e)); //$NON-NLS-1$
        }
        throw newCantModifyPackageStoredInArchive();
    }

    @Override
    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    @Override
    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    @Override
    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar olddate,
            GregorianCalendar newdate,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    @Override
    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        throw new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    private CoreException newCantModifyPackageStoredInArchive() {
        return new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    @Override
    public IResource getCorrespondingResource() {
        return null;
    }

    @Override
    public void findIpsObjects(IpsObjectType type, List<IIpsObject> result) throws CoreException {
        ArrayList<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();
        findIpsSourceFiles(type, ipsSrcFiles);
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            result.add(ipsSrcFile.getIpsObject());
        }
    }

    @Override
    public void findIpsSourceFiles(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException {
        findIpsSourceFilesInternal(type, result);
    }

    private void findIpsSourceFilesInternal(IpsObjectType type, List<IIpsSrcFile> result) throws CoreException {
        Set<QualifiedNameType> set = getQualifiedNames();
        for (QualifiedNameType qnt : set) {
            if (qnt.getIpsObjectType() == type) {
                IIpsSrcFile ipsSrcFile = getIpsSrcFile(qnt.getFileName());
                result.add(ipsSrcFile);
            }
        }
    }

    private Set<QualifiedNameType> getQualifiedNames() throws CoreException {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getParent();
        IIpsStorage archive = root.getIpsStorage();
        if (archive == null) {
            return Collections.emptySet();
        }
        Set<QualifiedNameType> set = archive.getQNameTypes(getName());
        return set;
    }

    public void findIpsObjects(List<IIpsObject> result) throws CoreException {
        Set<QualifiedNameType> set = getQualifiedNames();
        for (QualifiedNameType qnt : set) {
            result.add(getIpsSrcFile(qnt.getFileName()).getIpsObject());
        }
    }

    @Override
    public IIpsPackageFragmentSortDefinition getSortDefinition() {
        // TODO the sort definition for archives needs to be implemented
        return null;
    }

    @Override
    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException {
        // TODO Sort IpsPackageFragments by IpsPackageFragment.SORT_ORDER_FILE_NAME
        List<IIpsPackageFragment> sortedPacks = getChildIpsPackageFragmentsAsList();
        return sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all children of type IIpsPackageFragment.
     */
    private List<IIpsPackageFragment> getChildIpsPackageFragmentsAsList() throws CoreException {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getParent();
        String[] packNames = root.getIpsStorage().getNonEmptySubpackages(getName());

        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>(packNames.length);

        for (int i = 0; i < packNames.length; ++i) {
            String element = packNames[i];
            list.add(new LibraryIpsPackageFragment(root, element));
        }

        return list;
    }

    /**
     * This method is not supported for archives and throws a {@link RuntimeException} if called.
     */
    @Override
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException {
        throw new RuntimeException("Can't set the sort definition in archives!"); //$NON-NLS-1$
    }

    /**
     * This method returns null since this archive package fragment doesn't have a sort order file.
     */
    @Override
    public IFile getSortOrderFile() {
        return null;
    }

    @Override
    public boolean hasChildIpsPackageFragments() throws CoreException {
        LibraryIpsPackageFragmentRoot root = (LibraryIpsPackageFragmentRoot)getParent();
        String[] packNames = root.getIpsStorage().getNonEmptySubpackages(getName());

        return packNames.length > 0;
    }

    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("Archived IPS Packages cannot be deleted."); //$NON-NLS-1$
    }

}
