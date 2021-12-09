/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpttype;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IChangingOverTimeProperty;
import org.faktorips.runtime.model.type.PolicyCmptType;

/**
 * A directed relationship between to product component types.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAssociation extends IAssociation, IChangingOverTimeProperty {

    /**
     * The list of applicable types. For product component types only aggregations and associations
     * are supported.
     */
    public static final AssociationType[] APPLICABLE_ASSOCIATION_TYPES = new AssociationType[] {
            AssociationType.AGGREGATION, AssociationType.ASSOCIATION };

    public static final String PROPERTY_MATCHING_ASSOCIATION_SOURCE = "matchingAssociationSource"; //$NON-NLS-1$

    public static final String PROPERTY_MATCHING_ASSOCIATION_NAME = "matchingAssociationName"; //$NON-NLS-1$

    public static final String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    public static final String PROPERTY_RELEVANT = "relevant"; //$NON-NLS-1$

    /**
     * Message code for validation messages when the matching association was not found
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_NOT_FOUND = IAssociation.MSGCODE_PREFIX
            + "MatchingAssociationNotFound"; //$NON-NLS-1$

    /**
     * Message code for validation messages when the matching association is invalid
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_INVALID = IAssociation.MSGCODE_PREFIX
            + "MatchingAssociationInvalid"; //$NON-NLS-1$

    /**
     * Message code when derived unions and their subsets have different changing over time
     * properties. i.e. a subset is defined as changing over time, but the derived union is defined
     * as static.
     */
    public static final String MSGCODE_DERIVED_UNION_CHANGING_OVER_TIME_MISMATCH = IAssociation.MSGCODE_PREFIX
            + "DerivedUnionChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that two associations matching associations with the same
     * name. Although these associations could be two different ones we would generate duplicated
     * methods.
     */
    public static final String MSGCODE_MATCHING_ASSOCIATION_DUPLICATE_NAME = IAssociation.MSGCODE_PREFIX
            + "MatchingAssociationDuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the property change over time mismatch with the
     * constrained association
     */
    public static final String MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH = IAssociation.MSGCODE_PREFIX
            + "ConstrainedChangeOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the product component type this relation belongs to. Never returns <code>null</code>.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Returns the target product component type or <code>null</code> if either this relation hasn't
     * got a target or the target does not exists.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    public IProductCmptType findTargetProductCmptType(IIpsProject project);

    /**
     * Returns <code>true</code> if this association constrains a policy component type association,
     * otherwise <code>false</code>. If this method returns <code>true</code>,
     * {@link #findMatchingPolicyCmptTypeAssociation(IIpsProject)} returns the constrained
     * association, otherwise the finder method returns <code>null</code>.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject);

    /**
     * Returns the corresponding policy component type association or <code>null</code> if no such
     * association is found. If both {@link #getMatchingAssociationSource()} and
     * {@link #getMatchingAssociationName()} are not empty, the explicitly specified matching
     * association will be returned. Otherwise this method would try to find a matching association
     * automatically by comparing the associations target and order.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject);

    /**
     * Returns the corresponding policy component type association that is set per default or
     * <code>null</code> if no such association is found. This method would try to find a matching
     * association automatically by comparing the associations target and order.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    public IPolicyCmptTypeAssociation findDefaultPolicyCmptTypeAssociation(IIpsProject ipsProject);

    /**
     * Finding all {@link IPolicyCmptTypeAssociation IPolicyCmptTypeAssociations} that could be
     * configured by this {@link IProductCmptTypeAssociation}. In normal scenario these are all
     * Associations (never Detail-To-Master) from the {@link IPolicyCmptType} that is configured by
     * the source of this association to the {@link IPolicyCmptType} that is configured by the
     * target of this association. But there could also be associations in {@link PolicyCmptType
     * PolicyCmptTypes} that are not configured by any {@link IProductCmptType}. This scenario is
     * described in FIPS-563.
     * 
     * @param ipsProject The {@link IIpsProject} used as search base project
     * @return the list of all {@link IPolicyCmptTypeAssociation} that could potentially be
     *         configured by this {@link IProductCmptTypeAssociation}
     * 
     * @throws CoreRuntimeException In case of a core exception while loading the objects and resources
     */
    Set<IPolicyCmptTypeAssociation> findPossiblyMatchingPolicyCmptTypeAssociations(IIpsProject ipsProject)
            throws CoreRuntimeException;

    /**
     * Setting the name of the explicitly specified matching association. If the matching
     * association should be found automatically this field should be set to
     * {@link StringUtils#EMPTY}
     * <p>
     * The policy component type of the explicitly matching association is set by
     * {@link #setMatchingAssociationSource(String)}
     * 
     * @param matchingAssociationName the name of the matching association
     */
    void setMatchingAssociationName(String matchingAssociationName);

    /**
     * Getting the name of the explicitly specified matching association. If the matching
     * association should be found automatically, this field is empty.
     * <p>
     * The explicitly matching association is located in the policy component type specified by
     * {@link #getMatchingAssociationSource()}
     * 
     * @return The name of the matching association
     */
    String getMatchingAssociationName();

    /**
     * Setting the qualified name of the explicitly specified matching association source. If the
     * matching association should be found automatically, this field should be set to
     * {@link StringUtils#EMPTY}.
     * <p>
     * The name of the explicitly matching association is set by
     * {@link #setMatchingAssociationName(String)}
     * 
     * @param matchingAssociationSource The name of the policy component type which association
     *            should be constrained
     */
    void setMatchingAssociationSource(String matchingAssociationSource);

    /**
     * Getting the qualified name of the policy component type for the explicitly specified matching
     * association. If the matching association should be found automatically, this field should be
     * empty.
     * <p>
     * The name of the explicitly matching association is get by
     * {@link #getMatchingAssociationName()}
     * 
     * @return The qualified name of the policy component type which association is constrained
     */
    String getMatchingAssociationSource();

    /**
     * Marks this association as changing over time (<code>true</code>) or static (
     * <code>false</code>). Instances of changing-over-time associations ({@link IProductCmptLink
     * product component links}) will be part of {@link IProductCmptGeneration product component
     * generations}. Each generation may specify a different target component. Instances of static
     * associations will be part of the {@link IProductCmpt product component} directly.
     * 
     * @param changingOverTime <code>true</code> if instances of this associations change over time,
     *            <code>false</code> if they are static.
     */
    void setChangingOverTime(boolean changingOverTime);

    /**
     * Returns whether this association is relevant or not.
     * <p>
     * If this method returns <code>true</code>, the association will be displayed in the product
     * component editor and can be edited by the user. If this method returns <code>false</code>,
     * the association will not be displayed in the editor and its value cannot be modified. This
     * property also affects the product structure explorer.
     * <p>
     * The default value is <code>true</code>.
     */
    boolean isRelevant();

    /**
     * Sets the property <code>relevant</code> for this association.
     * <p>
     * If this association is marked as relevant (<code>true</code>), the association will be
     * displayed in the component editor and will be taken into account in the product structure. If
     * marked as not relevant, the association will not be displayed in the component editor and
     * will be ignored in product structure.
     * 
     * @param relevant true to mark the association as relevant, false to mark it as not relevant
     */
    void setRelevant(boolean relevant);

}
