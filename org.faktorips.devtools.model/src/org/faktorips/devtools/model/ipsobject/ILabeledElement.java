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

import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;

/**
 * A labeled element is an element that supports attaching {@link ILabel ILabels} in different
 * languages to it. Labels can be attached for all languages that are supported by the IPS project
 * the labeled element belongs to.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 * 
 * @see ILabel
 * @see IIpsProjectProperties#getSupportedLanguages()
 */
public interface ILabeledElement extends IIpsElement {

    /**
     * Returns the {@link ILabel} for the given {@link Locale} or null if no label for the locale
     * exists.
     * 
     * @param locale The locale to retrieve the label for
     * 
     * @throws NullPointerException If the parameter is null
     */
    ILabel getLabel(Locale locale);

    /**
     * Returns the list of labels this element currently has attached.
     * <p>
     * Note that only a defensive copy is returned. The labels are ordered according to the order of
     * the supported languages as they occur in the {@code .ipsproject} file.
     */
    List<ILabel> getLabels();

    /**
     * Returns whether this element has a plural label.
     */
    boolean isPluralLabelSupported();

    /**
     * Creates a new label for this element.
     */
    ILabel newLabel();

    /**
     * Returns the value of the {@link ILabel} that has the given {@link Locale} or null if no such
     * label exists.
     * 
     * @param locale The {@link Locale} of the {@link ILabel} to obtain the value for
     * 
     * @throws NullPointerException If the parameter is null
     */
    String getLabelValue(Locale locale);

    /**
     * Returns the plural value of the {@link ILabel} that has the given {@link Locale} or null if
     * no such label exists.
     * 
     * @param locale The {@link Locale} of the {@link ILabel} to obtain the plural value for
     * 
     * @throws NullPointerException If the parameter is null
     */
    String getPluralLabelValue(Locale locale);

    /**
     * Sets the value of the {@link ILabel} that has the given {@link Locale}.
     * 
     * @param locale The {@link Locale} of the {@link ILabel} to set the value for
     * @param value The value to set or null to set the value to the empty string
     * 
     * @throws NullPointerException If the parameter locale is null
     * @throws IllegalArgumentException If there is no label for the given locale
     */
    void setLabelValue(Locale locale, String value);

    /**
     * Sets the plural value of the {@link ILabel} that has the given {@link Locale}.
     * 
     * @param locale The {@link Locale} of the {@link ILabel} to set the plural value for
     * @param pluralValue The plural value to set or null to set plural value to the empty string
     * 
     * @throws NullPointerException If the parameter locale is null
     * @throws IllegalArgumentException If there is no label for the given locale
     */
    void setPluralLabelValue(Locale locale, String pluralValue);

}
