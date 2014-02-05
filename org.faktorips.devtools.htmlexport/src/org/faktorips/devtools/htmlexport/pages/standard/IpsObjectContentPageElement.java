/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * A complete page representing an {@link IEnumContent}
 * 
 * @author dicker
 * 
 */
public class IpsObjectContentPageElement extends AbstractIpsObjectContentPageElement<IIpsObject> {

    protected IpsObjectContentPageElement(IIpsObject documentedIpsObject, DocumentationContext context) {
        super(documentedIpsObject, context);
    }

}
