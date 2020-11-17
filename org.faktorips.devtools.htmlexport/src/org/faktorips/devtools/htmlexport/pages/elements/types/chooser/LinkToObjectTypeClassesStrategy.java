/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types.chooser;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.path.HtmlPathFactory;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ILinkStrategy;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

public class LinkToObjectTypeClassesStrategy implements ILinkStrategy {

    private final IpsObjectType ipsObjectType;

    public LinkToObjectTypeClassesStrategy(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    public LinkPageElement createLink(DocumentationContext context, IPageElement... containedPageElements) {
        LinkPageElement linkPageElement = new LinkPageElement(HtmlPathFactory.createPathUtil(ipsObjectType)
                .getPathFromRoot(LinkedFileType.OBJECT_TYPE_CLASSES_OVERVIEW),
                LinkedFileType.OBJECT_TYPE_CLASSES_OVERVIEW.getTarget(), context, containedPageElements);
        linkPageElement.setTitle(ipsObjectType.getDisplayNamePlural());
        return linkPageElement;

    }
}
