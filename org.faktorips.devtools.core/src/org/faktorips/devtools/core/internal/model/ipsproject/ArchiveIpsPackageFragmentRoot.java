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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 *
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot implements IIpsPackageFragmentRoot {

    private IPath archivePath;

    /**
     * @param ipsProject IPS project
     * @param archivePath Path to an IPS archive
     */
    public ArchiveIpsPackageFragmentRoot(IIpsProject ipsProject, IPath archivePath) {
    	super(ipsProject, archivePath.lastSegment());
    	this.archivePath = archivePath;
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

        String[] packNames = archive.getNonEmptyPackages();

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
    	if (isExternal()) {
    		return null;
    	}
    	IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile resource = workspaceRoot.getFileForLocation(archivePath);
    	if (resource == null) {
    		resource = workspaceRoot.getFile(archivePath);
    	}
    	return resource;
    }

    private boolean isExternal() {
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	boolean isExternal = (root.getFileForLocation(archivePath) == null);
    	isExternal = isExternal && (root.getFile(archivePath).exists() == false);
    	
    	return isExternal;
    }
    
	private CoreException newExceptionMethodNotAvailableForArchvies() {
        return new CoreException(new IpsStatus("Not possible for archives because they are not modifiable.")); //$NON-NLS-1$
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
            if(!type.equals(qnt.getIpsObjectType())){
                continue;
            }
            IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
            if (pack==null) {
                return;
            }
            IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
            if (file.exists()) {
                result.add(file);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsAr.gif"); //$NON-NLS-1$
    }

}
