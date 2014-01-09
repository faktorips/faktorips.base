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

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

public class IpsObjectTypeHtmlPath implements IHtmlPath {
    private final IpsObjectType ipsObjectType;

    public IpsObjectTypeHtmlPath(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    public String getPathToRoot() {
        return EMPTY_PATH;
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return ipsObjectType.getFileExtension() + "_index"; //$NON-NLS-1$
    }
}
