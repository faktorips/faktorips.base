/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Provides the children of an {@link IIpsProject} for a view of the model.
 * 
 * @author dicker
 */
public class IpsProjectChildrenProvider implements IChildrenProvider<IIpsProject> {

    @Override
    public Object[] getChildren(IIpsProject project) throws CoreException {

        List<Object> result = new ArrayList<Object>();

        List<IIpsObjectPathContainer> containerEntries = getContainerEntries(project);
        result.addAll(containerEntries);

        List<IIpsPackageFragmentRoot> existingRoots = getExistingRoots(project);
        result.addAll(existingRoots);

        List<IResource> nonIpsResources = Arrays.asList(project.getNonIpsResources());
        result.addAll(nonIpsResources);

        return result.toArray();
    }

    private List<IIpsPackageFragmentRoot> getExistingRoots(IIpsProject project) {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        List<IIpsPackageFragmentRoot> existingRoots = new ArrayList<IIpsPackageFragmentRoot>();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.exists()) {
                existingRoots.add(root);
            }
        }
        return existingRoots;
    }

    /* private */List<IIpsObjectPathContainer> getContainerEntries(IIpsProject project) throws CoreException {
        IIpsObjectPath ipsObjectPath = project.getIpsObjectPath();
        IIpsObjectPathEntry[] entries = ipsObjectPath.getEntries();

        List<IIpsObjectPathContainer> containerEntries = new ArrayList<IIpsObjectPathContainer>();
        for (IIpsObjectPathEntry entry : entries) {
            if (entry.isContainer()) {
                IIpsContainerEntry containerEntry = (IIpsContainerEntry)entry;
                IIpsObjectPathContainer ipsObjectPathContainer = containerEntry.getIpsObjectPathContainer();
                List<IIpsObjectPathEntry> resolveEntries = ipsObjectPathContainer.resolveEntries();
                if (!resolveEntries.isEmpty()) {
                    containerEntries.add(ipsObjectPathContainer);
                }
            }
        }
        return containerEntries;
    }

}
