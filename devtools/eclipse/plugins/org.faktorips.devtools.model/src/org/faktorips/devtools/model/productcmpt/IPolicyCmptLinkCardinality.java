/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.Objects;
import java.util.function.Function;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.util.ArgumentCheck;

/**
 * The cardinality configuration for a policy component association.
 *
 * @see IPolicyCmptTypeAssociation
 * @since 26.7
 */
public interface IPolicyCmptLinkCardinality extends ITemplatedValue {

    String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    String PROPERTY_CARDINALITY = "cardinality"; //$NON-NLS-1$
    String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$

    /**
     * The name of the XML-tag used if this object is saved to XML.
     */
    String TAG_NAME = "PolicyLinkCardinality"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "POLICY_LINK_CARDINALITY-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the association in the model can't be found.
     */
    String MSGCODE_UNKNOWN_ASSOCIATION = MSGCODE_PREFIX + "UnknownAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is missing.
     */
    String MSGCODE_MISSING_MAX_CARDINALITY = MSGCODE_PREFIX + "MissingMaxCardinality"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality exceeds the maximum
     * cardinality defined in the model.
     */
    String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = MSGCODE_PREFIX
            + "MaxCardinalityExceedsModelMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate minimum cardinality falls below the minimum cardinality
     * defined in the model.
     */
    String MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN = MSGCODE_PREFIX
            + "MinCardinalityFallsBelowModelMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum
     * cardinality.
     */
    String MSGCODE_INVALID_TARGET = MSGCODE_PREFIX + "InvalidTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate a mismatch with the specification in the product
     * component type association. A message with this message code is added if this link is part of
     * a container that changes over time but the product component type association is defined as
     * static (not changing over time), et vice versa.
     */
    String MSGCODE_CHANGING_OVER_TIME_MISMATCH = MSGCODE_PREFIX + "ChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the {@link IProductCmptLinkContainer link container} this cardinality is a part of.
     */
    IProductCmptLinkContainer getProductCmptLinkContainer();

    /**
     * Returns the name of the policy component type association for which this is the cardinality
     * configuration.
     */
    String getAssociation();

    /**
     * Sets the name of the policy component type association for which this is the cardinality
     * configuration.
     */
    void setAssociation(String association);

    /**
     * Finds the policy component type association this link is an instance of. Note that the method
     * searches not only the direct policy component type configured by the product component, but
     * also it's super type hierarchy.
     *
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     *
     * @return the association or {@code null} if no such association exists.
     *
     * @throws IpsException if an exception occurs while searching the relation.
     * @throws NullPointerException if ipsProject is {@code null}.
     */
    IPolicyCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the cardinality of target instances in this link
     */
    Cardinality getCardinality();

    /**
     * Set the cardinality of target instances in this link
     */
    void setCardinality(Cardinality cardinality);

    /**
     * Returns the minimum number of target instances required by this link.
     */
    int getMinCardinality();

    /**
     * Sets the minimum number of target instances required by this link.
     */
    void setMinCardinality(int newValue);

    /**
     * Returns the maximum number of target instances allowed in this relation. If the number is not
     * limited {@link Cardinality#CARDINALITY_MANY} is returned.
     */
    int getMaxCardinality();

    /**
     * Sets the maximum number of target instances allowed in this relation. An unlimited number is
     * represented by {@link Cardinality#CARDINALITY_MANY}.
     */
    void setMaxCardinality(int newValue);

    /**
     * Returns whether this relation is mandatory. A Relation is mandatory if both minimum and
     * maximum-cardinality are equal to 1.
     */
    boolean isMandatory();

    /**
     * Returns whether this Relation is optional. A Relation is optional if the minimum cardinality
     * equals 0 and the maximum cardinality equals 1.
     *
     * @return <code>true</code> if this Relation is optional, else <code>false</code>.
     */
    boolean isOptional();

    /**
     * Returns <code>true</code> if this is a to-many association. This is the case if the max
     * cardinality is greater than 1.
     */
    boolean is1ToMany();

    @Override
    IPolicyCmptLinkCardinality findTemplateProperty(IIpsProject ipsProject);

    @Override
    Function<IPolicyCmptLinkCardinality, Object> getValueGetter();

    @Override
    Function<IPolicyCmptLinkCardinality, Object> getInternalValueGetter();

    /** A class that can be used to identify links by means of their association. */
    class PolicyCmptLinkIdentifier implements ITemplatedValueIdentifier {

        private final String association;

        public PolicyCmptLinkIdentifier(String association) {
            this.association = association;
        }

        public PolicyCmptLinkIdentifier(IPolicyCmptLinkCardinality linkCardinality) {
            super();
            association = linkCardinality.getAssociation();
        }

        public String getAssociation() {
            return association;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(association);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            PolicyCmptLinkIdentifier other = (PolicyCmptLinkIdentifier)obj;
            return Objects.equals(association, other.association);
        }

        @Override
        public IPolicyCmptLinkCardinality getValueFrom(ITemplatedValueContainer container) {
            ArgumentCheck.isInstanceOf(container, IProductCmptLinkContainer.class);
            IProductCmptLinkContainer linkContainer = (IProductCmptLinkContainer)container;
            return linkContainer.getPolicyCmptLinkCardinality(association).orElse(null);
        }
    }

}
