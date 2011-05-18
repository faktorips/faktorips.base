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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

public interface IPropertyValueContainer extends IIpsObjectPartContainer {

    /**
     * Returns the property value for the given property or <code>null</code> if no value is defined
     * for this generation. In this case {@link #computeDeltaToModel(IIpsProject)} returns a delta
     * containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if property is <code>null</code>.
     * <p>
     * Note that this method searches only the property values that have the same property type as
     * the indicated property. If you want to search only by name, use
     * {@link #getPropertyValue(String)}.
     */
    public abstract IPropertyValue getPropertyValue(IProductCmptProperty property);

    /**
     * Returns the property values for the given property name or <code>null</code> if no value is
     * defined for this generation. In this case {@link #computeDeltaToModel(IIpsProject)} returns a
     * delta containing an entry for the missing property value.
     * <p>
     * Returns <code>null</code> if propertyName is <code>null</code>.
     */
    public abstract IPropertyValue getPropertyValue(String propertyName);

    /**
     * Returns all property values for the given type. Returns an empty array if type is
     * <code>null</code>.
     */
    public abstract List<IPropertyValue> getPropertyValues(ProductCmptPropertyType type);

    /**
     * Creates a new property value for the given property.
     * 
     * @throws NullPointerException if property is <code>null</code>.
     */
    public abstract IPropertyValue newPropertyValue(IProductCmptProperty property);

    /**
     * Returns the delta between this product component and it's product component type.
     * 
     * @param ipsProject The ips project which search path is used to search the type.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public IGenerationToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the qualified name of the product component type this product component is based on.
     */
    public abstract String getProductCmptType();

}