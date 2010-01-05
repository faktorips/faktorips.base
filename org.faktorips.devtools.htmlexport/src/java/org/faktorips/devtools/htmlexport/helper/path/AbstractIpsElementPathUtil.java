package org.faktorips.devtools.htmlexport.helper.path;

import java.io.File;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

abstract class AbstractIpsElementPathUtil<T extends IIpsElement> implements IpsElementPathUtil {
    protected static final String PATH_UP = "../";

    protected T ipsElement;

    protected AbstractIpsElementPathUtil(T ipsElement) {
        this.ipsElement = ipsElement;
    }

    protected AbstractIpsElementPathUtil() {

    }

    protected String getPackageFragmentPathToRoot(IIpsPackageFragment packageFragment) {
        if (packageFragment.isDefaultPackage())
            return "";

        StringBuilder builder = new StringBuilder();
        
        builder.append(PATH_UP);
        packageFragment = packageFragment.getParentIpsPackageFragment();
        while (packageFragment.getParentIpsPackageFragment() != null) {
            builder.append(PATH_UP);
            packageFragment = packageFragment.getParentIpsPackageFragment();
        }
        return builder.toString();
    }

    protected String getPackageFragmentPathFromRoot(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage())
            return "";
        return ipsPackageFragment.getRelativePath().toOSString() + File.separator;
    }

    public String getLinkText(boolean withImage) {
        return ipsElement.getName();
    }

    public T getIpsElement() {
        return ipsElement;
    }

    public String getPathFromRoot(LinkedFileTypes linkedFileType) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPackageFragmentPathFromRoot(getIpsPackageFragment()));
        builder.append(linkedFileType.getPrefix());
        builder.append(getFileName());
        builder.append(linkedFileType.getSuffix());
        
        return builder.toString();
    }

    protected String getFileName() {
        return getIpsElement().getName();
    }

    protected abstract IIpsPackageFragment getIpsPackageFragment();
}
