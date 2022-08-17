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

import org.faktorips.devtools.model.ipsobject.IpsObjectType;

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
