/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.util.Locale;

import org.faktorips.runtime.internal.DescriptionXmlHelper;
import org.faktorips.runtime.internal.InternationalStringXmlReaderWriter;

/**
 * A description is an object that has a locale and a text.
 *
 * @since 3.1
 *
 * @author Alexander Weickmann
 */
public interface IDescription extends IIpsObjectPart {

    String XML_TAG_NAME = DescriptionXmlHelper.XML_ELEMENT_DESCRIPTION;

    String PROPERTY_LOCALE = DescriptionXmlHelper.XML_ATTRIBUTE_LOCALE;

    String PROPERTY_TEXT = InternationalStringXmlReaderWriter.XML_ATTR_TEXT;

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "DESCRIPTION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this description is not supported by
     * the IPS project.
     */
    String MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT = MSGCODE_PREFIX
            + "LocaleNotSupportedByIpsProject"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this description is missing.
     */
    String MSGCODE_LOCALE_MISSING = MSGCODE_PREFIX + "LocaleMissing"; //$NON-NLS-1$

    /**
     * Returns the locale of this description or null if this information is for some reason not
     * available.
     */
    Locale getLocale();

    /**
     * Sets the locale of this description.
     *
     * @param locale The locale of this description
     */
    void setLocale(Locale locale);

    /**
     * Returns the text of this description.
     */
    String getText();

    /**
     * Sets the text of this description.
     *
     * @param text The text for this description
     */
    void setText(String text);

}
