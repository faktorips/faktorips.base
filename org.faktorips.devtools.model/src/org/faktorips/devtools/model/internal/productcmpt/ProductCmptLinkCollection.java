/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang.NullArgumentException;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

/**
 * Contains all {@link IProductCmptLink link} instances for one {@link IProductCmptLinkContainer
 * product component link container}. While link containers define a common interface for IPS
 * objects that can contain links, this class actually holds and manages the link instances. It is
 * designed to be used by {@link IProductCmptLinkContainer} implementations.
 * <p>
 * Introduced for static associations. Both {@link IProductCmpt product components} and
 * {@link IProductCmptGeneration product component generations} can contain links.
 * <p>
 * Note: this class does not ensure that all link instances are part of the same parent. It accepts
 * all link instance regardless of their parent. It also accepts links with undefined association
 * (e.g. <code>link.getAssociation()</code> returns <code>null</code>)
 * 
 * @since 3.8
 * @author widmaier
 */
public class ProductCmptLinkCollection {

    private final List<IProductCmptLink> links = new ArrayList<>();

    /**
     * Returns all links in this collection for a given association. Returns an empty list if there
     * are no associations for the given name.
     * <p>
     * The links are returned in the same order they were inserted into this collection.
     * 
     * @param associationName the association name whose instances (links) should be returned.
     */
    public List<IProductCmptLink> getLinks(String associationName) {
        return links.stream()
                .filter(l -> associationName.equals(l.getAssociation()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all links in this collection as a map. The name of the product component type
     * association is used as a key. A list of all links belonging to a specific association is the
     * value. e.g. <code>getLinksAsMap().get("aSpecificAssociation")</code> will return a list of
     * all links that are instances of the association "aSpecificAssociation".
     * <p>
     * Allows links with association <code>null</code>. Thus all all links with undefined
     * association are returned when calling <code>getLinksAsMap().get(null)</code>.
     */
    public Map<String, List<IProductCmptLink>> getLinksAsMap() {
        Map<String, List<IProductCmptLink>> associationNameToLinksMap = new LinkedHashMap<>();
        for (IProductCmptLink link : links) {
            associationNameToLinksMap.computeIfAbsent(link.getAssociation(), $ -> new ArrayList<>()).add(link);
        }
        return associationNameToLinksMap;
    }

    /**
     * Returns all links in this collection.
     * <p>
     * The links are returned in a consistent order that is mostly defined by their insertion order.
     * Firstly all links are ordered by their original association. Which means first all links for
     * association "oneAssociation" are returned then all links for "anotherAssociation" follow. The
     * order of those associations is defined by the occurrence of link instances. The links for
     * each association on the other hand are returned in the same order they were inserted into
     * this collection.
     * <p>
     * e.g. links for the association "standardCoverages" will be returned above (or in front of)
     * links for "additionalCoverages" if these associations were inserted that way. All links for
     * "additionalCoverages" will be returned in the order they are defined in the product component
     * editor (but after all "standardCoverages"-links).
     */
    public List<IProductCmptLink> getLinks() {
        // a stream with flatMap would be nice code, but not as efficient
        List<IProductCmptLink> allLinks = new ArrayList<>();
        for (List<IProductCmptLink> linkList : getLinksAsMap().values()) {
            allLinks.addAll(linkList);
        }
        return allLinks;
    }

    /**
     * Adds the given link to this link collection.
     * 
     * @param link the link to be added
     * @return <code>true</code> if the link could be added to this collection, <code>false</code>
     *             otherwise.
     * @throws NullPointerException if the given link is <code>null</code>.
     * @throws IllegalArgumentException if the given link does not belong to any association, IOW
     *             {@link IProductCmptLink#getAssociation()} returns <code>null</code>.
     */
    public boolean addLink(IProductCmptLink link) {
        throwExceptionIfLinkCannotBeAdded(link);
        return addLinkInternal(link);
    }

    private void throwExceptionIfLinkCannotBeAdded(IProductCmptLink link) {
        if (link == null) {
            throw new NullArgumentException("Cannot add \"null\" to a ProductCmptLinkCollection"); //$NON-NLS-1$
        }
    }

    /**
     * Creates (and returns) a new link.
     * 
     * @param container the container the new link should be part of
     * @param associationName the name of the original association the new link is an instance of
     * @param partId the part id the new link should have
     * @return the newly creates
     */
    public IProductCmptLink createAndAddNewLink(IProductCmptLinkContainer container,
            String associationName,
            String partId) {
        IProductCmptLink link = createLink(container, associationName, partId);
        addLink(link);
        return link;
    }

    /**
     * Creates (and returns) a new link. The new link is inserted above the link
     * <code>insertAbove</code> if possible.
     * 
     * @param container the container the new link should be part of
     * @param associationName the name of the original association the new link is an instance of
     * @param partId the part id the new link should have
     * @param insertAbove the newly create link is inserted into this collection above this link
     * @return the newly creates
     */
    public IProductCmptLink createAndInsertNewLink(IProductCmptLinkContainer container,
            String associationName,
            String partId,
            IProductCmptLink insertAbove) {
        IProductCmptLink link = createLink(container, associationName, partId);
        insertLink(link, insertAbove);
        return link;
    }

    protected IProductCmptLink createLink(IProductCmptLinkContainer container, String associationName, String partId) {
        IProductCmptLink link = new ProductCmptLink(container, partId);
        link.setAssociation(associationName);
        return link;
    }

    private boolean addLinkInternal(IProductCmptLink link) {
        return links.add(link);
    }

    /**
     * Adds <code>link</code> to this container and places it above (or in front of) the link
     * <code>insertAbove</code>. Not though, that this does not necessarily mean <code>link</code>
     * is returned above <code>insertAbove</code> when calling {@link #getLinks()}. The specified
     * order is only retained if both links are instance of the same association.
     * 
     * @param link the link to insert
     * @param insertAbove the link above (or in front of) which the link should be inserted. Ignored
     *            if <code>null</code> or if not part of this collection.
     */
    public void insertLink(IProductCmptLink link, IProductCmptLink insertAbove) {
        if (insertAbove == null) {
            links.add(link);
        } else {
            int index = links.indexOf(insertAbove);
            if (index == -1) {
                links.add(link);
            } else {
                links.add(index, link);
            }
        }
    }

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
     *             <code>false</code> if either the link or the target are <code>null</code> or if
     *             one of the links is not a part of this container.
     * 
     */
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean above) {
        // if toMove and target are the same we have to do nothing
        if (toMove == target) {
            return true;
        }
        if (toMove == null || target == null || !links.contains(target)) {
            return false;
        }
        boolean removed = links.remove(toMove);
        if (!removed) {
            return false;
        }
        int index = links.indexOf(target);
        if (!above) {
            index++;
        }
        links.add(index, toMove);
        toMove.setAssociation(target.getAssociation());
        return true;
    }

    /**
     * Removes the given link from this link collection. Does nothing if the given link is
     * <code>null</code> or if it does not belong to any association, IOW
     * {@link IProductCmptLink#getAssociation()} returns <code>null</code>.
     * 
     * @param link the link to be removed
     * @return <code>true</code> if the link was removed from this collection, <code>false</code>
     *             otherwise.
     */
    public boolean remove(IProductCmptLink link) {
        if (link == null) {
            return false;
        }
        return removeInternal(link);
    }

    private boolean removeInternal(IProductCmptLink link) {
        return links.remove(link);
    }

    /**
     * Removes all links from this collection.
     */
    public void clear() {
        links.clear();
    }

    /**
     * Returns <code>true</code> if the link is contained in this collection, <code>false</code>
     * else.
     * 
     * @param link the link to be searched for in this collection
     */
    public boolean containsLink(IProductCmptLink link) {
        if (link == null) {
            return false;
        }
        return containsLinkInternal(link);
    }

    private boolean containsLinkInternal(IProductCmptLink link) {
        return links.contains(link);
    }

    /**
     * Add the qualified name types of all related product cmpt's inside the given generation to the
     * given set
     */
    public void addRelatedProductCmptQualifiedNameTypes(Set<IDependency> qaTypes,
            Map<IDependency, List<IDependencyDetail>> details) {
        for (IProductCmptLink link : links) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(link.getIpsObject()
                    .getQualifiedNameType(), new QualifiedNameType(link.getTarget(), IpsObjectType.PRODUCT_CMPT));
            qaTypes.add(dependency);

            if (details != null) {
                List<IDependencyDetail> detailList = details.computeIfAbsent(dependency,
                        $ -> new ArrayList<>());
                detailList.add(new DependencyDetail(link, IProductCmptLink.PROPERTY_TARGET));
            }
        }
    }

    /**
     * Returns the number links currently stored in this collection.
     */
    public int size() {
        return links.size();
    }

    /**
     * Removes all links whose {@link TemplateValueStatus} is {@link TemplateValueStatus#UNDEFINED}.
     */
    public void removeUndefinedLinks() {
        for (IProductCmptLink link : new CopyOnWriteArrayList<>(links)) {
            if (link.getTemplateValueStatus() == TemplateValueStatus.UNDEFINED) {
                remove(link);
            }
        }
    }

}
