/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.enumtype.EnumAttribute;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Utility class for handling documentation of model elements.
 */
public class Documentation {

    /**
     * This tag, if included in an element's description, is replaced by the description of the
     * super-element overwritten by the element.
     */
    public static final String INHERIT_DESCRIPTION_TAG = "{inheritDesc}";

    private Documentation() {
        // utility class
    }

    /**
     * Returns the documentation of the given type for the given model element in the given locale.
     * If the documentation is empty and the element has a super-element(provided via the
     * super-element getter) it overwrites, that super-element's documentation is retrieved instead.
     * <p>
     * Should the documentation contain the tag {@value #INHERIT_DESCRIPTION_TAG}, the
     * super-element's documentation is inserted in this element's documentation, replacing that
     * tag.
     * <p>
     * In the case that the documentation is still empty and the element is not a {@link TypePart},
     * the given fallback value is returned.
     * 
     * @param element the documented element
     * @param type the documentation type
     * @param locale the desired locale
     * @param fallback the text to be returned when no documentation is found
     * @param superElementGetter provides access to the element's super-element if necessary
     * 
     * @param <E> the model element's type, to make sure the super-element getter matches the type
     */
    public static <E extends ModelElement> String of(E element,
            DocumentationKind type,
            Locale locale,
            String fallback,
            Supplier<Optional<? extends E>> superElementGetter) {
        String documentation = fallback;
        MessagesHelper messageHelper = element.getMessageHelper();
        if (messageHelper != null) {
            String docFallback = (element instanceof TypePart || element instanceof EnumAttribute) ? StringUtils.EMPTY
                    : fallback;
            documentation = messageHelper.getMessageOr(element.getMessageKey(type), locale, docFallback);
            if (IpsStringUtils.isEmpty(documentation)) {
                Optional<? extends E> superElement = superElementGetter.get();
                if (superElement.isPresent()) {
                    documentation = superElement.get().getDocumentation(locale, type, docFallback);
                }
            } else if (documentation.contains(INHERIT_DESCRIPTION_TAG)) {
                Optional<? extends E> superElement = superElementGetter.get();
                if (superElement.isPresent()) {
                    documentation = documentation.replace(INHERIT_DESCRIPTION_TAG,
                            superElement.get().getDocumentation(locale, type, fallback));
                }
            }
            if (IpsStringUtils.isEmpty(documentation)) {
                documentation = fallback;
            }
        }
        return documentation;
    }

}
