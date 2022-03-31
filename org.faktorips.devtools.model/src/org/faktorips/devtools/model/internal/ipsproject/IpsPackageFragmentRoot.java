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
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.util.ArgumentCheck;

public class IpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot {

    private AFolder correspondingResourceFolder;

    /**
     * Creates a new IPS package fragment root with the indicated parent and name.
     */
    IpsPackageFragmentRoot(IIpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * Returns the artefact destination for the artefacts generated on behalf of the IPS objects
     * within this IPS package fragment root.
     */
    @Override
    public APackageFragmentRoot getArtefactDestination(boolean derived) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        AFolder folder;
        if (derived) {
            folder = entry.getOutputFolderForDerivedJavaFiles();
        } else {
            folder = entry.getOutputFolderForMergableJavaFiles();
        }
        return getIpsProject().getJavaProject().toPackageFragmentRoot(folder);
    }

    /**
     * A root fragment exists if the underlying resource exists and the root fragment is on the
     * object path.
     * <p>
     * Overridden method.
     * 
     * @see org.faktorips.devtools.model.IIpsElement#exists()
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
     * IpsPackageFragments are always returned, whether they are output locations of the java
     * project corresponding to this root's IpsProject or not.
     */
    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    /**
     * Get all IIpsPackageFragments of a IpsRootPackageFragment as List.
     * 
     * @return List List of IIpsPackageFragments
     */
    private List<IIpsPackageFragment> getIpsPackageFragmentsAsList() {
        AFolder folder = (AFolder)getCorrespondingResource();
        List<IIpsPackageFragment> list = new ArrayList<>();
        // add the default package
        list.add(new IpsPackageFragment(this, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE));
        getIpsPackageFragments(folder, IIpsPackageFragment.NAME_OF_THE_DEFAULT_PACKAGE, list);
        return list;
    }

    @Override
    public AResource[] getNonIpsResources() {
        AContainer cont = (AContainer)getCorrespondingResource();
        List<AResource> childResources = new ArrayList<>();
        for (AResource child : cont) {
            if (!isPackageFragment(child)) {
                childResources.add(child);
            }
        }
        AResource[] resArray = new AResource[childResources.size()];
        return childResources.toArray(resArray);
    }

    /**
     * Returns <code>true</code> if the given IResource is a folder that corresponds to an
     * IpsPackageFragment contained in this IpsPackageFragmentRoot, <code>false</code> otherwise.
     */
    private boolean isPackageFragment(AResource res) {
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
    private void getIpsPackageFragments(AFolder folder, String namePrefix, List<IIpsPackageFragment> packs) {
        for (AResource resource : folder) {
            if (resource.getType() == AResourceType.FOLDER) {
                String name = resource.getName();
                if (isValidIpsPackageFragmentName(name)) {
                    name = namePrefix + name;
                    // package name is not the platform folder name, but the concatenation
                    // of platform folder names starting at the root folder separated by dots
                    packs.add(new IpsPackageFragment(this, name));
                    getIpsPackageFragments((AFolder)resource, name + ".", packs); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor) {
        if (!isValidIpsPackageFragmentName(name)) {
            return null;
        }
        AFolder folder = (AFolder)getCorrespondingResource();
        StringTokenizer tokenizer = new StringTokenizer(name, "."); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            folder = folder.getFolder(tokenizer.nextToken());
            if (!folder.exists()) {
                folder.create(monitor);
            }
        }
        return getIpsPackageFragment(name);
    }

    @Override
    public AResource getCorrespondingResource() {
        if (this.correspondingResourceFolder == null) {
            AProject project = (AProject)getParent().getCorrespondingResource();
            this.correspondingResourceFolder = project.getFolder(getName());
        }
        return this.correspondingResourceFolder;
    }

    @Override
    public IIpsElement[] getChildren() {
        return getIpsPackageFragments();
    }

    @Override
    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result) {
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
     * @throws IpsException if an error occurs while searching.
     */
    public void findIpsSourceFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result) {
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
    public void delete() {
        /*
         * Just deleting the default package fragment as all other fragments have the default
         * package as parent and will be deleted automatically.
         */
        getDefaultIpsPackageFragment().delete();
        getCorrespondingResource().delete(null);
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Package fragment root names cannot be changed."); //$NON-NLS-1$
    }
}
