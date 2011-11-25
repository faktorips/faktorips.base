/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * An abstraction of properties (defined by a type) that are configured by product components. Such
 * properties have exactly one value-instance ({@link IPropertyValue}).
 * <p>
 * As of yet, not all aspects that are configured by a product component are
 * {@link IProductCmptProperty}s. e.g. {@link ProductCmptTypeAssociation} as there are multiple
 * {@link IProductCmptLink} instances for a single association.
 * 
 * @author Jan Ortmann
 * @author Stefan Widmaier
 */
public interface IProductCmptProperty extends ITypePart {

    /**
     * Returns the type of the property. The different types of product definition properties are
     * defined by {@link ProductCmptPropertyType}. The type represents the different elements in the
     * model that implement this interface. Each type corresponds to one element.
     * <p>
     * See {@link ProductCmptPropertyType} for safe casts to a specific model element.
     * 
     * @see ProductCmptPropertyType
     */
    public ProductCmptPropertyType getProductCmptPropertyType();

    /**
     * Returns the name of the property. That name is unique in the corresponding
     * {@link IProductCmpt}.
     */
    public String getPropertyName();

    /**
     * Returns <code>true</code> if every {@link IProductCmptGeneration} may specify a different
     * value for this property, <code>false</code> if the value is the same for all generations.
     */
    public boolean isChangingOverTime();

    /**
     * Returns this property's data type.
     */
    public String getPropertyDatatype();

    /**
     * Returns whether this property's parent is a policy component type instead of a product
     * component type.
     */
    public boolean isPolicyCmptTypeProperty();

    /**
     * Returns whether this {@link IProductCmptProperty} corresponds to the indicated
     * {@link IPropertyValue}.
     * 
     * @param propertyValue the {@link IPropertyValue} to check for correspondence
     */
    public boolean isPropertyFor(IPropertyValue propertyValue);

    /**
     * Returns the name of the {@link IProductCmptCategory} this {@link IProductCmptProperty} is
     * assigned to.
     * <p>
     * <strong>Important:</strong> The returned string is always the name that is stored in the
     * {@link IProductCmptProperty} itself. However, this does not always reflect the property's
     * real {@link IProductCmptCategory}.
     * <ul>
     * <li>If the string is empty or the indicated {@link IProductCmptCategory} cannot be found, the
     * {@link IProductCmptProperty} is automatically assigned to the default
     * {@link IProductCmptCategory} corresponding to this property's {@link ProductCmptPropertyType}.
     * <li>If this {@link IProductCmptProperty} belongs to an {@link IPolicyCmptType} and the
     * category assignment is changed using
     * {@link IProductCmptType#changeCategoryAndDeferPolicyChange(IProductCmptProperty, String)},
     * this change is not immediately reflected by this getter as the method defers saving the
     * {@link IPolicyCmptType} until the {@link IProductCmptType} is saved.
     * </ul>
     */
    public String getCategory();

    /**
     * Sets the name of the {@link IProductCmptCategory} this {@link IProductCmptProperty} is
     * assigned to.
     * 
     * @see #getCategory()
     */
    public void setCategory(String category);

    /**
     * Returns whether this part is assigned to a specific {@link IProductCmptCategory}.
     * <p>
     * This operation is a shortcut for {@code !getCategory().isEmpty()}.
     */
    public boolean hasCategory();

    /**
     * Returns the {@link IProductCmptType} this {@link IProductCmptProperty} belongs to or null if
     * the referenced {@link IProductCmptType} could not be found.
     * 
     * @throws CoreException if an error occurs during the search
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

}
