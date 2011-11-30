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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
 * subsequently moved to another {@link IProductCmptCategory}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IProductCmptProperty
 * @see ProductCmptPropertyType
 */
public interface IProductCmptCategory extends IIpsObjectPart, ILabeledElement, IDescribedElement {

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
     * Validation message code to indicate that the name given to this {@link IProductCmptCategory}
     * is already used for another {@link IProductCmptCategory} in the scope of the product
     * component type's supertype hierarchy.
     */
    public final static String MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY = MSGCODE_PREFIX
            + "NameAlreadyUsedInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other {@link IProductCmptCategory}
     * within the product component type's supertype hierarchy is marked to be the default
     * {@link IProductCmptCategory} for formula signature definitions.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS = MSGCODE_PREFIX
            + "DuplicateDefaultsForFormulaSignatureDefinitions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other {@link IProductCmptCategory}
     * within the product component type's supertype hierarchy is marked to be the default
     * {@link IProductCmptCategory} for configurable validation rules.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES = MSGCODE_PREFIX
            + "DuplicateDefaultsForValidationRules"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other {@link IProductCmptCategory}
     * within the product component type's supertype hierarchy is marked to be the default
     * {@link IProductCmptCategory} for table structure usages.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES = MSGCODE_PREFIX
            + "DuplicateDefaultsForTableStructureUsages"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other {@link IProductCmptCategory}
     * within the product component type's supertype hierarchy is marked to be the default
     * {@link IProductCmptCategory} for product relevant policy component type attributes.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one other {@link IProductCmptCategory}
     * within the product component type's supertype hierarchy is marked to be the default
     * {@link IProductCmptCategory} for product component type attributes.
     */
    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForProductCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Returns the {@link IProductCmptType} this category belongs to.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Sets the name of this {@link IProductCmptCategory}.
     * 
     * @see #getName()
     */
    public void setName(String name);

    /**
     * Returns the list of product component properties assigned to this
     * {@link IProductCmptCategory}.
     * <p>
     * This method does consider assignments made in the supertype hierarchy if so desired.
     * <p>
     * The product component properties are sorted according to the order prescribed in the
     * assigning product component types.
     * 
     * @param contextType the {@link IProductCmptType} to start the search from. Product component
     *            properties assigned by product component types below the context type are not
     *            returned
     * @param searchSupertypeHierarchy flag indicating whether assignments made in the supertype
     *            hierarchy shall be searched as well
     * 
     * @throws CoreException if an error occurs during the search
     */
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing the property values corresponding to the product component
     * properties assigned to this {@link IProductCmptCategory}.
     * <p>
     * This method <strong>does</strong> consider assignments made in the supertype hierarchy.
     * <p>
     * The values are sorted according to the order of the properties as returned by
     * {@link #findProductCmptProperties(IProductCmptType, boolean, IIpsProject)}.
     * 
     * @param contextType the {@link IProductCmptType} to start the search from. Properties assigned
     *            by product component types below the context type are not returned
     * @param contextGeneration the {@link IProductCmptGeneration} to retrieve property values from
     * 
     * @throws CoreException if an error occurs during the search
     */
    public List<IPropertyValue> findPropertyValues(IProductCmptType contextType,
            IProductCmptGeneration contextGeneration,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this {@link IProductCmptCategory} contains the indicated
     * {@link IProductCmptProperty}.
     * <p>
     * If this {@link IProductCmptCategory} is marked as default for the corresponding
     * {@link ProductCmptPropertyType}, true will be returned if the {@link IProductCmptProperty}
     * has no {@link IProductCmptCategory} or belongs to an {@link IProductCmptCategory} that cannot
     * be found.
     * <p>
     * The supertype hierarchy of this category's {@link IProductCmptType} is considered while
     * searching for categories.
     * 
     * @param property the {@link IProductCmptProperty} to check for containment
     * @param contextType the {@link IProductCmptType} to start the search from. Properties assigned
     *            by product component types below the context type are not considered
     * 
     * @throws CoreException if an error occurs during the search
     */
    public boolean findIsContainingProperty(IProductCmptProperty property,
            IProductCmptType contextType,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this {@link IProductCmptCategory} is the corresponding default
     * {@link IProductCmptCategory} for the indicated {@link IProductCmptProperty}.
     * <p>
     * If this {@link IProductCmptCategory} is the corresponding default
     * {@link IProductCmptCategory}, new properties of the same {@link ProductCmptPropertyType} will
     * be automatically assigned to this {@link IProductCmptCategory}.
     */
    public boolean isDefaultFor(IProductCmptProperty property);

    /**
     * Returns whether this {@link IProductCmptCategory} is the corresponding default
     * {@link IProductCmptCategory} for the indicated {@link ProductCmptPropertyType}.
     * <p>
     * If this {@link IProductCmptCategory} is the corresponding default
     * {@link IProductCmptCategory}, new properties of the indicated {@link ProductCmptPropertyType}
     * will be automatically assigned to this {@link IProductCmptCategory}.
     */
    public boolean isDefaultFor(ProductCmptPropertyType propertyType);

    /**
     * Returns whether this {@link IProductCmptCategory} is marked as default
     * {@link IProductCmptCategory} for formula signature definitions.
     * <p>
     * If this is the case, new properties of the type
     * {@link ProductCmptPropertyType#FORMULA_SIGNATURE_DEFINITION} will be automatically assigned
     * to this {@link IProductCmptCategory}.
     */
    public boolean isDefaultForFormulaSignatureDefinitions();

    /**
     * Sets whether this {@link IProductCmptCategory} is marked as default category for formula
     * signature definitions.
     * 
     * @see #isDefaultForFormulaSignatureDefinitions()
     */
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions);

    /**
     * Returns whether this {@link IProductCmptCategory} is marked as default
     * {@link IProductCmptCategory} for product relevant policy component type attributes.
     * <p>
     * If this is the case, new properties of the type
     * {@link ProductCmptPropertyType#POLICY_CMPT_TYPE_ATTRIBUTE} will be automatically assigned to
     * this {@link IProductCmptCategory}.
     */
    public boolean isDefaultForPolicyCmptTypeAttributes();

    /**
     * Sets whether this {@link IProductCmptCategory} is the default {@link IProductCmptCategory}
     * for product relevant policy component type attributes.
     * 
     * @see #isDefaultForPolicyCmptTypeAttributes()
     */
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes);

    /**
     * Returns whether this {@link IProductCmptCategory} is marked as default
     * {@link IProductCmptCategory} for product component type attributes.
     * <p>
     * If this is the case, new properties of the type
     * {@link ProductCmptPropertyType#PRODUCT_CMPT_TYPE_ATTRIBUTE} will be automatically assigned to
     * this {@link IProductCmptCategory}.
     */
    public boolean isDefaultForProductCmptTypeAttributes();

    /**
     * Sets whether this {@link IProductCmptCategory} is the default {@link IProductCmptCategory}
     * for product component type attributes.
     * 
     * @see #isDefaultForProductCmptTypeAttributes()
     */
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes);

    /**
     * Returns whether this {@link IProductCmptCategory} is marked as default
     * {@link IProductCmptCategory} for table structure usages.
     * <p>
     * If this is the case, new properties of the type
     * {@link ProductCmptPropertyType#TABLE_STRUCTURE_USAGE} will be automatically assigned to this
     * {@link IProductCmptCategory}.
     */
    public boolean isDefaultForTableStructureUsages();

    /**
     * Sets whether this {@link IProductCmptCategory} is the default {@link IProductCmptCategory}
     * for table structure usages.
     * 
     * @see #isDefaultForTableStructureUsages()
     */
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages);

    /**
     * Returns whether this {@link IProductCmptCategory} is marked as default
     * {@link IProductCmptCategory} for configurable validation rules.
     * <p>
     * If this is the case, new properties of the type
     * {@link ProductCmptPropertyType#VALIDATION_RULE} will be automatically assigned to this
     * {@link IProductCmptCategory}.
     */
    public boolean isDefaultForValidationRules();

    /**
     * Sets whether this {@link IProductCmptCategory} is the default {@link IProductCmptCategory}
     * for configurable validation rules.
     * 
     * @see #isDefaultForValidationRules()
     */
    public void setDefaultForValidationRules(boolean defaultForValidationRules);

    /**
     * Returns the {@link Position} of this {@link IProductCmptCategory}.
     * <p>
     * The {@link Position} gives an indication of where this {@link IProductCmptCategory} is placed
     * by the user interface.
     * 
     * @see Position
     */
    public Position getPosition();

    /**
     * Sets the {@link Position} of this {@link IProductCmptCategory}.
     * 
     * @see #getPosition()
     * @see Position
     */
    public void setPosition(Position side);

    /**
     * Returns whether this {@link IProductCmptCategory} is positioned at {@link Position#LEFT}.
     */
    public boolean isAtLeftPosition();

    /**
     * Returns whether this {@link IProductCmptCategory} is positioned at {@link Position#RIGHT}.
     */
    public boolean isAtRightPosition();

    /**
     * Moves the product component properties identified by the provided indexes up or down by one
     * position.
     * <p>
     * If one of the indexes is 0 (the first object), no property is moved up. If one of the indexes
     * is the number objects - 1 (the last object), no property is moved down. Returns true if a
     * move was actually performed.
     * 
     * @param indexes the indexes identifying the product component properties to be moved
     * @param up flag indicating whether to move up or down
     * @param contextType the {@link IProductCmptType} the move is performed for. The provided
     *            indexes reference properties from the context type and it's supertypes, where
     *            properties from supertypes occupy the lesser indexes. Properties assigned to this
     *            {@link IProductCmptCategory} by product component types below the context type are
     *            ignored
     * 
     * @return the new indexes of the moved properties
     * 
     * @throws IndexOutOfBoundsException if not all provided indexes identify properties assigned to
     *             this {@link IProductCmptCategory} by the context type
     * 
     * @throws CoreException if an error occurs while searching the supertype hierarchy or while
     *             searching for policy component types
     */
    public int[] moveProductCmptProperties(int[] indexes, boolean up, IProductCmptType contextType)
            throws CoreException;

    /**
     * Inserts the provided {@link IProductCmptProperty} into this {@link IProductCmptCategory} just
     * above or just below the indicated target {@link IProductCmptProperty}.
     * <p>
     * Returns true if the operation was successful. If the index of any of the two provided
     * properties cannot be computed, the operation will fail. In this case, false is returned.
     * <p>
     * Note that only a single <em>whole content changed</em> event will be fired by this operation.
     * 
     * @param property the {@link IProductCmptProperty} to insert into this
     *            {@link IProductCmptCategory}
     * @param targetProperty the {@link IProductCmptProperty} to be inserted will be positioned just
     *            above or just below the target {@link IProductCmptProperty}. This parameter may be
     *            null - in this case the {@link IProductCmptProperty} is inserted at the end
     * @param above flag indicating whether to insert the {@link IProductCmptProperty} above or
     *            below the target {@link IProductCmptProperty}
     * 
     * @return true if the operation was successful, false if not
     * 
     * @throws CoreException if an error occurs while searching for the indices of the currently
     *             assigned properties or while moving properties
     */
    public boolean insertProductCmptProperty(IProductCmptProperty property,
            IProductCmptProperty targetProperty,
            boolean above) throws CoreException;

    /**
     * Defines the user interface position of an {@link IProductCmptCategory}.
     */
    public static enum Position {

        LEFT("left"), //$NON-NLS-1$

        RIGHT("right"); //$NON-NLS-1$

        /**
         * Returns the {@link Position} corresponding to the provided id.
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
         * Returns the id of this {@link Position}.
         */
        public String getId() {
            return id;
        }

    }

}
