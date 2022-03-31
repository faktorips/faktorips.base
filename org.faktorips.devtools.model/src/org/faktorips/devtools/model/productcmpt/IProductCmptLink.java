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

import org.apache.commons.lang.StringUtils;
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

    public static final String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public static final String PROPERTY_ASSOCIATION = "association"; //$NON-NLS-1$
    public static final String PROPERTY_CARDINALITY = "cardinality"; //$NON-NLS-1$
    public static final String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public static final String PROPERTY_DEFAULT_CARDINALITY = "defaultCardinality"; //$NON-NLS-1$
    public static final String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$

    /**
     * The name of the XML-tag used if this object is saved to XML.
     */
    public static final String TAG_NAME = "Link"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "PRODUCTCMPT_RELATION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the association in the model can't be found.
     */
    public static final String MSGCODE_UNKNWON_ASSOCIATION = MSGCODE_PREFIX + "UnknownAssociation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target product component does not exist.
     */
    public static final String MSGCODE_UNKNWON_TARGET = MSGCODE_PREFIX + "UnknownTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is missing.
     */
    public static final String MSGCODE_MISSING_MAX_CARDINALITY = MSGCODE_PREFIX + "MissingMaxCardinality"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality exceeds the maximum
     * cardinality defined in the model.
     */
    public static final String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = MSGCODE_PREFIX
            + "MaxCardinalityExceedsModelMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate minimum cardinality falls below the minimum cardinality
     * defined in the model.
     */
    public static final String MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN = MSGCODE_PREFIX
            + "MinCardinalityFallsBelowModelMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than the minimum
     * cardinality.
     */
    public static final String MSGCODE_INVALID_TARGET = MSGCODE_PREFIX + "InvalidTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate a mismatch with the specification in the product
     * component type association. A message with this message code is added if this link is part of
     * a container that changes over time but the product component type association is defined as
     * static (not changing over time), et vice versa.
     * 
     * @since 3.8
     */
    public static final String MSGCODE_CHANGING_OVER_TIME_MISMATCH = MSGCODE_PREFIX + "ChangingOverTimeMismatch"; //$NON-NLS-1$

    /**
     * Returns the product component this configuration element belongs to.
     */
    public IProductCmpt getProductCmpt();

    /**
     * Returns the {@link IProductCmptLinkContainer link container} this link is a part of.
     * 
     * @since 3.8
     * @see IProductCmptLinkContainer
     */
    public IProductCmptLinkContainer getProductCmptLinkContainer();

    /**
     * Returns the name of the product component type association this link is an instance of.
     */
    public String getAssociation();

    /**
     * Setting the association this link is an instance of
     */
    public void setAssociation(String association);

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
    public IProductCmptTypeAssociation findAssociation(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the target product component.
     */
    public String getTarget();

    /**
     * Sets the target product component.
     */
    public void setTarget(String newTarget);

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
    public IProductCmpt findTarget(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the cardinality of target instances in this link
     */
    public Cardinality getCardinality();

    /**
     * Set the cardinality of target instances in this link
     */
    public void setCardinality(Cardinality cardinality);

    /**
     * Returns the minimum number of target instances required by this link.
     */
    public int getMinCardinality();

    /**
     * Sets the minimum number of target instances required by this link.
     */
    public void setMinCardinality(int newValue);

    /**
     * returns the default number of target instances in this link.
     */
    public int getDefaultCardinality();

    /**
     * Sets the default number of target instances in this link.
     */
    public void setDefaultCardinality(int newValue);

    /**
     * Returns the maximum number of target instances allowed in this relation. If the number is not
     * limited {@link Cardinality#CARDINALITY_MANY} is returned.
     */
    public int getMaxCardinality();

    /**
     * Sets the maximum number of target instances allowed in this relation. An unlimited number is
     * represented by {@link Cardinality#CARDINALITY_MANY}.
     */
    public void setMaxCardinality(int newValue);

    /**
     * Returns true if the association this link is an instance of does constrains a policy
     * component type association
     * 
     * @see IProductCmptTypeAssociation#constrainsPolicyCmptTypeAssociation(IIpsProject)
     */
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject);

    /**
     * Returns whether this relation is mandatory. A Relation is mandatory if both minimum and
     * maximum-cardinality are equal to 1.
     */
    public boolean isMandatory();

    /**
     * Returns whether this Relation is optional. A Relation is optional if the minimum cardinality
     * equals 0 and the maximum cardinality equals 1.
     * 
     * @return <code>true</code> if this Relation is optional, else <code>false</code>.
     */
    public boolean isOptional();

    /**
     * Returns <code>true</code> if this is a to-many association. This is the case if the max
     * cardinality is greater than 1.
     */
    public boolean is1ToMany();

    /**
     * Checks if this link is a link of the given association. This includes the search for derived
     * union associations if the link's is a subset of a derived union.
     * 
     * @param association The association that should be checked
     * @param ipsProject The project used to find the associations
     * @return true if this link is a link instance of the given association including search for
     *         derived unions.
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
    public IProductCmptLink findTemplateProperty(IIpsProject ipsProject);

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
            result = prime * result + target.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LinkIdentifier other = (LinkIdentifier)obj;
            if (!association.equals(other.association)) {
                return false;
            }
            if (!target.equals(other.target)) {
                return false;
            }
            return true;
        }

        @Override
        public IProductCmptLink getValueFrom(ITemplatedValueContainer container) {
            ArgumentCheck.isInstanceOf(container, IProductCmptLinkContainer.class);
            IProductCmptLinkContainer linkContainer = (IProductCmptLinkContainer)container;
            for (IProductCmptLink link : linkContainer.getLinksAsList(association)) {
                if (StringUtils.equals(target, link.getTarget())) {
                    return link;
                }
            }
            return null;
        }
    }

}
