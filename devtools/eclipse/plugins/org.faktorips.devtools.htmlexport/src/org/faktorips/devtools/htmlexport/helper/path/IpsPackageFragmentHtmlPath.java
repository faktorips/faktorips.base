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

import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IHtmlPath} for an {@link IIpsPackageFragment}
 * 
 * @author dicker
 * 
 */
public class IpsPackageFragmentHtmlPath extends AbstractIpsElementHtmlPath<IIpsPackageFragment> {
    private static final String PACKAGE_INDEX_FILE_NAME = "package_index"; //$NON-NLS-1$

    public IpsPackageFragmentHtmlPath(IIpsPackageFragment ipsElement) {
        super(ipsElement);
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        if (getIpsElement().isDefaultPackage()) {
            return PACKAGE_INDEX_FILE_NAME;
        }
        return super.getPathFromRoot(linkedFileType);
    }

    @Override
    public String getPathToRoot() {
        if (getIpsElement().isDefaultPackage()) {
            return EMPTY_PATH;
        }
        return getPackageFragmentPathToRoot(getIpsElement());
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement();
    }
}
