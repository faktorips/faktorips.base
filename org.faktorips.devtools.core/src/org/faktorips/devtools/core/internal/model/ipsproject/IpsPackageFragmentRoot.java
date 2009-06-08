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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.ArgumentCheck;

/**
 */
public class IpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot implements IIpsPackageFragmentRoot {

    /**
     * Creates a new ips package fragment root with the indicated parent and name.
     */
    IpsPackageFragmentRoot(IpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * Returns the artefact destination for the artefacts generated on behalf of the ips objects
     * within this ips package fragment root.
     */
    public IFolder getArtefactDestination(boolean derived) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        if(derived){
            return entry.getOutputFolderForDerivedJavaFiles();
        }
        return entry.getOutputFolderForMergableJavaFiles();
    }

    /**
     * A root fragment exists if the underlying resource exists and the root fragment is on the
     * object path.
     * <p>
     * Overridden method.
     *
     * @see org.faktorips.devtools.core.model.IIpsElement#exists()
     */
    public boolean exists() {
        if (!getCorrespondingResource().exists()) {
            return false;
        }
        try {
            IIpsObjectPath path = ((IpsProject)getIpsProject()).getIpsObjectPathInternal();
            return path.getEntry(getName())!=null;
        }
        catch (CoreException e1) {
            IpsPlugin.log(e1);
            return false;
        }
    }

    /**
     * {@inheritDoc} IpsPackageFragments are always returned, whether they are output locations of
     * the javaproject corresponding to this roots IpsProject or not.
     */
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        List list = getIpsPackageFragmentsAsList();
        return (IIpsPackageFragment[])list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment[] getSortedIpsPackageFragments() throws CoreException {
        IpsPackageNameComparator comparator = new IpsPackageNameComparator(false);
        List sortedPacks = getIpsPackageFragmentsAsList();
        Collections.sort(sortedPacks, comparator);

        return (IIpsPackageFragment[])sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
    }

    /**
     * Get all IIpsPackageFragments of a IpsRootPackageFragment as List.
     *
     * @return List List of IIpsPackageFragments
     * @throws CoreException
     */
    private List getIpsPackageFragmentsAsList() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        List list = new ArrayList();
        list.add(new IpsPackageFragment(this, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE)); // add the default package
        getIpsPackageFragments(folder, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE, list);
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        List childResources = new ArrayList();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isPackageFragment(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];
        return (IResource[])childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a folder that corresponds to an IpsPackageFragment
     * contained in this IpsPackageFragmentRoot, <code>false</code> otherwise.
     */
    private boolean isPackageFragment(IResource res) throws CoreException {
        IIpsPackageFragment[] frags = getIpsPackageFragments();
        for (int i = 0; i < frags.length; i++) {
            if (frags[i].getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        return false;
    }

    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new IpsPackageFragment(this, name);
    }

    /*
     * Creates the packages based on the contents of the given platform folder and adds them to the
     * list. This is an application of the collecting parameter pattern.
     */
    private void getIpsPackageFragments(IFolder folder, String namePrefix, List packs) throws CoreException {
        IResource[] resources = folder.members();
        for (int i = 0; i < resources.length; i++) {
            if (resources[i].getType() == IResource.FOLDER) {
                String name = resources[i].getName();
                if (isValidIpsPackageFragmentName(name)) {
                    name = namePrefix + name;
                    // package name is not the platform folder name, but the concatenation
                    // of platform folder names starting at the root folder separated by dots
                    packs.add(new IpsPackageFragment(this, name));
                    getIpsPackageFragments((IFolder)resources[i], name + ".", packs); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {
        
        if (!isValidIpsPackageFragmentName(name)) {
            return null;
        }
        IFolder folder = (IFolder)getCorrespondingResource();
        StringTokenizer tokenizer = new StringTokenizer(name, "."); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            folder = folder.getFolder(tokenizer.nextToken());
            if (!folder.exists()) {
                folder.create(force, true, monitor);
            }
        }
        return getIpsPackageFragment(name);
    }

    /**
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        IProject project = (IProject)getParent().getCorrespondingResource();
        return project.getFolder(getName());
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragments();
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$
    }

    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List result) throws CoreException {
        if (!exists()) {
            return;
        }
        if(packageFragment == null){
            IIpsPackageFragment[] packs = this.getIpsPackageFragments();
            for (int i = 0; i < packs.length; i++) {
                ((IpsPackageFragment)packs[i]).findIpsSourceFiles(type, result);
            }
            return;
        }
        IpsPackageFragment ipsPackageFragment = (IpsPackageFragment)getIpsPackageFragment(packageFragment);
        if(ipsPackageFragment != null){
            ipsPackageFragment.findIpsSourceFiles(type, result);
        }
    }

    /**
     * Searches all objects of the given type starting with the given prefix in this root folder and
     * adds them to the result.
     *
     * @throws NullPointerException if either type, prefix or result is null.
     * @throws CoreException if an error occurs while searching.
     */
    public void findIpsSourceFilesStartingWithInternal(IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IIpsPackageFragment[] packs = getIpsPackageFragments();
        for (int i = 0; i < packs.length; i++) {
            ((IpsPackageFragment)packs[i]).findIpsSourceFilesStartingWith(type, prefix, ignoreCase, result);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getJavaBasePackageNameForGeneratedJavaClasses() throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaBasePackageNameForExtensionJavaClasses() throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsArchive getIpsArchive() {
        return null;
    }

}
