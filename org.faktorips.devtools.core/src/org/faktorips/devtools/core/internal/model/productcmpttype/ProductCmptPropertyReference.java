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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.w3c.dom.Element;

/**
 * @author Alexander Weickmann
 */
public abstract class ProductCmptPropertyReference extends AtomicIpsObjectPart implements IProductCmptPropertyReference {

    protected ProductCmptPropertyReference(IProductCmptCategory parentCategory, String id) {
        super(parentCategory, id);
    }

    /**
     * Returns the {@link IProductCmptType} this reference belongs to.
     */
    protected final IProductCmptType getProductCmptType() {
        return getProductCmptCategory().getProductCmptType();
    }

    /**
     * Returns the {@link IProductCmptCategory} this reference belongs to.
     */
    protected final IProductCmptCategory getProductCmptCategory() {
        return (IProductCmptCategory)getParent();
    }

    @Override
    protected final void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, getName());
        element.setAttribute(PROPERTY_PROPERTY_TYPE, getProductCmptPropertyType().getId());
    }

}
