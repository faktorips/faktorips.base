/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.List;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public class IpsObjectTypeListPageElement extends IpsElementListPageElement {

    public IpsObjectTypeListPageElement(IpsObjectType ipsObjectType, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        super(context.getIpsProject(), srcFiles, context);
        setTitle(ipsObjectType.getDisplayName());
    }

}
