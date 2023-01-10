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
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.ipsobject.ILabel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;

/**
 * Common interface for {@link IOverridableElement overridable elements} and {@link ILabeledElement
 * labeled elements} that can be overridden: {@link IAttribute attributes}, {@link IMethod methods},
 * {@link IAssociation associations} and {@link IEnumAttribute enum attributes}.
 */
public interface IOverridableLabeledElement extends IOverridableElement, ILabeledElement {

    /**
     * Returns the element's {@link ILabel label} value for the given {@link Locale locale}. If the
     * the element has no label for the locale and overrides another element, the label value of the
     * super element is returned.
     */
    default String getLabelValueFromThisOrSuper(Locale locale) {
        String labelValue = getLabelValue(locale);
        if (StringUtils.EMPTY.equals(labelValue)) {
            IOverridableLabeledElement superPart = (IOverridableLabeledElement)findOverriddenElement(getIpsProject());
            if (superPart != null) {
                return superPart.getLabelValueFromThisOrSuper(locale);
            }
        }
        return labelValue;
    }
}
