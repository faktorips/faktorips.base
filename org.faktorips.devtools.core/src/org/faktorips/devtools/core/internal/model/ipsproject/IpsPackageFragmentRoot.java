/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.ArgumentCheck;

public class IpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot {

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
    @Override
    public IFolder getArtefactDestination(boolean derived) throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        if (derived) {
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
    @Override
    public boolean exists() {
        if (!getCorrespondingResource().exists()) {
            return false;
        }
        IIpsObjectPath path = ((IpsProject)getIpsProject()).getIpsObjectPathInternal();
        return path.getEntry(getName()) != null;
    }

    /**
     * IpsPackageFragments are always returned, whether they are output locations of the javaproject
     * corresponding to this roots IpsProject or not.
     */
    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    @Override
    public IIpsPackageFragment[] getSortedIpsPackageFragments() throws CoreException {
        IpsPackageNameComparator comparator = new IpsPackageNameComparator(false);
        List<IIpsPackageFragment> sortedPacks = getIpsPackageFragmentsAsList();
        Collections.sort(sortedPacks, comparator);
        return sortedPacks.toArray(new IIpsPackageFragment[sortedPacks.size()]);
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
        IProject project = (IProject)getParent().getCorrespondingResource();
        return project.getFolder(getName());
    }

    @Override
    public IIpsElement[] getChildren() throws CoreException {
        return getIpsPackageFragments();
    }

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

    public String getJavaBasePackageNameForGeneratedJavaClasses() throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForMergableJavaClasses();
    }

    public String getJavaBasePackageNameForExtensionJavaClasses() throws CoreException {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPathEntry();
        return entry.getBasePackageNameForDerivedJavaClasses();
    }

    @Override
    public IIpsArchive getIpsArchive() {
        return null;
    }

    @Override
    public boolean isContainedInArchive() {
        return false;
    }

}
