/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.modeltype;

/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelTypeAssociation extends IModelElement {

    public static final String XML_TAG = "ModelTypeAssociation";

    public static final String XML_WRAPPER_TAG = "ModelTypeAssociations";

    public static final String PROPERTY_TARGET = "target";

    public static final String PROPERTY_MIN_CARDINALITY = "minCardinality";

    public static final String PROPERTY_MAX_CARDINALITY = "maxCardinality";

    public static final String PROPERTY_ASSOCIATION_TYPE = "associationType";

    public static final String PROPERTY_PRODUCT_RELEVANT = "isProductRelevant";

    public static final String PROPERTY_DERIVED_UNION = "isDerivedUnion";

    public static final String PROPERTY_SUBSET_OF_A_DERIVED_UNION = "isSubsetOfADerivedUnion";

    public static final String PROPERTY_TARGET_ROLE_PLURAL_REQUIRED = "isTargetRolePluralRequired";

    public static final String PROPERTY_INVERSE_ASSOCIATION = "inverseAssociation";

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

}
