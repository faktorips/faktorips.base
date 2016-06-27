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

import java.util.Locale;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelTypeAssociation extends IModelElement {

    /**
     * Returns the model type this association belongs to.
     */
    public IModelType getModelType();

    /**
     * Returns the target model type object of this association.
     * 
     */
    public IModelType getTarget();

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
     * Returns the plural form of this model type's name or the empty String if no plural for the
     * name is set.
     * 
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
     * Returns if this association is a subset of a derived union.
     */
    public boolean isSubsetOfADerivedUnion();

    /**
     * Returns the name of the inverse association if it is defined.
     * 
     * @return The name of the inverse association or null if there is no inverse association or it
     *         is a product component associations
     */
    public String getInverseAssociation();

    /**
     * Returns the plural label for this model element in the specified locale. If there is no
     * plural label in the specified locale, it tries to find the plural label in the default
     * locale. If there is also no plural label in the default locale the element's plural name is
     * returned.
     * 
     * @return the label for the given locale or the element's name if no label exists for the given
     *         locale nor in the default locale
     */
    public String getLabelForPlural(Locale locale);

    /**
     * Returns if this association is product relevant.
     * 
     * @deprecated Since 3.18, use isMatchingAssociationPresent
     */
    @Deprecated
    public boolean isProductRelevant();

    /**
     * Returns <code>true</code> if the association has a matching association. For policy
     * associations that means it is configured by the product component. For product component
     * associations that means it configures a policy association.
     * 
     * @see #getMatchingAssociationName()
     * 
     * @return <code>true</code> if this association has a matching association.
     */
    public boolean isMatchingAssociationPresent();

    /**
     * Returns the name of the matching product respectively policy component type association or
     * <code>null</code> if no matching association is defined for this association.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the name of the matching product
     * association and vice versa.
     * 
     * @return The name of the matching association
     */
    public String getMatchingAssociationName();

    /**
     * Returns the qualified name of source type of the matching association or <code>null</code> if
     * no matching association is defined.
     * <p>
     * Example: Taking two policy component types called 'Policy' and 'Coverage' with a composition
     * association between them. Policy is constrained by the product component type 'Product' and
     * coverage by 'CoverageType'. There is also an association from 'Product' to 'CoverageType'.
     * The product association configures the policy association. If this is a model type
     * association for the policy association this method returns the qualified name of the source
     * of the matching product association and vice versa. The source is the type which defines the
     * matching association.
     * 
     * @return The qualified name of the matching association source
     */
    public String getMatchingAssociationSource();

    /**
     * Returns the {@link IModelType} identified by {@link #getMatchingAssociationSource()}
     * 
     * @see #getMatchingAssociationSource()
     * 
     * @return The model type object of the matching association source
     */
    public IModelType getMatchingAssociationSourceType();

    /**
     * Returns if this association is a derived union.
     */
    public boolean isDerivedUnion();

    /**
     * Enum defining the possible association types.
     */
    public enum AssociationType {
        Association,
        Composition,
        CompositionToMaster;
    }
}
