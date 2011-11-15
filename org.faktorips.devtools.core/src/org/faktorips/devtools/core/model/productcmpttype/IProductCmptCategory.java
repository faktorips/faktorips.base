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
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

/**
 * Used to arrange product component properties into groups that make more sense to the insurance
 * department than a technical arrangement.
 * <p>
 * For example, an {@link IProductCmptCategory} called <em>premium computation</em> including a
 * <em>premium table</em> might be created. Prior to this feature, the <em>premium table</em> would
 * be automatically assigned to the <em>Tables and Formulas</em> section.
 * <p>
 * An {@link IProductCmptCategory} can be marked to be the <em>default</em> for specific property
 * types. New parts of these property types will then automatically be referenced by the
 * corresponding <em>default</em> {@link IProductCmptCategory}. Of course, the parts can still be
 * moved to another {@link IProductCmptCategory} subsequently.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptProperty
 * @see ProductCmptPropertyType
 */
public interface IProductCmptCategory extends IIpsObjectPart, ILabeledElement, IDescribedElement {

    public final static String XML_TAG_NAME = "Category"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS = "defaultForFormulaSignatureDefinitions"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_VALIDATION_RULES = "defaultForValidationRules"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES = "defaultForTableStructureUsages"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = "defaultForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = "defaultForProductCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_POSITION = "position"; //$NON-NLS-1$

    public final static String MSGCODE_PREFIX = "ProductCmptCategory-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no name has been specified for this
     * {@link IProductCmptCategory}.
     */
    public final static String MSGCODE_NAME_IS_EMPTY = MSGCODE_PREFIX + "NameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name given to this category is already used for
     * another category in the scope of the product component type's hierarchy.
     */
    public final static String MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY = MSGCODE_PREFIX
            + "NameAlreadyUsedInTypeHierarchy"; //$NON-NLS-1$

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
     * Returns the list of product component properties assigned to this
     * {@link IProductCmptCategory}.
     * <p>
     * This method <strong>does</strong> consider references of the supertype hierarchy.
     * <p>
     * The product component properties are sorted according to the order prescribed in the
     * assigning product component types.
     * 
     * @param contextType the {@link IProductCmptType} to start the search from; product component
     *            properties assigned by product component types below the context type are not
     *            returned
     * @param searchSupertypeHierarchy flag indicating whether the supertype hierarchy shall be
     *            searched as well
     * 
     * @throws CoreException if an error occurs during the search
     */
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException;

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
     * Returns whether this category contains the indicated property.
     * <p>
     * If this category is marked as default for the corresponding property type, true will be
     * returned if the property has no category or belongs to a category that cannot be found.
     * <p>
     * The supertype hierarchy of this category's {@link IProductCmptType} is considered while
     * searching for categories.
     * 
     * @param property The {@link IProductCmptProperty} to check for containment
     * @param ipsProject The {@link IIpsProject} whose {@link IIpsObjectPath} to use for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public boolean findIsContainingProperty(IProductCmptProperty property, IIpsProject ipsProject) throws CoreException;

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
     * Returns whether this category is marked as default category for configurable
     * {@link IValidationRule}s.
     */
    public boolean isDefaultForValidationRules();

    /**
     * Sets whether this category is the default category for configurable {@link IValidationRule}s.
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
     * Moves the {@link IProductCmptProperty}s identified by the provided indexes up or down by one
     * position.
     * <p>
     * If one of the indexes is 0 (the first object), no property is moved up. If one of the indexes
     * is the number objects - 1 (the last object), no property is moved down. Returns true if a
     * move was actually performed.
     * 
     * @param indexes The indexes identifying the {@link IProductCmptCategory}s to be moved
     * @param up Flag indicating whether to move up or down
     * @param contextType The {@link IProductCmptType} the moved properties belong to. The provided
     *            indexes reference the properties assigned to this category by this type, other
     *            properties are ignored
     * 
     * @return The new indexes of the moved properties
     * 
     * @throws IndexOutOfBoundsException If not all provided indexes identify properties assigned to
     *             this category with respect to the context type
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy or while
     *             searching for policy component types
     */
    public int[] moveProductCmptProperties(int[] indexes, boolean up, IProductCmptType contextType)
            throws CoreException;

    /**
     * Defines the position of this category.
     */
    public static enum Position {

        LEFT("left"), //$NON-NLS-1$
        RIGHT("right"); //$NON-NLS-1$

        /**
         * Returns the enum value corresponding to the provided id.
         * 
         * @param id the id of the enum value to be returned
         */
        public static final Position getValueById(String id) {
            if (Position.LEFT.getId().equals(id)) {
                return LEFT;
            } else if (Position.RIGHT.getId().equals(id)) {
                return RIGHT;
            }
            return null;
        }

        private final String id;

        private Position(String id) {
            this.id = id;
        }

        /**
         * Returns the ID of this position.
         */
        public final String getId() {
            return id;
        }

    }

}
