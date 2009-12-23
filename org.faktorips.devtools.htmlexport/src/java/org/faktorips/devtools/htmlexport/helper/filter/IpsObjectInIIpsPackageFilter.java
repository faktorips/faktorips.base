package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

public class IpsObjectInIIpsPackageFilter implements IpsObjectFilter {
    private IIpsPackageFragment ipsPackageFragment;

    public IpsObjectInIIpsPackageFilter(IIpsPackageFragment ipsPackageFragment) {
        this.ipsPackageFragment = ipsPackageFragment;
    }

    public boolean accept(IIpsElement object) {
        return ((IIpsObject)object).getIpsPackageFragment().equals(ipsPackageFragment);
    }
}
