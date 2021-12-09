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

import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValueContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Common interface for all classes that can contain {@link IProductCmptLink product component
 * links}.
 * <p>
 * Created with the introduction of static associations. {@link IProductCmptLink product component
 * links} can now be part of both {@link IProductCmpt product components} and
 * {@link IProductCmptGeneration product component generations}. Thus both classes implement this
 * interface.
 * 
 * @since 3.8
 * @author widmaier
 */
public interface IProductCmptLinkContainer extends IProductPartsContainer, ITemplatedValueContainer {

    public static final String MSGCODE_PREFIX = "ProductCmptLinkContainer"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this link container contains less relations of a
     * specific relation type than specified in the model. E.g. a motor product must contain at
     * least one relation to a collision coverage component, but it does not.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the container and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     * 
     */
    public static final String MSGCODE_NOT_ENOUGH_RELATIONS = MSGCODE_PREFIX + "NotEnoughRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this link container contains more relations of a
     * specific relation type than specified in the model. E.g. a motor product can contain at most
     * one relation to a collision coverage component, but contains two (or more) relations to
     * collision coverage components.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the container and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     * 
     */
    public static final String MSGCODE_TOO_MANY_RELATIONS = MSGCODE_PREFIX + "ToManyRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that two or more relations of a specific type have the
     * same target.
     * 
     */
    public static final String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicates that a link references to a product component that
     * doesn't have an effective date that is before or equal to the effective date of the
     * referencing link container.
     */
    public static final String MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE = MSGCODE_PREFIX + "LinksWithWrongEffectivDate"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if this container is responsible for the given association. For
     * example a {@link IProductCmptGeneration} is only responsible for associations that may change
     * over time but not for unchanging (static) associations. This method will return
     * <code>false</code> in the latter case.
     * 
     * @param association The association that should be checked by the container
     * 
     * @return <code>true</code> if the association could part of this container, <code>false</code>
     *         otherwise.
     * 
     * @throws NullPointerException if the association is <code>null</code>
     */
    boolean isContainerFor(IProductCmptTypeAssociation association);

    /**
     * Returns the number of relations.
     */
    public int getNumOfLinks();

    /**
     * Creates a new link that is an instance of the product component type association identified
     * by the given association name.
     * 
     * @throws NullPointerException if associationName is <code>null</code>.
     */
    public IProductCmptLink newLink(String associationName);

    /**
     * Creates a new link that is an instance of the product component type association.
     * 
     * @throws NullPointerException if the association is <code>null</code>.
     */
    public IProductCmptLink newLink(IProductCmptTypeAssociation association);

    /**
     * Creates a new link that is an instance of the given association. The new link is placed
     * before the given one or at the end of the list of links, if the given link is
     * <code>null</code>.
     */
    public IProductCmptLink newLink(String association, IProductCmptLink insertAbove);

    /**
     * Checks whether a new link as instance of the given {@link IProductCmptTypeAssociation product
     * component type association} and the given target will be valid.
     * 
     * @param ipsProject The project whose IPS object path is used for the search. This is not
     *            necessarily the project this component is an element of.
     * 
     * @return <code>true</code> if a new relation with the given values will be valid,
     *         <code>false</code> otherwise.
     * 
     * @throws CoreRuntimeException if a problem occur during the search of the type hierarchy.
     */
    public boolean canCreateValidLink(IProductCmpt target,
            IProductCmptTypeAssociation association,
            IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Moves the link given with parameter <code>toMove</code> above or below the specified target
     * link. If the target belongs to another association, the association of the toMove link will
     * change, too. E.g. if the target link is based on association "standardCoverages" and the link
     * to move is an instance of the association "additionalCoverages". If calling move() returns
     * <code>true</code> (the link could be moved) the moved link's association will be
     * "standardCoverage", independent of the above/below flag.
     * <p>
     * The boolean parameter <code>above</code> specifies to move the link either above or below the
     * target link.
     * 
     * @param toMove the link you want to move
     * @param target target link you want to move the <code>toMove</code>-Link
     * @param above <code>true</code> to place the link above the target, <code>false</code> to
     *            place it below the target
     * 
     * @return The method returns <code>true</code> if the link could be moved and returns
     *         <code>false</code> if either the link or the target are <code>null</code> or if one
     *         of the links is not a part of this container.
     * 
     */
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean above);

    /**
     * Returns all {@link IProductCmptLink product component links} this class contains as a typed
     * list.
     */
    public List<IProductCmptLink> getLinksAsList();

    /**
     * Returns all {@link IProductCmptLink product component links} for the given association name
     * as a typed list.
     * 
     * @param associationName the name (=target role singular) of the association to return links
     *            for
     */
    public List<IProductCmptLink> getLinksAsList(String associationName);

    @Override
    public IProductCmpt getProductCmpt();

    @Override
    public String getProductCmptType();

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreRuntimeException;

    @Override
    public IProductCmptLinkContainer findTemplate(IIpsProject ipsProject);

    /**
     * Removes all links whose
     * {@link org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus
     * TemplateValueStatus} is
     * {@link org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus#UNDEFINED
     * UNDEFINED}
     */
    public void removeUndefinedLinks();

    /**
     * Returns the point in time from that on this generation contains the object's data.
     */
    public GregorianCalendar getValidFrom();

}
