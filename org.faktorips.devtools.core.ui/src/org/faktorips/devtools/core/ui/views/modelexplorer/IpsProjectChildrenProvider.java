/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
