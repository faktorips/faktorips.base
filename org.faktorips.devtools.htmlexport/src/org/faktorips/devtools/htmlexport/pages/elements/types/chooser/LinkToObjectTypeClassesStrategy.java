/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types.chooser;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.helper.path.LinkedFileType;
import org.faktorips.devtools.htmlexport.helper.path.PathUtilFactory;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.ILinkStrategy;

public class LinkToObjectTypeClassesStrategy implements ILinkStrategy {

    private final IpsObjectType ipsObjectType;

    public LinkToObjectTypeClassesStrategy(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    public LinkPageElement createLink(PageElement... containedPageElements) {
        LinkPageElement linkPageElement = new LinkPageElement(PathUtilFactory.createPathUtil(ipsObjectType)
                .getPathFromRoot(LinkedFileType.OBJECT_TYPE_CLASSES_OVERVIEW),
                LinkedFileType.OBJECT_TYPE_CLASSES_OVERVIEW.getTarget(), containedPageElements);
        linkPageElement.setTitle(ipsObjectType.getDisplayNamePlural());
        return linkPageElement;

    }
}
