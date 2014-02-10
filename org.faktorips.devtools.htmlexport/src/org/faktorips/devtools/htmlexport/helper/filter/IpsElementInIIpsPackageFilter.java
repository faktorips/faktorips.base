/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * Filter, which checks, whether an {@link IpsObject} is within the given {@link IpsPackageFragment}
 * or the is within the {@link IpsPackageFragment} with the same name of the given
 * {@link IpsPackageFragment} (necessary for {@link IpsObject}s in referenced {@link IpsProject}s
 * 
 * @author dicker
 * 
 */
public class IpsElementInIIpsPackageFilter implements IIpsElementFilter {
    private final IIpsPackageFragment ipsPackageFragment;
    private final DocumentationContext context;

    public IpsElementInIIpsPackageFilter(IIpsPackageFragment ipsPackageFragment, DocumentationContext context) {
        this.ipsPackageFragment = ipsPackageFragment;
        this.context = context;
    }

    @Override
    public boolean accept(IIpsElement ipsElement) {

        if (ipsElement instanceof IIpsSrcFile) {

            try {
                IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
                return acceptIpsObject(ipsObject);
            } catch (CoreException e) {
                context.addStatus(new IpsStatus(IStatus.WARNING, "Could not filter package", e)); //$NON-NLS-1$
                return false;
            }
        }
        if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
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
