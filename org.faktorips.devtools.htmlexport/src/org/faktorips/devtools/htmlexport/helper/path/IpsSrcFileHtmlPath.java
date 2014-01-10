/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IHtmlPath} for an {@link IIpsObject}
 * 
 * @author dicker
 * 
 */
public class IpsSrcFileHtmlPath extends AbstractIpsElementHtmlPath<IIpsSrcFile> {

    public IpsSrcFileHtmlPath(IIpsSrcFile ipsElement) {
        super(ipsElement);
    }

    @Override
    protected String getFileName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getIpsElement().getName());
        return builder.toString();
    }

    @Override
    public String getPathToRoot() {
        return getPackageFragmentPathToRoot(getIpsPackageFragment());
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement().getIpsPackageFragment();
    }
}