/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model.scope;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public abstract class AbstractModelSearchScope implements ModelSearchScope {

    @Override
    public Set<IIpsSrcFile> getSelectedIpsSrcFiles() throws CoreException {
        Set<IIpsSrcFile> srcFiles = new HashSet<IIpsSrcFile>();

        List<?> selectedObjects = getSelectedObjects();

        for (Object object : selectedObjects) {
            IResource resource = getResource(object);

            if (resource == null) {
                continue;
            }
            addResource(srcFiles, resource);
        }

        return srcFiles;
    }

    private void addResource(Set<IIpsSrcFile> srcFiles, IResource resource) throws CoreException {
        IIpsElement element = (IIpsElement)resource.getAdapter(IIpsElement.class);

        addSrcFilesOfElement(srcFiles, element);
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

    protected void addSrcFilesOfElement(Set<IIpsSrcFile> srcFiles, IIpsElement element) throws CoreException {
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