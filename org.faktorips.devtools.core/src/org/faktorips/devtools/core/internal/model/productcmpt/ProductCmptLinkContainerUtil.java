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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * Utility class used by {@link IProductCmptLinkContainer} implementations. The code was extracted
 * from the original {@link IProductCmptGeneration} implementation with the introduction of static
 * associations, so it could be used by other link containers also.
 * 
 * @since 3.8
 * 
 * @author widmaier
 */
public class ProductCmptLinkContainerUtil {

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
     */
    public static boolean canCreateValidLink(IProductCmptLinkContainer linkContainer,
            IProductCmpt target,
            IAssociation association,
            IIpsProject ipsProject) throws CoreException {

        if (association == null || target == null || !linkContainer.getProductCmpt().getIpsSrcFile().isMutable()) {
            return false;
        }
        IProductCmptType type = linkContainer.getProductCmpt().findProductCmptType(ipsProject);
        if (type == null) {
            return false;
        }
        // it is not valid to create more than one relation with the same type and target.
        if (!isFirstRelationOfThisType(linkContainer, association, target, ipsProject)) {
            return false;
        }
        // is correct type
        IProductCmptType targetType = target.findProductCmptType(ipsProject);
        if (targetType == null) {
            return false;
        }
        if (!targetType.isSubtypeOrSameType(association.findTarget(ipsProject), ipsProject)) {
            return false;
        }

        return linkContainer.getLinksAsList(association.getName()).size() < association.getMaxCardinality()
                && ProductCmptLink.willBeValid(target, association, ipsProject);
    }

    private static boolean isFirstRelationOfThisType(IProductCmptLinkContainer linkContainer,
            IAssociation association,
            IProductCmpt target,
            IIpsProject ipsProject) throws CoreException {

        // TODO Sometimes there were concurrent modification when adding multiple links in the
        // product component editor at once (add existing --> multi select). This fixes the problem
        // but does not fix the root of the problem.
        for (IProductCmptLink link : linkContainer.getLinksAsList()) {
            if (link.findAssociation(ipsProject).equals(association)
                    && link.getTarget().equals(target.getQualifiedName())) {
                return false;
            }
        }
        return true;
    }
}
