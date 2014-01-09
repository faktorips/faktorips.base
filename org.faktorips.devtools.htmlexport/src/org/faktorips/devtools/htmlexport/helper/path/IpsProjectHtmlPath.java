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

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

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
        throw new NotImplementedException("An IpsProject has no IpsPackageFragment"); //$NON-NLS-1$
    }
}
