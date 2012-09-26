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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.NullArgumentException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Contains all {@link IProductCmptLink link} instances for one {@link IProductCmptLinkContainer
 * product component link container}. While link containers define a common interface for IPS
 * objects that can contain links, this class actually holds and manages the link instances. It is
 * designed to be used by {@link IProductCmptLinkContainer} implementations.
 * <p>
 * Introduced for static associations. Both {@link IProductCmpt product components} and
 * {@link IProductCmptGeneration product component generations} can contain links.
 * 
 * @since 3.8
 * @author widmaier
 */
public class ProductCmptLinkCollection {

    private Map<String, List<IProductCmptLink>> associationNameToLinkListMap = new LinkedHashMap<String, List<IProductCmptLink>>();

    /**
     * Returns all links in this collection for a given association. Returns an empty list if there
     * are no associations for the given name.
     * 
     * @param associationName the association name whose instances (links) should be returned.
     */
    public List<IProductCmptLink> getLinks(String associationName) {
        List<IProductCmptLink> linksForAssociation = associationNameToLinkListMap.get(associationName);
        if (linksForAssociation == null) {
            return new CopyOnWriteArrayList<IProductCmptLink>();
        } else {
            return new CopyOnWriteArrayList<IProductCmptLink>(linksForAssociation);
        }
    }

    /**
     * Returns all links in this collection.
     * 
     * 
     * The associations are returned in their natural order. This includes the order in which the
     * associations are defined in the corresponding {@link IProductCmptType} and the order of links
     * for a specific association.
     * <p>
     * e.g. links for the association "standardCoverages" will be returned in front of links for
     * "additionalCoverages" if these associations were defined in the model that way. All links for
     * "additionalCoverages" will be returned in the order they are defined in the product component
     * (but after all "standardCoverages"-links).
     */
    public List<IProductCmptLink> getLinks() {
        List<IProductCmptLink> allLinks = new ArrayList<IProductCmptLink>();
        for (List<IProductCmptLink> linkList : associationNameToLinkListMap.values()) {
            allLinks.addAll(linkList);
        }
        return allLinks;
    }

    /**
     * Creates (and returns) a new link.
     * 
     * @param container the container the new link should be part of
     * @param association the original association the new link is an instance of
     * @param partId the part id the new link should have
     * @return the newly creates
     */
    public IProductCmptLink newLink(IProductCmptLinkContainer container,
            IProductCmptTypeAssociation association,
            String partId) {
        return null;
    }

    /**
     * Creates (and returns) a new link.
     * 
     * @param container the container the new link should be part of
     * @param associationName the name of the original association the new link is an instance of
     * @param partId the part id the new link should have
     * @return the newly creates
     */
    public IProductCmptLink newLink(IProductCmptLinkContainer container, String associationName, String partId) {
        return null;
    }

    protected IProductCmptLink createLink(IProductCmptLinkContainer container, String associationName, String partId) {
        // IProductCmptLink link = new ProductCmptLink(container, partId);

        return null;
    }

    /**
     * Adds the given link to this link collection.
     * 
     * @param link the link to be added
     * @return <code>true</code> if the link could be added to this collection, <code>false</code>
     *         otherwise.
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
        if (link.getAssociation() == null) {
            throw new IllegalArgumentException(
                    NLS.bind(
                            "Cannot add {0} as it does not belong to any association (IProductCmptLink#getAssociation() returned null).", //$NON-NLS-1$
                            link));
        }
    }

    private boolean addLinkInternal(IProductCmptLink link) {
        String associationName = link.getAssociation();
        List<IProductCmptLink> linksForAssciation = associationNameToLinkListMap.get(associationName);
        if (linksForAssciation == null) {
            linksForAssciation = new ArrayList<IProductCmptLink>();
            associationNameToLinkListMap.put(associationName, linksForAssciation);
        }
        linksForAssciation.add(link);
        return false;
    }

    /**
     * Removes the given link from this link collection.
     * 
     * @param link the link to be removed
     * @return <code>true</code> if the link was removed from this collection, <code>false</code>
     *         otherwise.
     */
    public boolean remove(IProductCmptLink link) {
        return false;

    }

    /**
     * Removes all links from this collection.
     */
    public void clear() {

    }
}
