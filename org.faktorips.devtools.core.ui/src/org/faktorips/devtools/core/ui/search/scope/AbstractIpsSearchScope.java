/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Abstract implementation of the IIpsSearchScope
 * 
 * @author dicker
 */
public abstract class AbstractIpsSearchScope implements IIpsSearchScope {

    @Override
    public Set<IIpsSrcFile> getSelectedIpsSrcFiles() throws CoreException {
        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();

        for (IResource resource : getSelectedResources()) {
            addResource(srcFiles, resource);
        }

        return srcFiles;
    }

    private List<IResource> getSelectedResources() {

        List<IResource> resources = new ArrayList<IResource>();

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

        List<String> names = new ArrayList<String>();

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

    private void addResource(Set<IIpsSrcFile> srcFiles, IResource resource) throws CoreException {
        IIpsElement element = (IIpsElement)resource.getAdapter(IIpsElement.class);

        if (element != null) {
            addSrcFilesOfElement(srcFiles, element);
            return;
        }

        IIpsArchive archive = getIpsArchive(resource);

        if (archive != null) {
            addSrcFilesOfElement(srcFiles, archive.getRoot());
        }

        // TODO aufpassen: Parent vom IpsPackageFragmentRoot muss nicht unbedingt das Projekt sein
        // ==> im projekt alle IPFR durchgehen!
    }

    private IIpsArchive getIpsArchive(IResource resource) throws CoreException {

        IProject project = resource.getProject();

        IIpsProject ipsProject = (IIpsProject)project.getAdapter(IIpsElement.class);

        IIpsPackageFragmentRoot[] ipsPackageFragmentRoots = ipsProject.getIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot ipsPackageFragmentRoot : ipsPackageFragmentRoots) {
            if (ipsPackageFragmentRoot.isBasedOnIpsArchive()) {
                IPath archiveLocation = ipsPackageFragmentRoot.getIpsArchive().getLocation();
                IPath resourceLocation = resource.getLocation();

                if (resourceLocation.isPrefixOf(archiveLocation)) {
                    return ipsPackageFragmentRoot.getIpsArchive();
                }
            }
        }
        return null;
    }

    private IResource getResource(Object object) {
        if (object instanceof IResource) {
            return (IResource)object;
        }
        if (object instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)object;

            return (IResource)adaptable.getAdapter(IResource.class);
        }
        return null;
    }

    protected abstract List<?> getSelectedObjects();

    private void addSrcFilesOfElement(Set<IIpsSrcFile> srcFiles, IIpsElement element) throws CoreException {
        if (element instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)element;
            srcFiles.add(srcFile);
            return;
        }

        if (element instanceof IIpsProject) {
            IIpsProject ipsProject = (IIpsProject)element;

            for (IResource resource : ipsProject.getProject().members()) {
                addResource(srcFiles, resource);
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