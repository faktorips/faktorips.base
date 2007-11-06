/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community)
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 *
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot implements IIpsPackageFragmentRoot {

    private IFile archiveFile;

    /**
     * @param parent
     * @param name
     */
    public ArchiveIpsPackageFragmentRoot(IFile archiveFile) {
        super(IpsPlugin.getDefault().getIpsModel().getIpsProject(archiveFile.getProject()), archiveFile.getName());
        this.archiveFile = archiveFile;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnSourceFolder() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnIpsArchive() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("IpsPackageFragmentRoot does not exist!")); //$NON-NLS-1$
        }
        IIpsObjectPathEntry[] entries = ((IpsProject)getIpsProject()).getIpsObjectPathInternal().getEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getType().equals(IIpsObjectPathEntry.TYPE_ARCHIVE)) {
                IIpsArchiveEntry entry = (IIpsArchiveEntry)entries[i];
                if (entry.getArchiveFile().equals(archiveFile)) {
                    return entry;
                }
            }
        }
        throw new CoreException(new IpsStatus("No IpsObjectPathEntry found for package fragment root " + this)); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArchive getIpsArchive() throws CoreException {
        return ((IpsArchiveEntry)getIpsObjectPathEntry()).getIpsArchive();
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getArtefactDestination(boolean derived) throws CoreException {
        throw newExceptionMethodNotAvailableForArchvies();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {

        List list = getIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getSortedIpsPackageFragments() throws CoreException {
        // TODO Sort IpsPackageFragments by IpsPackageFragment.SORT_ORDER_FILE_NAME

        List sortedPacks = getIpsPackageFragmentsAsList();

        return (IIpsPackageFragment[])sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * @return
     * @throws CoreException
     */
    private List getIpsPackageFragmentsAsList() throws CoreException {

        IIpsArchive archive = getIpsArchive();
        if (archive==null) {
            return new ArrayList(0);
        }

        String[] packNames = archive.getNoneEmptyPackages();

        List list = new ArrayList(packNames.length);

        for (int i = 0; i < packNames.length; i++) {
            list.add(new ArchiveIpsPackageFragment(this, packNames[i]));
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new ArchiveIpsPackageFragment(this, name);
    }

    /**
     * {@inheritDoc}
     */
    public IResource[] getNonIpsResources() throws CoreException {

        return new IResource[0];
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newExceptionMethodNotAvailableForArchvies();
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return archiveFile;
    }

    private CoreException newExceptionMethodNotAvailableForArchvies() {
        return new CoreException(new IpsStatus("Not possible for archives because they are not modifiable.")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void findIpsObjects(IpsObjectType type, List result) throws CoreException {
        if (type==null) {
            return;
        }
        IIpsArchive archive = getIpsArchive();
        if (archive==null) {
            return;
        }
        Set qntSet = archive.getQNameTypes();
        for (Iterator it = qntSet.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            if (type.equals(qnt.getIpsObjectType())) {
                IIpsObject object = findIpsObject(qnt);
                if (object!=null) {
                    result.add(object);
                }
            }
        }
    }
    
    public void findIpsSourceFiles(IpsObjectType type, List result) throws CoreException {
        if (type==null) {
            return;
        }
        IIpsArchive archive = getIpsArchive();
        if (archive==null) {
            return;
        }
        Set qntSet = archive.getQNameTypes();
        for (Iterator it = qntSet.iterator(); it.hasNext();) {
            QualifiedNameType qnt = (QualifiedNameType)it.next();
            IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
            if (pack==null) {
                return;
            }
            IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
            if (!file.exists()) {
                result.add(file);
            }
        }
    }

    public void findIpsObjects(List result) throws CoreException {
        IIpsArchive archive = getIpsArchive();
        if (archive == null) {
            return;
        }
        
        IIpsPackageFragment[] packs = this.getIpsPackageFragments();
        for (int i = 0; i < packs.length; i++) {
            ((ArchiveIpsPackageFragment)packs[i]).findIpsObjects(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsAr.gif"); //$NON-NLS-1$
    }

}
