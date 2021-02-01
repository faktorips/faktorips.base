/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import java.io.File;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Base implementation of the {@link IHtmlPath}
 * 
 * @author dicker
 * 
 */
public abstract class AbstractIpsElementHtmlPath<T extends IIpsElement> implements IHtmlPath {
    protected static final String UP_PATH = "../"; //$NON-NLS-1$

    protected T ipsElement;

    protected AbstractIpsElementHtmlPath(T ipsElement) {
        this.ipsElement = ipsElement;
    }

    /**
     * returns the relative path from the {@link IIpsPackageFragment} to the root
     * 
     */
    protected String getPackageFragmentPathToRoot(IIpsPackageFragment packageFragment) {
        if (packageFragment.isDefaultPackage()) {
            return EMPTY_PATH;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(UP_PATH);
        packageFragment = packageFragment.getParentIpsPackageFragment();
        while (packageFragment.getParentIpsPackageFragment() != null) {
            builder.append(UP_PATH);
            packageFragment = packageFragment.getParentIpsPackageFragment();
        }
        return builder.toString();
    }

    /**
     * returns the relative path from the root to the {@link IIpsPackageFragment}
     * 
     */
    protected String getPackageFragmentPathFromRoot(IIpsPackageFragment ipsPackageFragment) {
        if (ipsPackageFragment.isDefaultPackage()) {
            return EMPTY_PATH;
        }
        return ipsPackageFragment.getRelativePath().toOSString() + File.separator;
    }

    public T getIpsElement() {
        return ipsElement;
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPackageFragmentPathFromRoot(getIpsPackageFragment()));
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
