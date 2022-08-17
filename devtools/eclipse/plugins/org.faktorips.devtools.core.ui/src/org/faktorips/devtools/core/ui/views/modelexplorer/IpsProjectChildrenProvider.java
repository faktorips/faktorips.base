/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Provides the children of an {@link IIpsProject} for a view of the model.
 * 
 * @author dicker
 */
public class IpsProjectChildrenProvider implements IChildrenProvider<IIpsProject> {

    @Override
    public Object[] getChildren(IIpsProject project) {

        List<Object> result = new ArrayList<>();

        List<IIpsObjectPathContainer> containerEntries = getContainerEntries(project);
        result.addAll(containerEntries);

        List<IIpsPackageFragmentRoot> existingRoots = getExistingRoots(project);
        result.addAll(existingRoots);

        List<IResource> nonIpsResources = Wrappers.unwrap(project.getNonIpsResources()).asList();

        result.addAll(nonIpsResources);

        return result.toArray();
    }

    private List<IIpsPackageFragmentRoot> getExistingRoots(IIpsProject project) {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots(false);
        List<IIpsPackageFragmentRoot> existingRoots = new ArrayList<>();
        for (IIpsPackageFragmentRoot root : roots) {
            if (root.exists()) {
                existingRoots.add(root);
            }
        }
        return existingRoots;
    }

    /* private */List<IIpsObjectPathContainer> getContainerEntries(IIpsProject project) {
        IIpsObjectPath ipsObjectPath = project.getIpsObjectPath();
        IIpsObjectPathEntry[] entries = ipsObjectPath.getEntries();

        List<IIpsObjectPathContainer> containerEntries = new ArrayList<>();
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
