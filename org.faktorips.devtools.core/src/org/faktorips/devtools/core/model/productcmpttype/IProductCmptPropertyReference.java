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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * An object part used by {@link IProductCmptCategory}s to store references to
 * {@link IProductCmptProperty}s.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptPropertyReference extends IIpsObjectPart {

    public final static String XML_TAG_NAME = "ProductCmptPropertyReference"; //$NON-NLS-1$

    public final static String PROPERTY_PROPERTY_TYPE = "propertyType"; //$NON-NLS-1$

    /**
     * Returns the name of the referenced property.
     */
    @Override
    public String getName();

    /**
     * Returns the {@link ProductCmptPropertyType} of the referenced property.
     */
    public ProductCmptPropertyType getProductCmptPropertyType();

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
     */
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType);

    /**
     * Returns whether the given {@link IProductCmptProperty} is identified by this reference.
     * 
     * @param property The property to check whether this is a corresponding reference
     */
    public boolean isReferencingProperty(IProductCmptProperty property);

    /**
     * Returns the referenced {@link IProductCmptProperty} or null no such property is found.
     * 
     * @param ipsProject The IPS project whose IPS object path to use for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptProperty findProductCmptProperty(IIpsProject ipsProject) throws CoreException;

}
