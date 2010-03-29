package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * Filter, which checks, whether an {@link IpsObject} is within the given {@link IpsPackageFragment} or the is within the {@link IpsPackageFragment} with the same name of the given {@link IpsPackageFragment} (necessary for {@link IpsObject}s in referenced {@link IpsProject}s 
 * @author dicker
 *
 */
public class IpsObjectInIIpsPackageFilter implements IpsElementFilter {
    private IIpsPackageFragment ipsPackageFragment;

    public IpsObjectInIIpsPackageFilter(IIpsPackageFragment ipsPackageFragment) {
        this.ipsPackageFragment = ipsPackageFragment;
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter#accept(org.faktorips.devtools.core.model.IIpsElement)
     */
    public boolean accept(IIpsElement ipsElement) {
    	if (!(ipsElement instanceof IIpsObject)) return false;
        return ((IIpsObject)ipsElement).getIpsPackageFragment().equals(ipsPackageFragment) || ((IIpsObject)ipsElement).getIpsPackageFragment().getName().equals(ipsPackageFragment.getName());
    }
}
