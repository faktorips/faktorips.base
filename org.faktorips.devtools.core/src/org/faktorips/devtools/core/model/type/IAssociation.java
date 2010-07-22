/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;

/**
 * An association is a directed relationship from one type (the source) to another (the target). The
 * association is stored as part of the source type.
 * 
 * @author Jan Ortmann
 */
public interface IAssociation extends IIpsObjectPart {

    public static final int CARDINALITY_ONE = 1;
    public static final int CARDINALITY_MANY = Integer.MAX_VALUE;

    // String constants for the relation class' properties according
    // to the Java beans standard.
    public final static String PROPERTY_ASSOCIATION_TYPE = "associationType"; //$NON-NLS-1$
    public final static String PROPERTY_AGGREGATION_KIND = "aggregationKind"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_SINGULAR = "targetRoleSingular"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET_ROLE_PLURAL = "targetRolePlural"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_DERIVED_UNION = "derivedUnion"; //$NON-NLS-1$
    public final static String PROPERTY_SUBSETTED_DERIVED_UNION = "subsettedDerivedUnion"; //$NON-NLS-1$
    public final static String PROPERTY_QUALIFIED = "qualified"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "Association-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target does not exist.
     */
    public final static String MSGCODE_TARGET_DOES_NOT_EXIST = MSGCODE_PREFIX + "TargetDoesNotExists"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target role singular must be set and it's not.
     */
    public final static String MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET = MSGCODE_PREFIX
            + "TargetRoleSingularMustBeSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target role plural must be set for to-many
     * associations.
     */
    public final static String MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET = MSGCODE_PREFIX + "TargetRolePluralMustBeSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association has the same role name singular and
     * plural.
     */
    public final static String MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR = MSGCODE_PREFIX
            + "TargetRoleSingularEqualsTargetRoleSingular"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the max cardinality must be at least 1 and it's not.
     */
    public final static String MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1 = MSGCODE_PREFIX
            + "MaxCardinalityMustBeAtLeast1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that for derived union associations the max cardinality
     * must be greater than 1, but it's not.
     */
    public final static String MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW = MSGCODE_PREFIX
            + "MaxCardinalityForContainerRelationTooLow"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum,
     * but it must be greater or equal than the minimum.
     */
    public final static String MSGCODE_MAX_IS_LESS_THAN_MIN = MSGCODE_PREFIX + "MaxIsLessThanMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the an association subsets a derived union, but the
     * derived union can't be found in the type's hierarchy.
     */
    public final static String MSGCODE_DERIVED_UNION_NOT_FOUND = MSGCODE_PREFIX + "DerivedUnionNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association specifies to subset another
     * association, but the other one is not marked as derived union.
     */
    public final static String MSGCODE_NOT_MARKED_AS_DERIVED_UNION = MSGCODE_PREFIX + "NotMarkedAsDerivedUnion"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an association specifies to subset a derived union,
     * but the target of the derived union does not exist and so it can't be checked, it the target
     * type of the subsetting association is a subtype (or the same type) as the target type of the
     * derived union.
     */
    public final static String MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "ContainerRelationTargetDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that a derived union cannot be its own subset.
     */
    public final static String MSGCODE_DERIVED_UNION_SUBSET_NOT_SAME_AS_DERIVED_UNION = MSGCODE_PREFIX
            + "DerivedUnionSubsetNotSameAsDerivedUnion"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that a derived union cannot be its own subset.
     */
    public final static String MSGCODE_SUBSET_OF_DERIVED_UNION_SAME_MAX_CARDINALITY = MSGCODE_PREFIX
            + "SubsetOfDerivedUnionSameMaxCardinality"; //$NON-NLS-1$

    /**
     * Given an association that is subsetting a derived union, this validation message code
     * indicates that the association's target type is not a subtype (or the same type) of the
     * derived union's target type.
     */
    public final static String MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE = IPolicyCmptTypeAssociation.MSGCODE_PREFIX
            + "TargetTypeNotASubtype"; //$NON-NLS-1$

    public final static AssociationType DEFAULT_RELATION_TYPE = AssociationType.ASSOCIATION;

    /**
     * Returns the type this association belongs to. Never returns <code>null</code>.
     */
    public IType getType();

    /**
     * Returns the association's type.
     */
    public AssociationType getAssociationType();

    /**
     * Sets the association's type.
     * 
     * @throws NullPointerException If <tt>newType</tt> is <code>null</code>.
     */
    public void setAssociationType(AssociationType newType);

    /**
     * Returns the kind of aggregation. The method never returns <code>null</code>.
     */
    public AggregationKind getAggregationKind();

    /**
     * Returns <code>true</code> if this is an association (no composition, no aggregation)
     * otherwise <code>false</code>.
     */
    public boolean isAssoziation();

    /**
     * Returns <code>true</code> if this is a derived association, otherwise <code>false</code>.
     */
    public boolean isDerived();

    /**
     * Returns the qualified name of the target type.
     */
    public String getTarget();

    /**
     * Returns the target type or <code>null</code> if either this association hasn't got a target
     * or the target does not exist.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException If an error occurs while searching for the target.
     */
    public IType findTarget(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the qualified name of the target type.
     */
    public void setTarget(String newTarget);

    /**
     * Returns the role of the target in this association.
     */
    public String getTargetRoleSingular();

    /**
     * Returns the default for the target role singular.
     */
    public String getDefaultTargetRoleSingular();

    /**
     * Sets the role of the target in this association. The role is specified in singular form, e.g.
     * policy and not policies. The distinction is more relevant in other languages than English,
     * where you can't derive the plural from the singular form.
     */
    public void setTargetRoleSingular(String newRole);

    /**
     * Returns the role of the target in this relation. The role is specified in plural form.
     */
    public String getTargetRolePlural();

    /**
     * Returns the default for the target role plural.
     */
    public String getDefaultTargetRolePlural();

    /**
     * Sets the new role in plural form of the target in this association.
     */
    public void setTargetRolePlural(String newRole);

    /**
     * Returns if the target role plural is required (or not) based on the associations's maximum
     * cardinality and the artifact builderset's information if it needs the plural form for to-1
     * relations.
     * 
     * @see IIpsArtefactBuilderSet#isRoleNamePluralRequiredForTo1Relations()
     */
    public boolean isTargetRolePluralRequired();

    /**
     * Returns the minimum number of target instances required in this association.
     */
    public int getMinCardinality();

    /**
     * Sets the minimum number of target instances required in this association.
     */
    public void setMinCardinality(int newValue);

    /**
     * Returns the maximum number of links allowed in this association. If this is a qualified
     * association, the max cardinality specifies the number of links per qualifier(!).
     * <p>
     * If the number is not limited, <tt>CARDINALITY_MANY</tt> is returned.
     */
    public int getMaxCardinality();

    /**
     * Returns <code>true</code> if this association is qualified, <code>false</code> otherwise.
     * <p>
     * At the moment only compositions between policy component types can be qualified. In this case
     * The qualifier is always the product component type that configures the target policy
     * component type.
     * <p>
     * For associations between product component types, this method returns <code>false</code>.
     */
    public boolean isQualified();

    /**
     * Returns <code>true</code> if this is a to-many association. This is the case if either the
     * max cardinality is greater than 1 or (!) this association is a qualified association. In the
     * latter case the max cardinality specifies the number of allowed links per qualifier
     * instance(!).
     * <p>
     * If this method returns <code>true</code> {{@link #is1To1()} returns <code>false</code> and
     * vice versa.
     */
    public boolean is1ToMany();

    /**
     * Returns <code>true</code> if the max cardinality is greater 1 and ignores if this is a
     * qualified association or not.
     */
    public boolean is1ToManyIgnoringQualifier();

    /**
     * Returns <code>true</code> if this is a 1 (or 0) to 1 association. This is the case if the max
     * cardinality is 1 and(!) the association is not qualified.
     * <p>
     * If this method returns <code>true</code> {{@link #is1ToMany()} returns <code>false</code> and
     * vice versa.
     */
    public boolean is1To1();

    /**
     * Sets the maximum number of target instances allowed in this association. An unlimited number
     * is represented by <tt>CARDINALITY_MANY</tt>.
     */
    public void setMaxCardinality(int newValue);

    /**
     * Returns <code>true</code> if this is a derived union. The term derived union is used as
     * defined in the UML specification.
     */
    public boolean isDerivedUnion();

    /**
     * Sets the information if this is a derived union. The term derived union is used as defined in
     * the UML specification.
     */
    public void setDerivedUnion(boolean flag);

    /**
     * Sets the derived union association that is subsetted by this association.
     */
    public void setSubsettedDerivedUnion(String newDerivedUnion);

    /**
     * Returns the name of the derived union association this one is a subset of.
     * <p>
     * Example: <br>
     * A <code>Policy</code> class has a 1-many association to it's <code>PolicyPart</code>s
     * (PolicyPartAssociation). Derived from <code>Policy</code> is a <code>MotorPolicy</code>.
     * Derived from <code>PolicyPart</code> is a <code>MotorCollisionPart</code>. There exists a 1-1
     * relation between <code>MotorPolicy</code> and <code>MotorCollisionPart</code>. To express
     * that the a motor policy instance returns the collision part when all it's parts
     * (PolicyPartAssociation) are requested, the policy part association has to be defined as
     * derived union. The association between the motor policy and the motor collision part then
     * defines a subset of the PolicyPartAssociation. Of course multiple subsets can belong to the
     * same derived union. For example a MotorTplCoverage could also be a policy part.
     */
    public String getSubsettedDerivedUnion();

    /**
     * Returns <code>true</code> if this association defines a subset of a derived union. Note that
     * it is possible for an association to be a derived union itself and also be a subset of
     * another derived union.
     */
    public boolean isSubsetOfADerivedUnion();

    /**
     * Returns <code>true</code> if this association defines a (direct) subset of the given derived
     * union, otherwise <code>false</code>. Returns <code>false</code> if derived union is
     * <code>null</code>. This method does not check if the given association is *really* a derived
     * union.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException If an error occurs while searching for the derived union.
     */
    public boolean isSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject ipsProject) throws CoreException;

    /**
     * Searches the derived union association and returns it, if it exists. Returns
     * <code>null</code> if the derived union does not exist. Note that this method does not check
     * if the association referred by this one as a derived union is <b>really</b> a derived union.
     * It just returns the association that is defined as being subsetted by this one.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IAssociation findSubsettedDerivedUnion(IIpsProject ipsProject) throws CoreException;

    /**
     * Searches for derived union associations in the type's hierarchy (of the type this association
     * belongs to) which are candidates for a derived union for this associations. This is the case
     * if an association in the type's hierarchy is marked as a derived union and the target is the
     * same or a supertype of this association's target.
     * <p>
     * Returns an empty array if no such associations exist.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IAssociation[] findDerivedUnionCandidates(IIpsProject ipsProject) throws CoreException;

}
