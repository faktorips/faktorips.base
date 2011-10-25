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
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Used to arrange {@link IProductCmptProperty} into groups that make more sense to the insurance
 * department than a technical arrangement. For example, a category called
 * <em>premium computation</em> including a <em>premium table</em> might be created. Prior to this
 * feature, the <em>premium table</em> would be automatically assigned to the
 * <em>tables and formulas</em> section.
 * <p>
 * A category can be marked to be the <em>default category</em> for each type of
 * {@link IProductCmptProperty}. New parts of that property type will then automatically be
 * referenced by the corresponding default category. Of course, the parts can still be moved to
 * other categories by the user.
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

    public final static String PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS = "defaultForMethods"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_VALIDATION_RULES = "defaultForValidationRules"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES = "defaultForTableStructureUsages"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = "defaultForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = "defaultForProductCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_POSITION = "position"; //$NON-NLS-1$

    public final static String MSGCODE_PREFIX = "ProductCmptCategory-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no name has been specified for this category.
     */
    public final static String MSGCODE_NAME_IS_EMPTY = MSGCODE_PREFIX + "NameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name given to this category is already used for
     * another category in the scope of the product component type's hierarchy.
     */
    public final static String MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY = "NameAlreadyUsedInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other category within the product
     * component type's supertype hierarchy is marked to be the default category for formula
     * signature definitions.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS = MSGCODE_PREFIX
            + "DuplicateDefaultsForFormulaSignatureDefinitions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other category within the product
     * component type's supertype hierarchy is marked to be the default category for product
     * relevant validation rules.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES = MSGCODE_PREFIX
            + "DuplicateDefaultsForValidationRules"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other category within the product
     * component type's supertype hierarchy is marked to be the default category for table structure
     * usages.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES = MSGCODE_PREFIX
            + "DuplicateDefaultsForTableStructureUsages"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other category within the product
     * component type's supertype hierarchy is marked to be the default category for product
     * relevant policy component attributes.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other category within the product
     * component type's supertype hierarchy is marked to be the default category for product
     * component type attributes.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForProductCmptTypeAttributes"; //$NON-NLS-1$

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
     * Returns the list of {@link IProductCmptProperty}s assigned to this category (defensive copy).
     * <p>
     * This method <strong>does</strong> consider references of the supertype hierarchy.
     * <p>
     * The properties in the list are sorted accordingly.
     * 
     * @param contextType The {@link IProductCmptType} to start the search from; properties assigned
     *            by {@link IProductCmptType}s below the context type are not returned
     * @param ipsProject The {@link IIpsProject} to use for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns a list containing the property values corresponding to the product component
     * properties assigned to this category.
     * <p>
     * This method <strong>does</strong> consider property references of the supertype hierarchy.
     * 
     * @param contextType The product component type to start the search from; properties assigned
     *            by product component types below the context type are not returned
     * @param contextGeneration The product component generation to retrieve property values from
     * @param ipsProject The IPS project to use for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public List<IPropertyValue> findPropertyValues(IProductCmptType contextType,
            IProductCmptGeneration contextGeneration,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this category is marked as default category for
     * {@link IProductCmptTypeMethod}s defining formula signatures.
     */
    public boolean isDefaultForFormulaSignatureDefinitions();

    /**
     * Sets whether this category is marked as default category for {@link IProductCmptTypeMethod}s.
     * 
     * @param defaultForFormulaSignatureDefinitions Flag indicating whether this category shall be
     *            the default category for {@link IProductCmptTypeMethod}s defining formula
     *            signatures
     */
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions);

    /**
     * Returns whether this category is marked as default category for product relevant
     * {@link IPolicyCmptTypeAttribute}s.
     */
    public boolean isDefaultForPolicyCmptTypeAttributes();

    /**
     * Sets whether this category is the default category for product relevant
     * {@link IPolicyCmptTypeAttribute}s.
     * 
     * @param defaultForPolicyCmptTypeAttributes Flag indicating whether this category shall be the
     *            default category for product relevant {@link IPolicyCmptTypeAttribute}s
     */
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes);

    /**
     * Returns whether this category is marked as default category for
     * {@link IProductCmptTypeAttribute}s.
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
     * Returns whether this category is marked as default category for {@link ITableStructureUsage}
     * s.
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
     * Returns whether this category is marked as default category for product relevant
     * {@link IValidationRule}s.
     */
    public boolean isDefaultForValidationRules();

    /**
     * Sets whether this category is the default category for product relevant
     * {@link IValidationRule}s.
     * 
     * @param defaultForValidationRules Flag indicating whether this category shall be the default
     *            category for product relevant {@link IValidationRule}s
     */
    public void setDefaultForValidationRules(boolean defaultForValidationRules);

    /**
     * Sets the {@link Position} of this category.
     * 
     * @param side The {@link Position} to set this category to
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setPosition(Position side);

    /**
     * Returns the {@link Position} of this category.
     */
    public Position getPosition();

    /**
     * Returns whether this category is positioned at the left.
     */
    public boolean isAtLeftPosition();

    /**
     * Returns whether this category is positioned at the right.
     */
    public boolean isAtRightPosition();

    /**
     * Moves the given {@link IProductCmptProperty}s up or down by one position.
     * <p>
     * If one of the properties is the first object, no property is moved up. If one of the
     * properties is the last object, no property is moved down. Returns true if a move was actually
     * performed.
     * 
     * @param properties The {@link IProductCmptCategory} to be moved
     * @param up Flag indicating whether to move up or down
     * 
     * @throws NullPointerException If the properties array is null
     * @throws IllegalArgumentException If not all provided properties belong to the same product
     *             component type or it's configured policy component type
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy or while
     *             searching for policy component types
     */
    public boolean moveProductCmptProperties(List<IProductCmptProperty> properties, boolean up) throws CoreException;

    /**
     * Defines the position of this category.
     */
    public static enum Position {

        LEFT,
        RIGHT;

    }

}
