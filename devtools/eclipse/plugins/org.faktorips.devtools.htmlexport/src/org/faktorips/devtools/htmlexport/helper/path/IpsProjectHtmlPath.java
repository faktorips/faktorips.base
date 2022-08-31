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
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * {@link IHtmlPath} for an {@link IIpsProject}
 * 
 * @author dicker
 * 
 */
public class IpsProjectHtmlPath extends AbstractIpsElementHtmlPath<IIpsProject> {

    private static final String INDEX_HTML = "indes.html"; //$NON-NLS-1$

    public IpsProjectHtmlPath(IIpsProject ipsElement) {
        super(ipsElement);
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return INDEX_HTML;
    }

    @Override
    public String getPathToRoot() {
        return EMPTY_PATH;
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        throw new UnsupportedOperationException("An IpsProject has no IpsPackageFragment"); //$NON-NLS-1$
    }
}
