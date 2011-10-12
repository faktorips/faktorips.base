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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * An {@link IProductCmptPropertyReference} to {@link IProductCmptProperty}s that are stored in the
 * {@link IPolicyCmptType} configured by the {@link IProductCmptType} the reference belongs to.
 * <p>
 * The reference name and property type must be set manually.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptPropertyExternalReference extends IProductCmptPropertyReference {

    public final static String XML_TAG_NAME = "ProductCmptPropertyExternalReference"; //$NON-NLS-1$

    /**
     * Sets the name of the referenced property.
     * 
     * @param name The name identifying the referenced property
     */
    public void setName(String name);

    /**
     * Sets the {@link ProductCmptPropertyType} of the referenced property.
     * 
     * @param propertyType The {@link ProductCmptPropertyType} of the referenced property
     * 
     * @throws IllegalArgumentException If the given property type is any of:
     *             <ul>
     *             <li>{@link ProductCmptPropertyType#FORMULA_SIGNATURE_DEFINITION}
     *             <li>{@link ProductCmptPropertyType#TABLE_STRUCTURE_USAGE}
     *             <li>{@link ProductCmptPropertyType#PRODUCT_CMPT_TYPE_ATTRIBUTE}
     *             </ul>
     */
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType);

}
