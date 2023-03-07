/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Filter, which checks, whether an {@link IIpsObject} is within the given
 * {@link IIpsPackageFragment} or the is within the {@link IIpsPackageFragment} with the same name
 * of the given {@link IIpsPackageFragment} (necessary for {@link IIpsObject}s in referenced
 * {@link IIpsProject}s
 * 
 * @author dicker
 * 
 */
public class IpsElementInIIpsPackageFilter implements IIpsElementFilter {

    private final IIpsPackageFragment ipsPackageFragment;

    public IpsElementInIIpsPackageFilter(IIpsPackageFragment ipsPackageFragment) {
        this.ipsPackageFragment = ipsPackageFragment;
    }

    @Override
    public boolean accept(IIpsElement ipsElement) {

        if (ipsElement instanceof IIpsSrcFile) {

            IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
            return acceptIpsObject(ipsObject);
        }
        if (ipsElement instanceof IIpsObject ipsObject) {
            return acceptIpsObject(ipsObject);
        }
        return false;
    }

    /**
     * check, whether the IpsPackageFragment are equal or their names (because of different
     * projects)
     */
    private boolean acceptIpsObject(IIpsObject ipsObject) {
        return ipsObject.getIpsPackageFragment().equals(ipsPackageFragment)
                || ipsObject.getIpsPackageFragment().getName().equals(ipsPackageFragment.getName());
    }
}
