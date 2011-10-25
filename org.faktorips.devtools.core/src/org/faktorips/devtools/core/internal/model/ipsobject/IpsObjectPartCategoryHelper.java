/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.ipsobject.ICategorisableElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Element;

/**
 * Helper class that can be used by {@link IIpsObjectPart}s that want to implement the
 * {@link ICategorisableElement} interface.
 * 
 * @author Alexander Weickmann
 */
public final class IpsObjectPartCategoryHelper {

    private final IpsObjectPart ipsObjectPart;

    private String category = ""; //$NON-NLS-1$

    public IpsObjectPartCategoryHelper(IIpsObjectPart ipsObjectPart) {
        this.ipsObjectPart = (IpsObjectPart)ipsObjectPart;
    }

    public void setCategory(String category) {
        String oldValue = this.category;
        this.category = category;
        ipsObjectPart.valueChanged(oldValue, category, ICategorisableElement.PROPERTY_CATEGORY);
    }

    public String getCategory() {
        return category;
    }

    public void initPropertiesFromXml(Element element) {
        // TODO AW 25-10-2011: Handle null
        category = element.getAttribute(ICategorisableElement.PROPERTY_CATEGORY);
    }

    public void propertiesToXml(Element element) {
        element.setAttribute(ICategorisableElement.PROPERTY_CATEGORY, category);
    }

}
