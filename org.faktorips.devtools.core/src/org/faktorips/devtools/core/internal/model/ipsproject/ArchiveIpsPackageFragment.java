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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.ArchiveIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentSortDefinition;

/**
 * 
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragment extends AbstractIpsPackageFragment implements IIpsPackageFragment {

    public ArchiveIpsPackageFragment(ArchiveIpsPackageFragmentRoot root, String name) {
        super(root, name);
    }

    @Override
    public boolean exists() {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getRoot();
        if (!root.exists()) {
            return false;
        }
        try {
            IIpsArchive archive = root.getIpsArchive();
            if (archive == null) {
                return false;
            }
            return archive.containsPackage(getName());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    public IIpsPackageFragment[] getChildIpsPackageFragments() throws CoreException {

        List list = getChildIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
    }

    public IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive == null) {
            return new IIpsSrcFile[0];
        }
        Set set = archive.getQNameTypes(getName());
        IIpsSrcFile[] srcFiles = new IIpsSrcFile[set.size()];
        int i = 0;
        for (Iterator it = set.iterator(); it.hasNext(); i++) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            srcFiles[i] = new ArchiveIpsSrcFile(this, qnt.getFileName());
        }
        return srcFiles;
    }

    public IResource[] getNonIpsResources() throws CoreException {
        return new IResource[0];
    }

    @Override
    public IIpsSrcFile getIpsSrcFile(String name) {
        return new ArchiveIpsSrcFile(this, name);
    }

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

    public IIpsSrcFile createIpsFile(String name, String content, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    public IIpsSrcFile createIpsFile(IpsObjectType type, String ipsObjectName, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    public IIpsSrcFile createIpsFileFromTemplate(String name,
            IIpsObject template,
            GregorianCalendar date,
            boolean force,
            IProgressMonitor monitor) throws CoreException {

        throw newCantModifyPackageStoredInArchive();
    }

    public IIpsPackageFragment createSubPackage(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        throw new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    private CoreException newCantModifyPackageStoredInArchive() {
        return new CoreException(new IpsStatus("Can't modifiy package stored in an archive.")); //$NON-NLS-1$
    }

    public IResource getCorrespondingResource() {
        return null;
    }

    @Override
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        findIpsSourceFilesInternal(type, result, true);
    }

    @Override
    public void findIpsSourceFiles(IpsObjectType type, List result) throws CoreException {
        findIpsSourceFilesInternal(type, result, false);
    }

    private void findIpsSourceFilesInternal(IpsObjectType type, List result, boolean asIpsObject) throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive == null) {
            return;
        }
        Set set = archive.getQNameTypes(getName());
        for (Iterator it = set.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            if (qnt.getIpsObjectType() == type) {
                IIpsSrcFile ipsSrcFile = getIpsSrcFile(qnt.getFileName());
                if (asIpsObject) {
                    result.add(ipsSrcFile.getIpsObject());
                } else {
                    result.add(ipsSrcFile);
                }
            }
        }
    }

    public void findIpsObjects(List result) throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        IIpsArchive archive = root.getIpsArchive();
        if (archive == null) {
            return;
        }
        Set set = archive.getQNameTypes(getName());
        for (Iterator it = set.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            result.add(getIpsSrcFile(qnt.getFileName()).getIpsObject());
        }
    }

    public IIpsPackageFragmentSortDefinition getSortDefinition() {
        // TODO the sort definition for archives needs to be implemented
        return null;
    }

    public IIpsPackageFragment[] getSortedChildIpsPackageFragments() throws CoreException {
        // TODO Sort IpsPackageFragments by IpsPackageFragment.SORT_ORDER_FILE_NAME
        List sortedPacks = getChildIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all children of type IIpsPackageFragment.
     * 
     * @return IpsPackageFragments as List.
     * @throws CoreException
     */
    private List getChildIpsPackageFragmentsAsList() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        String[] packNames = root.getIpsArchive().getNonEmptySubpackages(getName());

        List list = new ArrayList(packNames.length);

        for (int i = 0; i < packNames.length; ++i) {
            String element = packNames[i];
            list.add(new ArchiveIpsPackageFragment(root, element));
        }

        return list;
    }

    /**
     * This method is not supported for archives and throws a {@link RuntimeException} if called.
     */
    public void setSortDefinition(IIpsPackageFragmentSortDefinition newDefinition) throws CoreException {
        throw new RuntimeException("Can't set the sort definition in archives!"); //$NON-NLS-1$
    }

    /**
     * This method returns null since this archive package fragment doesn't have a sort order file.
     */
    public IFile getSortOrderFile() {
        return null;
    }

    public boolean hasChildIpsPackageFragments() throws CoreException {
        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)getParent();
        String[] packNames = root.getIpsArchive().getNonEmptySubpackages(getName());

        return packNames.length > 0;
    }

    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
