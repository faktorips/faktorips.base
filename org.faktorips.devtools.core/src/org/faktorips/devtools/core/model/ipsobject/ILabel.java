/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.util.Locale;

/**
 * A label is an object that has a locale, a value and a plural value.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public interface ILabel extends IIpsObjectPart {

    public static final String XML_TAG_NAME = "Label"; //$NON-NLS-1$

    public static final String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    public static final String PROPERTY_PLURAL_VALUE = "pluralValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "LABEL-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this label is not supported by the IPS
     * project.
     */
    public final static String MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT = MSGCODE_PREFIX
            + "LocaleNotSupportedByIpsProject"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this label is missing.
     */
    public final static String MSGCODE_LOCALE_MISSING = MSGCODE_PREFIX + "LocaleMissing"; //$NON-NLS-1$

    /**
     * Returns the locale of this label or null if this information is for some reason not
     * available.
     */
    public Locale getLocale();

    /**
     * Sets the locale of this label.
     * 
     * @param locale The locale of this label
     */
    public void setLocale(Locale locale);

    /**
     * Returns the value of the label or null if this label has no value.
     */
    public String getValue();

    /**
     * Sets the value of the label.
     * <p>
     * The value will be set to the empty string if null is given as value.
     * 
     * @param value The value for the label
     */
    public void setValue(String value);

    /**
     * Returns the plural value of the label or null if this label has no plural label.
     */
    public String getPluralValue();

    /**
     * Sets the plural value of the label.
     * <p>
     * The plural value will be set to the empty string if null is given as plural value.
     * 
     * @param pluralValue The plural value for the label
     */
    public void setPluralValue(String pluralValue);

}
