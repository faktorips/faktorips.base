/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import java.util.Locale;

/**
 * A description is an object that has a locale and a text.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public interface IDescription extends IIpsObjectPart {

    public static final String XML_TAG_NAME = "Description"; //$NON-NLS-1$

    public static final String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public static final String PROPERTY_TEXT = "text"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "DESCRIPTION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this description is not supported by
     * the IPS project.
     */
    public final static String MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT = MSGCODE_PREFIX
            + "LocaleNotSupportedByIpsProject"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the locale of this description is missing.
     */
    public final static String MSGCODE_LOCALE_MISSING = MSGCODE_PREFIX + "LocaleMissing"; //$NON-NLS-1$

    /**
     * Returns the locale of this description or null if this information is for some reason not
     * available.
     */
    public Locale getLocale();

    /**
     * Sets the locale of this description.
     * 
     * @param locale The locale of this description
     */
    public void setLocale(Locale locale);

    /**
     * Returns the text of this description.
     */
    public String getText();

    /**
     * Sets the text of this description.
     * 
     * @param text The text for this description
     */
    public void setText(String text);

}
