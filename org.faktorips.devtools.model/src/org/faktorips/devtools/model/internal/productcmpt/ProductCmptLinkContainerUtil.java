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

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;

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

    private ProductCmptLinkContainerUtil() {
        // hidden constructor for util class
    }

    /**
     * Checks whether a new link as instance of the given {@link IProductCmptTypeAssociation product
     * component type association} and the given target will be valid for the link container.
     * 
     * @param ipsProject The project whose IPS object path is used for the search. This is not
     *            necessarily the project this component is an element of.
     * 
     * @return <code>true</code> if a new relation with the given values will be valid,
     *         <code>false</code> otherwise.
     * 
     * @throws IpsException if a problem occur during the search of the type hierarchy.
     */
    public static boolean canCreateValidLink(IProductCmptLinkContainer linkContainer,
            IProductCmpt target,
            IProductCmptTypeAssociation association,
            IIpsProject ipsProject) {

        if (association == null || target == null || !linkContainer.getProductCmpt().getIpsSrcFile().isMutable()) {
            return false;
        }
        if (linkContainer.getLinksAsList(association.getName()).size() >= association.getMaxCardinality()) {
            return false;
        }
        if (!linkContainer.isContainerFor(association)) {
            return false;
        }

        // it is not valid to create more than one relation with the same type and target.
        if (isLinkExisting(linkContainer, association, target, ipsProject)) {
            return false;
        }
        if (!isTypeCorrect(target, association, ipsProject)) {
            return false;
        }

        return isProjectCorrect(linkContainer, target);
    }

    private static boolean isProjectCorrect(IProductCmptLinkContainer linkContainer, IProductCmpt target) {

        IIpsProject linkContainerProject = linkContainer.getIpsProject();
        IIpsProject targetProject = target.getIpsProject();

        if (linkContainerProject.equals(targetProject)) {
            return true;
        }

        return linkContainerProject.isReferencing(targetProject);
    }

    private static boolean isTypeCorrect(IProductCmpt target,
            IProductCmptTypeAssociation association,
            IIpsProject ipsProject) {

        IProductCmptType targetType = target.findProductCmptType(ipsProject);
        if (targetType == null) {
            return false;
        }
        return targetType.isSubtypeOrSameType(association.findTarget(ipsProject), ipsProject);
    }

    private static boolean isLinkExisting(IProductCmptLinkContainer linkContainer,
            IAssociation association,
            IProductCmpt target,
            IIpsProject ipsProject) {

        for (IProductCmptLink link : linkContainer.getLinksAsList()) {
            if (link.findAssociation(ipsProject).equals(association)
                    && link.getTarget().equals(target.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
}
