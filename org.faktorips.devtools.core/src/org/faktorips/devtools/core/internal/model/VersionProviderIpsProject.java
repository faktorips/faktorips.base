/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * 
 * Class implementing {@link IVersionProvider}. {@link VersionProviderIpsProject} is designed to
 * provide the adequate versions for specific {@link IpsObjectPartContainer} in ipsProject files.
 */
public class VersionProviderIpsProject implements IVersionProvider {

    @Override
    public IVersion getVersion(String versionAsString) {
        return new Version(versionAsString);
    }

    @Override
    public IVersion<?> getModelVersion(IIpsPackageFragmentRoot packageFragmentRoot) {
        IpsObjectPartContainer partContainer = getIpsObjectPartContainer(packageFragmentRoot);
        if (partContainer != null) {
            partContainer.getSinceVersion();
        }
        return null;
    }

    @Override
    public void setModelVersion(IIpsPackageFragmentRoot packageFragmentRoot, IVersion version) {
        IpsObjectPartContainer partContainer = getIpsObjectPartContainer(packageFragmentRoot);
        if (partContainer != null) {
            partContainer.setSinceVersion(version);
        }
    }

    private IpsObjectPartContainer getIpsObjectPartContainer(IIpsPackageFragmentRoot packageFragmentRoot) {
        try {
            IIpsElement[] children = packageFragmentRoot.getChildren();
            for (IIpsElement child : children) {
                if (child instanceof IpsObjectPartContainer) {
                    return ((IpsObjectPartContainer)child);
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

}
