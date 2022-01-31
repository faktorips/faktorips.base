/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Abstract implementation of the IIpsSearchScope
 * 
 * @author dicker
 */
public abstract class AbstractIpsSearchScope implements IIpsSearchScope {

    @Override
    public Set<IIpsSrcFile> getSelectedIpsSrcFiles() throws CoreRuntimeException {
        Set<IIpsSrcFile> srcFiles = new HashSet<>();

        for (IResource resource : getSelectedResources()) {
            addResource(srcFiles, resource);
        }

        return srcFiles;
    }

    private List<IResource> getSelectedResources() {

        List<IResource> resources = new ArrayList<>();

        for (Object object : getSelectedObjects()) {
            IResource resource = getResource(object);

            if (resource == null) {
                continue;
            }

            if (resource.isAccessible()) {
                resources.add(resource);
            }

        }
        return resources;
    }

    /**
     * returns a List of Names for the Label of the Result of the search
     */
    protected List<String> getNamesOfSelectedObjects() {

        List<String> names = new ArrayList<>();

        for (IResource resource : getSelectedResources()) {
            names.add(resource.getName());
        }

        return names;

    }

    @Override
    public String getScopeDescription() {

        List<String> namesOfSelectedObjects = getNamesOfSelectedObjects();

        int countSelectedResources = namesOfSelectedObjects.size();
        if (countSelectedResources == 0) {
            return Messages.IpsSearchScope_undefinedScope;
        }

        String scopeType = getScopeTypeLabel(countSelectedResources == 1);

        switch (countSelectedResources) {
            case 1:
                return Messages.bind(Messages.IpsSearchScope_scopeWithOneSelectedElement, new String[] { scopeType,
                        namesOfSelectedObjects.get(0) });

            case 2:
                return Messages.bind(Messages.IpsSearchScope_scopeWithTwoSelectedElements, new String[] { scopeType,
                        namesOfSelectedObjects.get(0), namesOfSelectedObjects.get(1) });

            default:
                return Messages.bind(Messages.IpsSearchScope_scopeWithMoreThanTwoSelectedElements, new String[] {
                        scopeType, namesOfSelectedObjects.get(0), namesOfSelectedObjects.get(1) });
        }
    }

    /**
     * returns the Type-Label of the Scope from the subclasses
     */
    protected abstract String getScopeTypeLabel(boolean singular);

    private void addResource(Set<IIpsSrcFile> srcFiles, IResource resource) throws CoreRuntimeException {
        IIpsElement element = resource.getAdapter(IIpsElement.class);

        if (element != null) {
            addSrcFilesOfElement(srcFiles, element);
            return;
        }

        IIpsPackageFragmentRoot storage = getPackageFragmentRootFromIpsStorage(resource);

        if (storage != null) {
            addSrcFilesOfElement(srcFiles, storage);
        }

        // TODO aufpassen: Parent vom IpsPackageFragmentRoot muss nicht unbedingt das Projekt sein
        // ==> im projekt alle IPFR durchgehen!
    }

    private IIpsPackageFragmentRoot getPackageFragmentRootFromIpsStorage(IResource resource) {

        IProject project = resource.getProject();

        IIpsProject ipsProject = (IIpsProject)project.getAdapter(IIpsElement.class);

        if (ipsProject == null) {
            return null;
        }

        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot ipsPackageFragmentRoot : ipsPackageFragmentRoots) {
            if (ipsPackageFragmentRoot.isBasedOnIpsArchive()) {
                String archiveLocation = ipsPackageFragmentRoot.getName();
                String resourceLocation = resource.getName();

                if (archiveLocation.equals(resourceLocation)) {
                    return ipsPackageFragmentRoot;
                }
            }
        }
        return null;
    }

    private IResource getResource(Object object) {
        if (object instanceof IResource) {
            return (IResource)object;
        }
        if (object instanceof IIpsObjectPart) {
            object = ((IIpsObjectPart)object).getIpsObject();
        }
        if (object instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)object;

            return adaptable.getAdapter(IResource.class);
        }
        return null;
    }

    protected abstract List<?> getSelectedObjects();

    private void addSrcFilesOfElement(Set<IIpsSrcFile> srcFiles, IIpsElement element) throws CoreRuntimeException {
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)element;
            srcFiles.add(srcFile);
            return;
        }

        if (element instanceof IIpsProject) {
            IIpsProject ipsProject = (IIpsProject)element;

            for (AResource resource : ipsProject.getProject()) {
                addResource(srcFiles, resource.unwrap());
            }

            /*
             * IIpsArchiveEntry[] archiveEntries =
             * ipsProject.getIpsObjectPath().getArchiveEntries(); for (IIpsArchiveEntry
             * iIpsArchiveEntry : archiveEntries) { iIpsArchiveEntry. }
             */

            IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
            for (IIpsPackageFragmentRoot iIpsPackageFragmentRoot : ipsPackageFragmentRoots) {
                addSrcFilesOfElement(srcFiles, iIpsPackageFragmentRoot);
            }

            return;
        }

        if (element instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot packageFragmentRoot = (IIpsPackageFragmentRoot)element;

            IIpsPackageFragment[] ipsPackageFragments = packageFragmentRoot.getIpsPackageFragments();

            for (IIpsPackageFragment packageFragment : ipsPackageFragments) {
                addSrcFilesOfElement(srcFiles, packageFragment);
            }
            return;
        }
        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)element;

            srcFiles.addAll(Arrays.asList(packageFragment.getIpsSrcFiles()));
        }
    }

}