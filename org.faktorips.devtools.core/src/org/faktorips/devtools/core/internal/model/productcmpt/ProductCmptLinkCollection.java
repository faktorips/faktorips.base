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

import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * Contains all {@link IProductCmptLink links} for one product component.
 * <p>
 * Introduced for static associations. Both {@link IProductCmpt product components} and
 * {@link IProductCmptGeneration product component generations} can contain links.
 * 
 * @since 3.8
 * @author widmaier
 */
public class ProductCmptLinkCollection {
    /**
     * Returns all links in this collection for a given association. Returns an empty list if there
     * are no associations for the given name.
     * 
     * @param associationName the association name whose instances (links) should be returned.
     */
    public List<IProductCmptLink> getLinks(String associationName) {
        return null;
    }

    /**
     * Returns all links in this collection. The associations are returned in their natural order.
     * This includes the order in which the associations are defined in the corresponding
     * {@link IProductCmptType} and the order of links for a specific association.
     * <p>
     * e.g. links for the association "standardCoverages" will be returned in front of links for
     * "additionalCoverages" if these associations were defined in the model that way. All links for
     * "additionalCoverages" will be returned in the order they are defined in the product component
     * (but after all "standardCoverages"-links).
     */
    public List<IProductCmptLink> getLinks() {
        return null;
    }

    public IProductCmptLink newLink(IProductCmptLinkContainer parent, IProductCmptTypeAssociation association) {
        return null;
    }

    public IProductCmptLink newLink(IProductCmptLinkContainer parent, String associationName) {
        return null;
    }

    public boolean addLink(IProductCmptLink link) {
        return false;

    }

    public boolean remove(IProductCmptLink link) {
        return false;

    }

    public void clear() {

    }
}
