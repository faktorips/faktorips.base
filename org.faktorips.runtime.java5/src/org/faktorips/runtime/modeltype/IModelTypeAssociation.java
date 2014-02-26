/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import java.util.List;
import java.util.Locale;

import org.faktorips.runtime.IModelObject;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelTypeAssociation extends IModelElement {

    public static final String XML_TAG = "ModelTypeAssociation";

    public static final String XML_WRAPPER_TAG = "ModelTypeAssociations";

    public static final String PROPERTY_NAME_PLURAL = "namePlural";

    public static final String PROPERTY_TARGET = "target";

    public static final String PROPERTY_MIN_CARDINALITY = "minCardinality";

    public static final String PROPERTY_MAX_CARDINALITY = "maxCardinality";

    public static final String PROPERTY_ASSOCIATION_TYPE = "associationType";

    public static final String PROPERTY_PRODUCT_RELEVANT = "isProductRelevant";

    public static final String PROPERTY_DERIVED_UNION = "isDerivedUnion";

    public static final String PROPERTY_SUBSET_OF_A_DERIVED_UNION = "isSubsetOfADerivedUnion";

    public static final String PROPERTY_TARGET_ROLE_PLURAL_REQUIRED = "isTargetRolePluralRequired";

    public static final String PROPERTY_INVERSE_ASSOCIATION = "inverseAssociation";

    public static final String PROPERTY_MATCHING_ASSOCIATION_NAME = "matchingAssociationName";

    public static final String PROPERTY_MATCHING_ASSOCIATION_SOURCE = "matchingAssociationSource";

    /**
     * Returns the model type this association belongs to.
     */
    public IModelType getModelType();

    /**
     * Returns the target model type object of this association.
     * 
     * @throws ClassNotFoundException if the target class could not be loaded.
     */
    public IModelType getTarget() throws ClassNotFoundException;

    /**
     * Returns the minimum cardinality for this association. <code>0</code> if no minimum is set.
     */
    public int getMinCardinality();

    /**
     * Returns the maximum cardinality for this association. <code>Integer.MAX_VALUE</code> if no
     * maximum is set.
     */
    public int getMaxCardinality();

    /**
     * Returns the plural form of this model type's name or <code>null</code> if no plural for for
     * the name is set.
     */
    public String getNamePlural();

    /**
     * Returns the singular or plural form of this model type's name as used in code generation
     * depending on cardinality.
     */
    public String getUsedName();

    /**
     * Returns the type of this association.
     */
    public AssociationType getAssociationType();

    /**
     * Enum defining the possible association types.
     */
    public enum AssociationType {
        Association,
        Composition,
        CompositionToMaster;
    }

    /**
     * Returns if this association is product relevant.
     */
    public boolean isProductRelevant();

    /**
     * Returns if this association is a derived union.
     */
    public boolean isDerivedUnion();

    /**
     * Returns if this association is a subset of a derived union.
     */
    public boolean isSubsetOfADerivedUnion();

    /**
     * Returns the name of the inverse association if it is defined.
     * 
     * @return The name of the inverse association or null for product component associations
     */
    public String getInverseAssociation();

    /**
     * Returns the plural label for the given locale.
     * <p>
     * Returns the association's plural name if no plural label for the given locale exists.
     */
    public String getLabelForPlural(Locale locale);

    /**
     * Returns a list of the target(s) of the given model object's association identified by this
     * model type association.
     * 
     * @param source a model object corresponding to the {@link IModelType} this association belongs
     *            to
     * @return a list of the target(s) of the given model object's association identified by this
     *         model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IModelObject> getTargetObjects(IModelObject source);

    /**
     * Returns the name of the matching product respectively policy component type association or
     * <code>null</code> if no matching association is defined for this associations.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the name of the matching product
     * association and vice versa.
     * 
     * @return The name of the matchingAssoctiation
     */
    public String getMatchingAssociationName();

    /**
     * Returns the qualified target name of the matching product respectively policy component type
     * association or <code>null</code> if no matching association is defined for component
     * associations.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the source of the matching product
     * association and vice versa. The source is the type which defines the matching association.
     * 
     * @return The qualified name of the matchingAssoctiation
     */
    public String getMatchingAssociationSource();
}
