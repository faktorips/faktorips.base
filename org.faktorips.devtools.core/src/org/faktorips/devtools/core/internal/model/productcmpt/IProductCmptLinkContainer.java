/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * Common interface for all classes that can contain {@link IProductCmptLink product component
 * links}.
 * <p>
 * Created with the introduction of static associations. {@link IProductCmptLink product component
 * links} can now be part of both {@link IProductCmpt product components} and
 * {@link ProductCmptGeneration product component generations}. Thus both classes implement this
 * interface.
 * 
 * @since 3.8
 * @author widmaier
 */
public interface IProductCmptLinkContainer extends IIpsObjectPartContainer {

    /**
     * Returns <code>true</code> if this container is responsible for the given association. For
     * example a {@link IProductCmptGeneration} is only responsible for associations that may change
     * over time but not for unchanging (static) associations. This method will return
     * <code>false</code> in the latter case.
     * <p>
     * The container could use {@link IProductCmptTypeAssociation#isChangingOverTime()} to decide
     * whether it is responsible for the given association or not.
     * 
     * @param association The association that should be checked by the container
     * 
     * @return <code>true</code> if the association could part of this container, <code>false</code>
     *         otherwise.
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
     * @throws CoreException if a problem occur during the search of the type hierarchy.
     * @see ProductCmptLinkContainerUtil
     */
    public boolean canCreateValidLink(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
            throws CoreException;

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
     * Returns all {@link ProductCmptLink product component links} this class contains as a typed
     * list.
     */
    List<IProductCmptLink> getLinkList();

    /**
     * Returns all {@link ProductCmptLink product component links} for the given association name as
     * a typed list.
     * 
     * @param associationName the name (=target role singular) of the association to return links
     *            for
     */
    List<IProductCmptLink> getLinkList(String associationName);

    /**
     * Returns the product component for this link container. If this container is a
     * {@link IProductCmptGeneration product component generation} the corresponding product
     * component is returned. If this is a {@link IProductCmpt product component} it returns itself.
     */
    IProductCmpt getProductCmpt();

}
