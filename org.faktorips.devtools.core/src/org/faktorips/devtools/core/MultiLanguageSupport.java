/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.util.ArgumentCheck;

/**
 * This class provides several methods related to multi-language support.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 */
public final class MultiLanguageSupport {

    private final Locale localizationLocale;

    MultiLanguageSupport() {
        String nl = Platform.getNL();
        // As of now, only the language is of concern to us, not the country.
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        localizationLocale = new Locale(nl);
    }

    /**
     * Returns the caption of the given {@link IIpsObjectPartContainer} for the locale that
     * Faktor-IPS uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no caption for that locale, the default caption will be returned. If there is no
     * default caption as well, a last resort caption that is specific to the element is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the localized
     *            caption of.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultCaption(IIpsObjectPartContainer)
     */
    public String getLocalizedCaption(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String localizedCaption = ipsObjectPartContainer.getCaption(getLocalizationLocale());
        if (localizedCaption == null) {
            localizedCaption = getDefaultCaption(ipsObjectPartContainer);
        }
        return localizedCaption;
    }

    /**
     * Returns the plural caption of the given {@link IIpsObjectPartContainer} for the locale that
     * Faktor-IPS uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no plural caption for that locale, the default plural caption will be returned.
     * If there is no default plural caption as well, a last resort plural caption that is specific
     * to the element is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the localized
     *            plural caption of.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultPluralCaption(IIpsObjectPartContainer)
     */
    public String getLocalizedPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String localizedPluralCaption = ipsObjectPartContainer.getPluralCaption(getLocalizationLocale());
        if (localizedPluralCaption == null) {
            localizedPluralCaption = getDefaultPluralCaption(ipsObjectPartContainer);
        }
        return localizedPluralCaption;
    }

    /**
     * Returns the caption of the given {@link IIpsObjectPartContainer} for the default language.
     * The default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no caption for that locale, a last resort caption that is specific to the element
     * is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the default
     *            caption of.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public String getDefaultCaption(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String defaultCaption = null;
        Locale defaultLocale = getDefaultLocale(ipsObjectPartContainer.getIpsProject());
        if (defaultLocale != null) {
            defaultCaption = ipsObjectPartContainer.getCaption(defaultLocale);
        }
        if (defaultCaption == null) {
            defaultCaption = ipsObjectPartContainer.getLastResortCaption();
        }
        return defaultCaption;
    }

    /**
     * Returns the plural caption of the given {@link IIpsObjectPartContainer} for the default
     * language. The default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural caption for that locale, a last resort plural caption that is specific
     * to the element is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} to obtain the default
     *            plural caption of.
     * 
     * @throws CoreException If any error occurs while retrieving the caption.
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public String getDefaultPluralCaption(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        ArgumentCheck.notNull(ipsObjectPartContainer);

        String defaultPluralCaption = null;
        Locale defaultLocale = getDefaultLocale(ipsObjectPartContainer.getIpsProject());
        if (defaultLocale != null) {
            defaultPluralCaption = ipsObjectPartContainer.getPluralCaption(defaultLocale);
        }
        if (defaultPluralCaption == null) {
            defaultPluralCaption = ipsObjectPartContainer.getLastResortPluralCaption();
        }
        return defaultPluralCaption;
    }

    /**
     * Returns the label of the given {@link ILabeledElement} for the locale that Faktor-IPS uses at
     * the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no label for that locale, the default label will be returned. If there is no
     * default label as well, the pure name of the element is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the localized label of.
     * 
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultLabel(ILabeledElement)
     */
    public String getLocalizedLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);

        String label;
        ILabel localizedLabel = labeledElement.getLabel(getLocalizationLocale());
        if (localizedLabel == null) {
            label = getDefaultLabel(labeledElement);
        } else {
            label = localizedLabel.getValue();
        }
        return label;
    }

    /**
     * Returns the plural label of the given {@link ILabeledElement} for the locale that Faktor-IPS
     * uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no plural label for that locale, the default label will be returned. If there is
     * no default label as well, the pure name of the element is returned.
     * <p>
     * Never returns <tt>null</tt>.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the localized plural label of.
     * 
     * @throws NullPointerException If <tt>ipsObjectPartContainer</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultPluralLabel(ILabeledElement)
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public String getLocalizedPluralLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        String label;
        ILabel localizedLabel = labeledElement.getLabel(getLocalizationLocale());
        if (localizedLabel == null) {
            label = getDefaultPluralLabel(labeledElement);
        } else {
            label = localizedLabel.getPluralValue();
        }
        return label;
    }

    /**
     * Returns the label of the given {@link ILabeledElement} for the default language. The default
     * language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no label for that locale or no default language is specified, the pure name of
     * the element is returned.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the default label of.
     * 
     * @throws NullPointerException If <tt>labeledElement</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public String getDefaultLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);

        String label = labeledElement.getName();
        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            label = defaultLabel.getValue();
        }
        return label;
    }

    /**
     * Returns the plural label of the given {@link ILabeledElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural label for that locale or no default language is specified, the pure
     * name of the element is returned.
     * 
     * @param labeledElement The {@link ILabeledElement} to obtain the default plural label of.
     * 
     * @throws NullPointerException If <tt>labeledElement</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public String getDefaultPluralLabel(ILabeledElement labeledElement) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        String pluralLabel = labeledElement.getName();
        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            pluralLabel = defaultLabel.getPluralValue();
        }
        return pluralLabel;
    }

    /**
     * Sets the label of the given {@link ILabeledElement} for the default language. The default
     * language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no label for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param labeledElement The {@link ILabeledElement} to set the default label for.
     * @param value The value to set the label to.
     * 
     * @throws NullPointerException If <tt>labeledElement</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public void setDefaultLabel(ILabeledElement labeledElement, String value) {
        ArgumentCheck.notNull(labeledElement);

        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            defaultLabel.setValue(value);
        }
    }

    /**
     * Sets the plural label of the given {@link ILabeledElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no plural label for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param labeledElement The {@link ILabeledElement} to set the default plural label for.
     * @param pluralValue The value to set the plural label to.
     * 
     * @throws NullPointerException If <tt>labeledElement</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException If the given {@link ILabeledElement} does not support plural
     *             labels.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public void setDefaultPluralLabel(ILabeledElement labeledElement, String pluralValue) {
        ArgumentCheck.notNull(labeledElement);
        ArgumentCheck.isTrue(labeledElement.isPluralLabelSupported());

        ILabel defaultLabel = getDefaultLabelPart(labeledElement);
        if (defaultLabel != null) {
            defaultLabel.setPluralValue(pluralValue);
        }
    }

    /**
     * Returns the description of the given {@link IDescribedElement} for the locale that Faktor-IPS
     * uses at the time this operation is called to internationalize Faktor-IPS elements.
     * <p>
     * If there is no description for that locale, the default description will be returned. If
     * there is no default description as well, an empty string is returned.
     * 
     * @param describedElement The {@link IDescribedElement} to obtain the localized description of.
     * 
     * @throws NullPointerException If <tt>describedElement</tt> is <tt>null</tt>.
     * 
     * @see #getLocalizationLocale()
     * @see #getDefaultDescription(IDescribedElement)
     */
    public String getLocalizedDescription(IDescribedElement describedElement) {
        ArgumentCheck.notNull(describedElement);

        String description = ""; //$NON-NLS-1$
        IDescription localizedDescription = describedElement.getDescription(getLocalizationLocale());
        if (localizedDescription == null) {
            description = getDefaultDescription(describedElement);
        } else {
            description = localizedDescription.getText();
        }
        return description;
    }

    /**
     * Returns the description of the given {@link IDescribedElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no description for that locale or no default language is specified, an empty
     * string is returned.
     * 
     * @param describedElement The {@link IDescribedElement} to obtain the default description of.
     * 
     * @throws NullPointerException If <tt>describedElement</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public String getDefaultDescription(IDescribedElement describedElement) {
        ArgumentCheck.notNull(describedElement);

        String description = ""; //$NON-NLS-1$
        IDescription defaultDescription = getDefaultDescriptionPart(describedElement);
        if (defaultDescription != null) {
            description = defaultDescription.getText();
        }
        return description;
    }

    /**
     * Sets the description of the given {@link IDescribedElement} for the default language. The
     * default language is specified trough the IPS project the element belongs to.
     * <p>
     * If there is no description for that locale or no default language is specified, nothing will
     * happen.
     * 
     * @param describedElement The {@link IDescribedElement} to set the default description for.
     * @param text The description text to set.
     * 
     * @throws NullPointerException If <tt>describedElement</tt> or <tt>text</tt> is <tt>null</tt>.
     * 
     * @see IIpsProjectProperties#getDefaultLanguage()
     */
    public void setDefaultDescription(IDescribedElement describedElement, String text) {
        ArgumentCheck.notNull(new Object[] { describedElement, text });

        IDescription defaultDescription = getDefaultDescriptionPart(describedElement);
        if (defaultDescription != null) {
            defaultDescription.setText(text);
        }
    }

    private ILabel getDefaultLabelPart(ILabeledElement labeledElement) {
        ILabel labelPart = null;
        Locale defaultLocale = getDefaultLocale(labeledElement.getIpsProject());
        if (defaultLocale != null) {
            labelPart = labeledElement.getLabel(defaultLocale);
        }
        return labelPart;
    }

    private IDescription getDefaultDescriptionPart(IDescribedElement describedElement) {
        IDescription descriptionPart = null;
        Locale defaultLocale = getDefaultLocale(describedElement.getIpsProject());
        if (defaultLocale != null) {
            descriptionPart = describedElement.getDescription(defaultLocale);
        }
        return descriptionPart;
    }

    /**
     * Returns the locale of the {@link ISupportedLanguage} that is set as the default language for
     * the given {@link IIpsProject}.
     * <p>
     * Returns <tt>null</tt> if no default language is set.
     */
    private Locale getDefaultLocale(IIpsProject ipsProject) {
        Locale defaultLocale = null;
        ISupportedLanguage defaultLanguage = ipsProject.getProperties().getDefaultLanguage();
        if (defaultLanguage != null) {
            defaultLocale = defaultLanguage.getLocale();
        }
        return defaultLocale;
    }

    /**
     * Returns the locale that Faktor-IPS uses to internationalize things like descriptions and
     * labels. Currently, this is the locale that is specified at the startup of Eclipse but at a
     * later point may be changed so that it can be configured via the IPS preferences for example.
     * <p>
     * Note that this is <strong>not</strong> the locale used for internationalization of Faktor-IPS
     * itself.
     * 
     * @see IpsPlugin#getUsedLanguagePackLocale()
     */
    public Locale getLocalizationLocale() {
        return localizationLocale;
    }

}
