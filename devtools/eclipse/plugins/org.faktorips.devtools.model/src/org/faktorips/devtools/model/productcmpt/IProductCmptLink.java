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
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueIdentifier;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;

/**
 * A link between two product components. A link is an instance of an association between product
 * component types.
 *
 * @see IProductCmptTypeAssociation
 */
public interface IProductCmptLink extends IDescribedElement, ITemplatedValue {

    String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    String PROPERTY_TARGET_RUNTIME_ID = "targetRuntimeId"; //$NON-NLS-1$
    String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    String PROPERTY_CARDINALITY = "cardinality"; //$NON-NLS-1$
    String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    String PROPERTY_DEFAULT_CARDINALITY = "defaultCardinality"; //$NON-NLS-1$
    String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$

    /**
     * The name of the XML-tag used if this object is saved to XML.
     */
    String TAG_NAME = "Link"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "PRODUCTCMPT_RELATION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the association in the model can't be found.
     */
    String MSGCODE_UNKNWON_ASSOCIATION = MSGCODE_PREFIX + "UnknownAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target product component does not exist.
     */
    String MSGCODE_UNKNWON_TARGET = MSGCODE_PREFIX + "UnknownTarget"; //$NON-NLS-1$

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
     *
     * @since 3.8
     */
    String MSGCODE_CHANGING_OVER_TIME_MISMATCH = MSGCODE_PREFIX + "ChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the product component this configuration element belongs to.
     */
    IProductCmpt getProductCmpt();

    /**
     * Returns the {@link IProductCmptLinkContainer link container} this link is a part of.
     *
     * @since 3.8
     * @see IProductCmptLinkContainer
     */
    IProductCmptLinkContainer getProductCmptLinkContainer();

    /**
     * Returns the name of the product component type association this link is an instance of.
     */
    String getAssociation();

    /**
     * Setting the association this link is an instance of
     */
    void setAssociation(String association);

    /**
     * Finds the product component type association this link is an instance of. Note that the
     * method searches not only the direct product component type this product component is based
     * on, but also it's super type hierarchy.
     *
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     *
     * @return the association or <code>null</code> if no such association exists.
     *
     * @throws IpsException if an exception occurs while searching the relation.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the target product component.
     */
    String getTarget();

    /**
     * Sets the target product component.
     */
    void setTarget(String newTarget);

    /**
     * Returns the product component which is the target of this association or <code>null</code>,
     * if no (valid) target name is set.
     *
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this component is part of.
     *
     * @throws IpsException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    IProductCmpt findTarget(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the runtime ID of the target product component.
     *
     * @since 23.6
     */
    String getTargetRuntimeId();

    /**
     * Sets the runtime ID of the target product component.
     *
     * @since 23.6
     */
    void setTargetRuntimeId(String newTargetRuntimeId);

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
     * returns the default number of target instances in this link.
     */
    int getDefaultCardinality();

    /**
     * Sets the default number of target instances in this link.
     */
    void setDefaultCardinality(int newValue);

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
     * Returns true if the association this link is an instance of does constrains a policy
     * component type association
     *
     * @see IProductCmptTypeAssociation#constrainsPolicyCmptTypeAssociation(IIpsProject)
     */
    boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject);

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

    /**
     * Checks if this link is a link of the given association. This includes the search for derived
     * union associations if the link's is a subset of a derived union.
     *
     * @param association The association that should be checked
     * @param ipsProject The project used to find the associations
     * @return true if this link is a link instance of the given association including search for
     *             derived unions.
     *
     * @throws IpsException Throws a core exception if there occurs exception during finding other
     *             objects
     */
    boolean isLinkOfAssociation(IAssociation association, IIpsProject ipsProject) throws IpsException;

    /**
     * Returns {@code true} if this link is configuring an association on the policy component
     * configured by it parent container. Returns {@code false} if its parent does not configure a
     * policy component or this link does not configure an association in the policy component.
     */
    boolean isConfiguringPolicyAssociation();

    @Override
    IProductCmptLink findTemplateProperty(IIpsProject ipsProject);

    @Override
    Function<IProductCmptLink, Object> getValueGetter();

    @Override
    Function<IProductCmptLink, Object> getInternalValueGetter();

    /** A class that can be used to identify links by means of their association and target. */
    class LinkIdentifier implements ITemplatedValueIdentifier {

        private final String association;
        private final String target;

        public LinkIdentifier(String association, String target) {
            super();
            this.association = Objects.requireNonNull(association);
            this.target = Objects.requireNonNull(target);
        }

        public LinkIdentifier(IProductCmptLink link) {
            this(link.getAssociation(), link.getTarget());
        }

        public String getAssociation() {
            return association;
        }

        public String getTarget() {
            return target;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + association.hashCode();
            return prime * result + target.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            LinkIdentifier other = (LinkIdentifier)obj;
            return Objects.equals(association, other.association)
                    && Objects.equals(target, other.target);
        }

        @Override
        public IProductCmptLink getValueFrom(ITemplatedValueContainer container) {
            ArgumentCheck.isInstanceOf(container, IProductCmptLinkContainer.class);
            IProductCmptLinkContainer linkContainer = (IProductCmptLinkContainer)container;
            for (IProductCmptLink link : linkContainer.getLinksAsList(association)) {
                if (Objects.equals(target, link.getTarget())) {
                    return link;
                }
            }
            return null;
        }
    }

}
