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
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.faktorips.util.ArgumentCheck;

public class IpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot {

    private IFolder correspondingResourceFolder;

    /**
     * Creates a new ips package fragment root with the indicated parent and name.
     */
    IpsPackageFragmentRoot(IpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * Returns the artefact destination for the artefacts generated on behalf of the ips objects within
     * this ips package fragment root.
     */
    @Override
    public IPackageFragmentRoot getArtefactDestination(boolean derived) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        IFolder folder;
        if (derived) {
            folder = entry.getOutputFolderForDerivedJavaFiles();
        } else {
            folder = entry.getOutputFolderForMergableJavaFiles();
        }
        return getIpsProject().getJavaProject().getPackageFragmentRoot(folder);
    }

    /**
     * A root fragment exists if the underlying resource exists and the root fragment is on the object
     * path.
     * <p>
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#exists()
     */
    @Override
    public boolean exists() {
        if (!getCorrespondingResource().exists()) {
            return false;
        }
        IIpsObjectPath path = ((IpsProject)getIpsProject()).getIpsObjectPathInternal();
        return path.getEntry(getName()) != null;
    }

    /**
     * IpsPackageFragments are always returned, whether they are output locations of the java project
     * corresponding to this root's IpsProject or not.
     */
    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * Get all IIpsPackageFragments of a IpsRootPackageFragment as List.
     * 
     * @return List List of IIpsPackageFragments
     */
    private List<IIpsPackageFragment> getIpsPackageFragmentsAsList() throws CoreException {
        IFolder folder = (IFolder)getCorrespondingResource();
        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>();
        // add the default package
        list.add(new IpsPackageFragment(this, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE));
        getIpsPackageFragments(folder, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE, list);
        return list;
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        IContainer cont = (IContainer)getCorrespondingResource();
        List<IResource> childResources = new ArrayList<IResource>();
        IResource[] children = cont.members();
        for (int i = 0; i < children.length; i++) {
            if (!isPackageFragment(children[i])) {
                childResources.add(children[i]);
            }
        }
        IResource[] resArray = new IResource[childResources.size()];
        return childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a folder that corresponds to an
     * IpsPackageFragment contained in this IpsPackageFragmentRoot, <code>false</code> otherwise.
     */
    private boolean isPackageFragment(IResource res) throws CoreException {
        IIpsPackageFragment[] frags = getIpsPackageFragments();
        for (IIpsPackageFragment frag : frags) {
            if (frag.getCorrespondingResource().equals(res)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new IpsPackageFragment(this, name);
    }

    /**
     * Creates the packages based on the contents of the given platform folder and adds them to the
     * list. This is an application of the collecting parameter pattern.
     */
    private void getIpsPackageFragments(IFolder folder, String namePrefix, List<IIpsPackageFragment> packs)
            throws CoreException {
        IResource[] resources = folder.members();
        for (IResource resource : resources) {
            if (resource.getType() == IResource.FOLDER) {
                String name = resource.getName();
                if (isValidIpsPackageFragmentName(name)) {
                    name = namePrefix + name;
                    // package name is not the platform folder name, but the concatenation
                    // of platform folder names starting at the root folder separated by dots
                    packs.add(new IpsPackageFragment(this, name));
                    getIpsPackageFragments((IFolder)resource, name + ".", packs); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
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

    @Override
    public IResource getCorrespondingResource() {
        if (this.correspondingResourceFolder == null) {
            IProject project = (IProject)getParent().getCorrespondingResource();
            this.correspondingResourceFolder = project.getFolder(getName());
        }
        return this.correspondingResourceFolder;
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragments();
    }

    @Override
    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result) throws CoreException {
        if (!exists()) {
            return;
        }
        if (packageFragment == null) {
            IIpsPackageFragment[] packs = this.getIpsPackageFragments();
            for (IIpsPackageFragment pack : packs) {
                ((IpsPackageFragment)pack).findIpsSourceFiles(type, result);
            }
            return;
        }
        IpsPackageFragment ipsPackageFragment = (IpsPackageFragment)getIpsPackageFragment(packageFragment);
        if (ipsPackageFragment != null) {
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
    public void findIpsSourceFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) throws CoreException {
        ArgumentCheck.notNull(type);
        ArgumentCheck.notNull(prefix);
        ArgumentCheck.notNull(result);
        if (!exists()) {
            return;
        }
        IIpsPackageFragment[] packs = getIpsPackageFragments();
        for (IIpsPackageFragment pack : packs) {
            ((IpsPackageFragment)pack).findIpsSourceFilesStartingWith(type, prefix, ignoreCase, result);
        }
    }

    public String getJavaBasePackageNameForGeneratedJavaClasses() {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    public String getJavaBasePackageNameForExtensionJavaClasses() {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }

    @Override
    public IIpsStorage getIpsStorage() {
        return null;
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
    }

    @Override
    public void delete() throws CoreException {
        /*
         * Just deleting the default package fragment as all other fragments have the default package as
         * parent and will be deleted automatically.
         */
        getDefaultIpsPackageFragment().delete();
        getCorrespondingResource().delete(true, null);
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Package fragment root names cannot be changed."); //$NON-NLS-1$
    }
}
