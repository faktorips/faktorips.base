package org.faktorips.devtools.htmlexport.helper;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

public class Util {
    public static String getIpsPackageName(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage())
            return "(default package)";
        return ipsPackageFragment.getName();
    }
}
