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

import java.util.List;

import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IPolicyCmptLinkCardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Element;

final class PolicyCmptLinkCardinalityTestUtil {

    private PolicyCmptLinkCardinalityTestUtil() {
        // utility class
    }

    static void newConfigurableAssociations(IPolicyCmptType policyCmptType, String... targetRoleSingulars) {
        for (String targetRoleSingular : targetRoleSingulars) {
            IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
            association.setTargetRoleSingular(targetRoleSingular);
            association.setTarget(policyCmptType.getQualifiedName());
            association.setCardinalityConfigurable(true);
        }
    }

    static void newCardinalities(IProductCmptLinkContainer linkContainer, String... associations) {
        for (String association : associations) {
            linkContainer.newPolicyCmptLinkCardinality(association);
        }
    }

    static void newCardinalitiesWithDescendingMax(IProductCmptLinkContainer linkContainer, String... associations) {
        for (int i = 0; i < associations.length; i++) {
            linkContainer.newPolicyCmptLinkCardinality(associations[i]).setMaxCardinality(associations.length - i);
        }
    }

    static List<Integer> maxCardinalitiesOf(IProductCmptLinkContainer linkContainer) {
        return linkContainer.getPolicyCmptLinkCardinalities().stream()
                .map(IPolicyCmptLinkCardinality::getMaxCardinality).toList();
    }

    static List<String> associationsOf(IProductCmptLinkContainer linkContainer) {
        return linkContainer.getPolicyCmptLinkCardinalities().stream().map(IPolicyCmptLinkCardinality::getAssociation)
                .toList();
    }

    static List<String> associationsInXml(Element containerElement) {
        return XmlUtil.getChildElements(containerElement, IPolicyCmptLinkCardinality.TAG_NAME).stream()
                .map(element -> element.getAttribute(IPolicyCmptLinkCardinality.PROPERTY_ASSOCIATION)).toList();
    }

    static Element reverseCardinalityElements(Element containerElement) {
        List<Element> cardinalityElements = XmlUtil.getChildElements(containerElement,
                IPolicyCmptLinkCardinality.TAG_NAME);
        cardinalityElements.reversed().forEach(containerElement::appendChild);
        return containerElement;
    }

    static Element moveFirstCardinalityElementToEnd(Element containerElement) {
        Element firstCardinalityElement = XmlUtil
                .getChildElements(containerElement, IPolicyCmptLinkCardinality.TAG_NAME).getFirst();
        containerElement.appendChild(firstCardinalityElement);
        return containerElement;
    }
}
