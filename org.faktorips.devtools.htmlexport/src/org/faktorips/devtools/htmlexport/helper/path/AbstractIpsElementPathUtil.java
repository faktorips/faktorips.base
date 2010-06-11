package org.faktorips.devtools.htmlexport.helper.path;

import java.io.File;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * Base implementation of the {@link IpsElementPathUtil}
 * 
 * @author dicker
 * 
 * @param <T>
 */
public abstract class AbstractIpsElementPathUtil<T extends IIpsElement> implements IpsElementPathUtil {
    protected static final String PATH_UP = "../"; //$NON-NLS-1$

    protected T ipsElement;

    protected AbstractIpsElementPathUtil(T ipsElement) {
        this.ipsElement = ipsElement;
    }

    protected AbstractIpsElementPathUtil() {

    }

    /**
     * returns the relative path from the {@link IIpsPackageFragment} to the root
     * 
     * @param packageFragment
     * @return
     */
    protected String getPackageFragmentPathToRoot(IIpsPackageFragment packageFragment) {
        if (packageFragment.isDefaultPackage()) {
            return ""; //$NON-NLS-1$
        }

        StringBuilder builder = new StringBuilder();

        builder.append(PATH_UP);
        packageFragment = packageFragment.getParentIpsPackageFragment();
        while (packageFragment.getParentIpsPackageFragment() != null) {
            builder.append(PATH_UP);
            packageFragment = packageFragment.getParentIpsPackageFragment();
        }
        return builder.toString();
    }

    /**
     * returns the relative path from the root to the {@link IIpsPackageFragment}
     * 
     * @param ipsPackageFragment
     * @return
     */
    protected String getPackageFragmentPathFromRoot(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage()) {
            return ""; //$NON-NLS-1$
        }
        return ipsPackageFragment.getRelativePath().toOSString() + File.separator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getLinkText(boolean)
     */
    public String getLinkText(boolean withImage) {
        return ipsElement.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getIpsElement()
     */
    public T getIpsElement() {
        return ipsElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getPathFromRoot(org.faktorips
     * .devtools.htmlexport.helper.path.LinkedFileType)
     */
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPackageFragmentPathFromRoot(getIpsPackageFragment()));
        builder.append(linkedFileType.getPrefix());
        builder.append(getFileName());
        builder.append(linkedFileType.getSuffix());

        return builder.toString();
    }

    /**
     * @return fileName of the {@link IIpsElement}
     */
    protected String getFileName() {
        return getIpsElement().getName();
    }

    /**
     * @return {@link IIpsPackageFragment} of the {@link IIpsElement}
     */
    protected abstract IIpsPackageFragment getIpsPackageFragment();
}
