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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Used to arrange {@link IProductCmptProperty} into groups that make more sense to the insurance
 * department than a technical arrangement. For example, a category called
 * <em>premium computation</em> including a <em>premium table</em> might be created. Prior to this
 * feature, the <em>premium table</em> would be automatically assigned to the
 * <em>tables and formulas</em> section.
 * <p>
 * A category can be marked to be the <em>default category</em> for each
 * {@link IProductCmptProperty}. New parts of that property type are then automatically assigned to
 * the corresponding default category. Of course, the parts can still be moved to other categories
 * by the user.
 * <p>
 * Furthermore, a category can be marked to be inherited from the supertype. In this case, a
 * category with the same name must be found in the supertype.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptCategory extends IIpsObjectPart {

    public final static String XML_TAG_NAME = "Category"; //$NON-NLS-1$

    public final static String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_METHODS = "defaultForMethods"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_VALIDATION_RULES = "defaultForValidationRules"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES = "defaultForTableStructureUsages"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = "defaultForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = "defaultForProductCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_SIDE = "side"; //$NON-NLS-1$

    /**
     * Returns the {@link IProductCmptType} this category belongs to.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Sets the name of this category.
     * 
     * @param name The new name of this category
     */
    public void setName(String name);

    /**
     * Returns an unmodifiable view on the list of {@link IProductCmptProperty} currently assigned
     * to this category.
     * <p>
     * If this category is <em>inherited</em>, this method does <strong>not</strong> return
     * {@link IProductCmptProperty}s assigned by the supertype hierarchy. The method
     * {@link #findAllAssignedProductCmptProperties(IIpsProject)} can be used to retrieve all
     * assigned {@link IProductCmptProperty}s.
     */
    public List<IProductCmptProperty> getAssignedProductCmptProperties();

    /**
     * Returns whether the indicated {@link IProductCmptProperty} is assigned to this category.
     * <p>
     * This method does <strong>not</strong> consider {@link IProductCmptProperty}s assigned by the
     * supertype hierarchy. To achieve this, use
     * {@link #findIsAssignedProductCmptProperty(IProductCmptProperty, IIpsProject)}.
     * 
     * @param productCmptProperty The property to check for assignment
     */
    public boolean isAssignedProductCmptProperty(IProductCmptProperty productCmptProperty);

    /**
     * Returns an unmodifiable view on the list of {@link IProductCmptProperty} currently assigned
     * to this category.
     * <p>
     * In contrast to {@link #getAssignedProductCmptProperties()}, the list returned by this method
     * <strong>does</strong> include {@link IProductCmptProperty}s assigned by the supertype
     * hierarchy.
     * <p>
     * The properties are sorted in such a way that assignments of supertypes are positioned at the
     * top of the list.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy
     */
    public List<IProductCmptProperty> findAllAssignedProductCmptProperties(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether the indicated {@link IProductCmptProperty} is assigned to this category.
     * <p>
     * This method <strong>does</strong> consider the supertype hierarchy.
     * 
     * @param property The property to check for assignment
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy
     */
    public boolean findIsAssignedProductCmptProperty(IProductCmptProperty property, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Assigns the given {@link IProductCmptProperty} to this category.
     * <p>
     * Returns false if the {@link IProductCmptProperty} is already assigned to this category, true
     * otherwise.
     * 
     * @param productCmptProperty The {@link IProductCmptProperty} to assign to this category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link IProductCmptProperty} does not belong to
     *             the {@link IProductCmptType} this category belongs to
     */
    public boolean assignProductCmptProperty(IProductCmptProperty productCmptProperty);

    /**
     * Removes the indicated {@link IProductCmptProperty} from this category.
     * <p>
     * Returns false if the {@link IProductCmptProperty} is actually not assigned to this category,
     * true otherwise.
     * 
     * @param productCmptProperty The {@link IProductCmptProperty} to remove from this category
     * 
     * @throws NullPointerException If the parameter is null
     */
    public boolean removeProductCmptProperty(IProductCmptProperty productCmptProperty);

    /**
     * Returns whether this category is inherited from the supertype hierarchy.
     */
    public boolean isInherited();

    /**
     * Sets whether this category is inherited from the supertype hierarchy.
     * 
     * @param inherited Flag indicating whether this category is inherited from the supertype
     *            hierarchy
     */
    public void setInherited(boolean inherited);

    /**
     * Returns whether this category is the default category for {@link IMethod}s.
     */
    public boolean isDefaultForMethods();

    /**
     * Sets whether this category is the default category for {@link IMethod}s.
     * 
     * @param defaultForMethods Flag indicating whether this category shall be the default category
     *            for {@link IMethod}s
     */
    public void setDefaultForMethods(boolean defaultForMethods);

    /**
     * Returns whether this category is the default category for {@link IPolicyCmptTypeAttribute}s.
     */
    public boolean isDefaultForPolicyCmptTypeAttributes();

    /**
     * Sets whether this category is the default category for {@link IPolicyCmptTypeAttribute}s.
     * 
     * @param defaultForPolicyCmptTypeAttributes Flag indicating whether this category shall be the
     *            default category for {@link IPolicyCmptTypeAttribute}s
     */
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes);

    /**
     * Returns whether this category is the default category for {@link IProductCmptTypeAttribute}s.
     */
    public boolean isDefaultForProductCmptTypeAttributes();

    /**
     * Sets whether this category is the default category for {@link IProductCmptTypeAttribute}s.
     * 
     * @param defaultForProductCmptTypeAttributes Flag indicating whether this category shall be the
     *            default category for {@link IProductCmptTypeAttribute}s
     */
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes);

    /**
     * Returns whether this category is the default category for {@link ITableStructureUsage}s.
     */
    public boolean isDefaultForTableStructureUsages();

    /**
     * Sets whether this category is the default category for {@link ITableStructureUsage}s.
     * 
     * @param defaultForTableStructureUsages Flag indicating whether this category shall be the
     *            default category for {@link ITableStructureUsage}s
     */
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages);

    /**
     * Returns whether this category is the default category for {@link IValidationRule}s.
     */
    public boolean isDefaultForValidationRules();

    /**
     * Sets whether this category is the default category for {@link IValidationRule}s.
     * 
     * @param defaultForValidationRules Flag indicating whether this category shall be the default
     *            category for {@link IValidationRule}s
     */
    public void setDefaultForValidationRules(boolean defaultForValidationRules);

    /**
     * Sets the {@link Side} this category is positioned at.
     * 
     * @param side The {@link Side} to position this category at
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setSide(Side side);

    /**
     * Returns the {@link Side} at which this category is positioned.
     */
    public Side getSide();

    /**
     * Returns whether this category is positioned at the left side.
     */
    public boolean isAtLeftSide();

    /**
     * Returns whether this category is positioned at the right side.
     */
    public boolean isAtRightSide();

    /**
     * Defines the side at which this category is positioned.
     */
    public static enum Side {

        LEFT,
        RIGHT;

    }

}
