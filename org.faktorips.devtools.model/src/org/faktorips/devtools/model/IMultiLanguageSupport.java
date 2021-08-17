/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Locale;

import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.value.IValue;

public interface IMultiLanguageSupport {

    /**
     * Returns the caption of the given {@link IIpsObjectPartContainer} for the locale that
     * Faktor-IPS uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no caption for that locale, the default caption will be returned. If there is no
     * default caption as well, a last resort caption that is specific to the element is returned.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the localized
     *            caption of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultCaption(IIpsObjectPartContainer)
     */
    String getLocalizedCaption(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the plural caption of the given {@link IIpsObjectPartContainer} for the locale that
     * Faktor-IPS uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no plural caption for that locale, the default plural caption will be returned.
     * If there is no default plural caption as well, a last resort plural caption that is specific
     * to the element is returned.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the localized
     *            plural caption of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultPluralCaption(IIpsObjectPartContainer)
     */
    String getLocalizedPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the caption of the given {@link IIpsObjectPartContainer} for the default language.
     * The default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no caption for that locale, a last resort caption that is specific to the element
     * is returned.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the default
     *            caption of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    String getDefaultCaption(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the plural caption of the given {@link IIpsObjectPartContainer} for the default
     * language. The default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural caption for that locale, a last resort plural caption that is specific
     * to the element is returned.
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the default
     *            plural caption of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    String getDefaultPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the label of the given {@link ILabeledElement} for the locale that Faktor-IPS uses at
     * the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no label for that locale, the default label will be returned. If there is no
     * default label as well, the pure name of the element is returned (capitalized).
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the localized label of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultLabel(ILabeledElement)
     */
    String getLocalizedLabel(ILabeledElement labeledElement);

    /**
     * Returns the plural label of the given {@link ILabeledElement} for the locale that Faktor-IPS
     * uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no plural label for that locale, the default label will be returned. If there is
     * no default label as well, the pure name of the element is returned (capitalized).
     * <p>
     * Never returns <code>null</code>.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the localized plural label of.
     * 
     * @throws NullPointerException If <code>ipsObjectPartContainer</code> is <code>null</code>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultPluralLabel(ILabeledElement)
     * @see ILabeledElement#isPluralLabelSupported()
     */
    String getLocalizedPluralLabel(ILabeledElement labeledElement);

    /**
     * Returns the label of the given {@link ILabeledElement} for the default language. The default
     * language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no label for that locale or no default language is specified, the pure name of
     * the element is returned (capitalized).
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the default label of.
     * 
     * @throws NullPointerException If <code>labeledElement</code> is <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    String getDefaultLabel(ILabeledElement labeledElement);

    /**
     * Returns the plural label of the given {@link ILabeledElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural label for that locale or no default language is specified, the pure
     * name of the element is returned (capitalized).
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the default plural label of.
     * 
     * @throws NullPointerException If <code>labeledElement</code> is <code>null</code>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     * @see ILabeledElement#isPluralLabelSupported()
     */
    String getDefaultPluralLabel(ILabeledElement labeledElement);

    /**
     * Returns the description of the given {@link IDescribedElement} for the locale that Faktor-IPS
     * uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no description for that locale, the default description will be returned. If
     * there is no default description as well, an empty string is returned.
     * 
     * @param describedElement The {@link IDescribedElement} to obtain the localized description of.
     * 
     * @throws NullPointerException If <code>describedElement</code> is <code>null</code>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultDescription(IDescribedElement)
     */
    String getLocalizedDescription(IDescribedElement describedElement);

    /**
     * Returns the description of the given {@link IDescribedElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no description for that locale or no default language is specified, an empty
     * string is returned.
     * 
     * @param describedElement The {@link IDescribedElement} to obtain the default description of.
     * 
     * @throws NullPointerException If <code>describedElement</code> is <code>null</code>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    String getDefaultDescription(IDescribedElement describedElement);

    /**
     * Returns the locale that Faktor-IPS uses to internationalize things like descriptions and
     * labels. Currently, this is the locale that is specified at the startup of Eclipse but at a
     * later point may be changed so that it can be configured via the IPS preferences for example.
     * <p>
     * Note that this is <strong>not</strong> the locale used for internationalization of Faktor-IPS
     * itself.
     * 
     * @see #getUsedLanguagePackLocale()
     */
    Locale getLocalizationLocale();

    /**
     * Returns the locale that Faktor-IPS uses to internationalize things like descriptions and
     * labels if this locale is supported by the given {@link IIpsProject}. If the locale is not
     * supported the default locale of the project will be returned.
     * 
     * @see #getLocalizationLocale()
     */
    Locale getLocalizationLocaleOrDefault(IIpsProject ipsProject);

    /**
     * Returns the localized content of the {@link IValue}.
     * <p>
     * If there is no content for the environment locale, the content for the default project
     * language is returned. If in turn the content based on the project language is missing, the
     * first non-empty content (saved in the value) will be returned. If all else fails an empty
     * string is returned.
     * 
     * @param value the value
     * @param ipsProject the actual {@link IIpsProject} of this value
     */
    String getLocalizedContent(IValue<?> value, IIpsProject ipsProject);

    /**
     * Returns the locale used by the localization. The returned locale is not the locale the
     * localization <strong>should use</strong>, but it is the locale the localization
     * <strong>actually uses</strong>.
     * <p>
     * That means if the default locale this plug-in runs is for example de_DE, but no language pack
     * for German is installed, the localization uses the English language, and this method will
     * return the Locale for "en".
     */
    Locale getUsedLanguagePackLocale();

}