/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.type;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.model.type.Documentation;

/**
 * Common interface for {@link IDescribedElement described elements} that can be overridden:
 * {@link IOverridableLabeledElement overridable labeled elements}, {@link IPolicyCmptType policy component
 * types}, {@link IProductCmptType product component types} and {@link IEnumType enum types}.
 */
public interface IOverridableElement extends IDescribedElement {

    /**
     * Returns the first element that is overridden by this element.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     */
    IOverridableElement findOverriddenElement(IIpsProject ipsProject);

    /**
     * Returns the element's {@link IDescription description} text for the given {@link Locale
     * locale}. If the the element has no description for the locale and overrides another element,
     * the description text of the super element is returned. If the description contains an
     * {inheritDesc} annotation, it will be replaced with the description of the super element.
     */
    default String getDescriptionTextFromThisOrSuper(Locale locale) {
        String descriptionText = getDescriptionText(locale);
        if (StringUtils.isEmpty(descriptionText)) {
            IOverridableElement superElement = findOverriddenElement(getIpsProject());
            if (superElement != null) {
                descriptionText = superElement.getDescriptionTextFromThisOrSuper(locale);
            }
        }

        if (descriptionText.contains(Documentation.INHERIT_DESCRIPTION_TAG)) {
            IOverridableElement superElement = findOverriddenElement(getIpsProject());
            if (superElement != null) {
                descriptionText = descriptionText.replace(Documentation.INHERIT_DESCRIPTION_TAG,
                        superElement.getDescriptionTextFromThisOrSuper(locale));
            }
        }
        return descriptionText;
    }
}
